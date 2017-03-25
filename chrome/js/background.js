var URL = "https://myearsareburning-159618.appspot-preview.com/";

var notifications = null;

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    sendResponse({notifications: notifications});
  }
);

var alarmInfo = {
  delayInMinutes:1.1,
  periodInMinutes:5
};
chrome.alarms.create("update-github-notifications", alarmInfo);

var updateNotifications = function(alarm) {
  var cookieDetails = {
    url : URL,
    name : "id"
  };
  chrome.cookies.get(cookieDetails, function(cookie) {
    if (cookie == null) {
      handleLogin();
    }
    var userId = cookie.value;
    pollForNotifications(userId);
  });
};

chrome.alarms.onAlarm.addListener(updateNotifications);

function handleLogin() {
  // TODO
}

function pollForNotifications(userId) {
  var notificationUrl = URL + 'api/notifications?id=' + userId;
  var x = new XMLHttpRequest();
  x.open('GET', notificationUrl);
  x.responseType = 'json';
  x.onload = function() {
    var response = x.response;
    if (!response) {
      console.log('No response from ' + URL + '!');
      return;
    }
    notifications = response.notifications;
    console.log("updating notifications: " + notifications.length);
  };
  x.onerror = function() {
    console.log('Network error.');
  };
  x.send();
}
