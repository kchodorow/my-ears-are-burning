/* global $ */

var Extension = {};

Extension.noStatus = function() {
  chrome.browserAction.setTitle({title : 'No notifications loaded, yet.'});
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

  // Before there are any notifications loaded.
  if (response.notifications == null) {
    mainDiv.text('Still loading...');
    Extension.noStatus();
    return;
  }

  var notifications = response.notifications;
  if (notifications.length == 0) {
    mainDiv.text('All caught up!');
    Extension.caughtUp();
    return;
  }

  var table = $('<table/>');
  for (var i = 0; i < notifications.length; ++i) {
    var notification = notifications[i];
    var reason = getReasonSymbol(notification.reason);
    var tr = $('<tr/>');
    $('<td/>').html(reason).appendTo(tr);
    var a = $('<a/>')
          .attr('href', notification.url)
          .text(notification.title);
    a.appendTo($('<td/>')).appendTo(tr);
    tr.appendTo(table);
  }
  table.appendTo(mainDiv);
  Extension.unread(notifications.length);
  return;
};

var getReasonSymbol = function(reason) {
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
