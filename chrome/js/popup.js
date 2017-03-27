/* global $, chrome */

var Extension = {
  alarm : null
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
  chrome.runtime.sendMessage({}, receiveNotifications);
};

chrome.alarms.onAlarm.addListener(fetchNotifications);
document.addEventListener('DOMContentLoaded', fetchNotifications);

var receiveNotifications = function(response) {
  // This is just a bug.
  if (response == null) {
    console.log('Got null response from background tab.');
    return;
  }

  var mainDiv = $('#github-notifications');
  mainDiv.empty();

  if (response.state == "error") {
    mainDiv.text(response.message);
    Extension.noStatus();
    return;
  }

  // Before there are any notifications loaded.
  if (response.state == "startup") {
    var nextUpdate = Extension.getNextUpdateSecs();
    if (nextUpdate == null) {
      mainDiv.text('Still loading...' );
    } else {
      mainDiv.text('Fetching in ' + nextUpdate + ' seconds.');
    }
    Extension.noStatus();
    return;
  }

  var notificationMap = response.notifications;
  if (notificationMap.length == 0) {
    mainDiv.text('All caught up!');
    Extension.caughtUp();
    return;
  }

  var table = $('<table/>').attr('class', 'table table-hover');
  for (var repo in notificationMap) {
    createRepoHeader(repo).appendTo(table);
    var notifications = notificationMap[repo];
    for (var i = 0; i < notifications.length; ++i) {
      var notification = notifications[i];
      var reason = Popup.getReasonSymbol(notification.reason);
      var url = Popup.getUrl(notification.url);
      var tr = $('<tr/>');
      $('<td/>').html(reason).appendTo(tr);
      var a = $('<a/>')
            .attr('href', '#')
            .text(notification.title);
      a.on('click', function() {
        chrome.tabs.create({url:url});
        return false;
      });
      a.appendTo($('<td/>')).appendTo(tr);
      tr.appendTo(table);
    }
  }
  table.appendTo(mainDiv);
  Extension.unread(notifications.length);
  return;
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


var Popup = {
  API_PREFIX:'https://api.github.com/repos/',
  HTML_PREFIX:'https://github.com/'
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
