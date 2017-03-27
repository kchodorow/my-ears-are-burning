/* global chrome */

var URL = "https://myearsareburning-159618.appspot-preview.com/";

var response = {
  notifications : [],
  state : "startup",
  message : null
};

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    sendResponse(response);
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
    var githubResponse = x.response;
    if (!githubResponse) {
      response.state = "error";
      response.message = 'No response from ' + URL + '!';
      return;
    }
    response.notifications = githubResponse.notifications;
    response.state = "loaded";
  };
  x.onerror = function() {
    response.state = "error";
    response.message = 'Network error.';
  };
  x.send();
}
