/* global $ */

var Extension = {
  DEFAULT_ICON:'assets/icon.png',
  UNREAD_ICON:'assets/unread.png',
  READ_ICON:'assets/read.png'
};

document.addEventListener('DOMContentLoaded', function () {
  chrome.runtime.sendMessage({}, fetchNotifications);
});

var Popup = {};

Popup.NO_NOTIFICATIONS = "No notifications loaded, yet.";

var fetchNotifications = function(response) {
  var icon = _fetchNotifications(response);
  chrome.browserAction.setIcon({path: icon});
};

var _fetchNotifications = function(response) {

  if (!response) {
    console.log("No response.");
    return Extension.DEFAULT_ICON;
  }

  var mainDiv = $('#github-notifications');
  mainDiv.empty();

  var notifications = response.notifications;
  if (notifications.length == 0) {
    mainDiv.html(Popup.NO_NOTIFICATIONS);
    return Extension.READ_ICON;
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
  return Extension.UNREAD_ICON;
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
