/* global $, chrome */

var Extension = {
  URL : 'https://meab.kchodorow.com/'
};

var fetchNotifications = function(alarm) {
  chrome.runtime.sendMessage({get:'notifications'}, receiveNotifications);
};

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
  case 'need-login':
    popup.login();
    break;
  case 'need-track':
    popup.track();
    break;
  case 'error':
    popup.error();
    break;
  case 'requesting':
    popup.requesting();
    break;
  case 'loaded':
    popup.loaded();
    break;
  default:
    console.log("Nothing matched.");
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
  this.div_.text('Still loading...' );
};

Popup.prototype.error = function() {
  this.div_.text(this.response_.message);
};

Popup.prototype.requesting = function() {
  this.div_.text('Requesting notifications! Please stand by...');
};

Popup.prototype.login = function() {
  var a = $('<a/>').attr('href', '#').text('Please login to get started.');
  a.on('click', function() {
    chrome.tabs.create({url:Extension.URL + 'login'});
    return false;
  });
  a.appendTo(this.div_);
};

Popup.prototype.track = function() {
  var a = $('<a/>').attr('href', '#').text('Please choose a repository to track.');
  a.on('click', function() {
    chrome.tabs.create({url:Extension.URL + 'user'});
    return false;
  });
  a.appendTo(this.div_);
};

Popup.prototype.loaded = function() {
  var notificationMap = this.response_.notifications;
  var table = $('<table/>').attr('class', 'table table-hover');
  var total = 0;
  for (var repo in notificationMap) {
    var notifications = notificationMap[repo];
    if (notifications.length == 0) {
      continue;
    }
    var trs = [];
    for (var i = 0; i < notifications.length; ++i) {
      var notification = notifications[i];
      if (notification.id in this.response_.muted) {
        continue;
      }
      var tr = $('<tr/>').attr('id', notification.id);
      var reason = Popup.getReasonSymbol(notification.reason);
      reason.appendTo($('<td/>').appendTo(tr));
      let url = Popup.getUrl(notification.url);
      var a = $('<a/>')
            .attr('href', '#')
            .attr('title', url)
            .text(notification.title);
      a.on('click', function() {
        chrome.tabs.create({url:url});
        return false;
      });
      a.appendTo($('<td/>').appendTo(tr));
      var mute = $('<button/>').attr('type', 'button')
            .addClass('btn btn-outline-primary btn-sm')
            .text('Done')
            .appendTo($('<td/>').appendTo(tr));
      mute.on('click', function() {
        var tr = $(this).parent().parent();
        var tbody = tr.parent();
        chrome.runtime.sendMessage(
          {post:'mute', id:tr.attr('id')}, receiveNotifications);
        tr.remove();
        if (tbody.is(':empty')) {
          var thead = tbody.prev();
          thead.remove();
          tbody.remove();
        }
      });
      trs.push(tr);
    }

    // Only display this section if there's at least one notification.
    if (trs.length > 0) {
      createRepoHeader(repo).appendTo($('<thead/>').appendTo(table));
      var tbody = $('<tbody/>');
      for (i = 0; i < trs.length; ++i) {
        trs[i].appendTo(tbody);
      }
      tbody.appendTo(table);
    }
    total += trs.length;
  }

  if (total == 0) {
    this.div_.text("All caught up!");
  } else {
    table.appendTo(this.div_);
    this.div_.css('height', '496px');
  }
};

Popup.getReasonSymbol = function(reason) {
  var div = $('<div/>');
  switch (reason) {
  case "mention":
    return div.attr('title', 'Mention').html('@');
  case "state_change":
    return div.attr('title', 'State change').html('&#916;');  // Delta.
  case "comment":
    return div.attr('title', 'New comment').html('&#128172;');  // Speech bubble.
  case "assign":
    return div.attr('title', 'Assigned').html('&#8618;');  // Arrow.
  case "review_requested":
    return div.attr('title', 'Code review').html('&#128591;'); // Prayer.
  }
  return div.html("?");
};

// There's probably a better way to get these, but so far they seem
// pretty consistently formatted.
Popup.getUrl = function(apiUrl) {
  return Popup.HTML_PREFIX + apiUrl.substring(Popup.API_PREFIX.length);
};
