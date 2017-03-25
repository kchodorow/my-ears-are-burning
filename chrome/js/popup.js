/* global $ */

document.addEventListener('DOMContentLoaded', function () {
  chrome.runtime.sendMessage({}, fetchNotifications);
});

var Popup = {};

Popup.NO_NOTIFICATIONS = "No notifications loaded, yet.";

var fetchNotifications = function(response) {
  if (!response) {
    console.log("No response.");
    return;
  }

  var mainDiv = $('#github-notifications');
  mainDiv.empty();

  var notifications = response.notifications;
  if (notifications.length == 0) {
    mainDiv.html(Popup.NO_NOTIFICATIONS);
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
