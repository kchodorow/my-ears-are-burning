document.addEventListener('DOMContentLoaded', function () {
  var greeting = {greeting: "What's your vector, Victor?"};
  chrome.runtime.sendMessage(greeting, function(response) {
    if (!response) {
      console.log("No response.");
      return;
    }

    var notifications = response.notifications;
    var html = $('<ul></ul>');
    for (var i = 0; i < notifications.length; ++i) {
      var notification = notifications[i];
      var reason = getReasonSymbol(notification.reason);
      var a = $('<a/>')
            .attr('href', notification.url)
            .text(reason + " " + notification.title);
      var li = $('<li/>');
      a.appendTo(li);
      li.appendTo(html);
    }
    html.appendTo($("#github-notifications"));
  });
});


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
