/* global $, chrome */

var Extension = {
  alarm : null,
  URL : 'https://myearsareburning-159618.appspot-preview.com/'
};

Extension.getNextUpdateSecs = function() {
  if (Extension.alarm == null) {
    return null;
  }
  var next = Extension.alarm.scheduledTime;
  var now = new Date().getTime();
  return Math.ceil((next - now) / 1000);
};

Extension.noStatus = function() {
  var title = 'No notifications loaded, yet.';
  var nextUpdate = Extension.getNextUpdateSecs();
  if (nextUpdate != null) {
    title += ' (Next update in ' + nextUpdate + ' seconds.)';
  }
  chrome.browserAction.setTitle({title : title});
  chrome.browserAction.setIcon({path: 'assets/icon.png'});
};

Extension.caughtUp = function() {
  chrome.browserAction.setTitle({title : 'All caught up!'});
  chrome.browserAction.setIcon({path: 'assets/read.png'});
};

Extension.unread = function(num) {
  chrome.browserAction.setTitle({title : num + ' unread notifications.'});
  chrome.browserAction.setIcon({path: 'assets/unread.png'});
};

var fetchNotifications = function(alarm) {
  chrome.alarms.get(
    "update-github-notifications",
    function(alarm) {
      Extension.alarm = alarm;
    }
  );
  chrome.runtime.sendMessage({get:'notifications'}, receiveNotifications);
};

chrome.alarms.onAlarm.addListener(fetchNotifications);
document.addEventListener('DOMContentLoaded', fetchNotifications);

var receiveNotifications = function(response) {
  // This is just a bug.
  if (response == null) {
    console.log('Got null response from background tab.');
    return;
  }

  var popup = new Popup(response);
  switch (response.state) {
  case 'startup':
    popup.startup();
    break;
  case 'login':
    popup.login();
    break;
  case 'error':
    popup.error();
    break;
  case 'loaded':
    popup.loaded();
  }
};

var createRepoHeader = function(repo) {
  var url = Popup.HTML_PREFIX + repo;
  var a = $('<a/>').attr('href', '#').text(url);
  a.on('click', function() {
    chrome.tabs.create({url:url});
    return false;
  });
  var th = $('<th/>').attr('colspan', '3');
  var tr = $('<tr/>');
  a.appendTo(th);
  th.appendTo(tr);
  return tr;
};

var Popup = function(response) {
  this.response_ = response;
  this.div_ = $('#github-notifications');
  this.div_.empty();
};

Popup.API_PREFIX = 'https://api.github.com/repos/';
Popup.HTML_PREFIX = 'https://github.com/';

Popup.prototype.startup = function() {
  var nextUpdate = Extension.getNextUpdateSecs();
  if (nextUpdate == null) {
    this.div_.text('Still loading...' );
  } else {
    this.div_.text('Fetching in ' + nextUpdate + ' seconds.');
  }
  Extension.noStatus();
};

Popup.prototype.error = function() {
  this.div_.text(this.response_.message);
  Extension.noStatus();
};

Popup.prototype.loaded = function() {
  var a = $('<a/>').attr('href', '#').text('Please login to get started.');
  a.on('click', function() {
    chrome.tabs.create({url:Extension.URL + 'login'});
    return false;
  });
  a.appendTo(this.div_);
};

Popup.prototype.loaded = function() {
    var notificationMap = this.response_.notifications;
  if (notificationMap.length == 0) {
    this.div_.text('All caught up!');
    Extension.caughtUp();
    return;
  }

  var table = $('<table/>').attr('class', 'table table-hover');
  var count = 0;
  for (var repo in notificationMap) {
    var notifications = notificationMap[repo];
    if (notifications.length == 0) {
      continue;
    }
    createRepoHeader(repo).appendTo(table);
    for (var i = 0; i < notifications.length; ++i) {
      var notification = notifications[i];
      if (notification.id in this.response_.muted) {
        // TODO: this could display an empty section.
        continue;
      }
      count++;
      var reason = Popup.getReasonSymbol(notification.reason);
      var url = Popup.getUrl(notification.url);
      var tr = $('<tr/>').attr('id', notification.id);
      $('<td/>').html(reason).appendTo(tr);
      var a = $('<a/>')
            .attr('href', '#')
            .text(notification.title);
      a.on('click', function() {
        chrome.tabs.create({url:url});
        return false;
      });
      a.appendTo($('<td/>').appendTo(tr));
      var mute = $('<button/>').attr('type', 'button')
            .addClass('btn btn-outline-primary btn-sm')
            .text('Mute')
            .appendTo($('<td/>').appendTo(tr));
      mute.on('click', function() {
        var tr = $(this).parent().parent();
        chrome.runtime.sendMessage(
          {post:'mute', id:tr.attr('id')}, receiveNotifications);
        tr.remove();
      });
      tr.appendTo(table);
    }
  }
  table.appendTo(this.div_);
  Extension.unread(count);
};

Popup.getReasonSymbol = function(reason) {
  switch (reason) {
  case "mention":
    return "@";
  case "state_change":
    return "&#916;";  // Delta.
  case "author":
    return "&#9997";  // Hand holding pen.
  case "comment":
    return "&#128172";  // Speech bubble.
  case "assign":
    return '&#8618;';  // Arrow.
  }
  return "?";
};

// There's probably a better way to get these, but so far they seem
// pretty consistently formatted.
Popup.getUrl = function(apiUrl) {
  return Popup.HTML_PREFIX + apiUrl.substring(Popup.API_PREFIX.length);
};
