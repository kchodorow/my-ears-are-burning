/* global chrome */

var URL = "https://myearsareburning-159618.appspot-preview.com/";

var BackgroundTask = function() {
};

BackgroundTask.response = {
  notifications : [],
  state : "startup",
  message : null,
  muted : {}
};

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    if ('get' in request && request.get == 'notifications') {
      sendResponse(BackgroundTask.response);
    } else if ('post' in request && request.post == 'mute') {
      BackgroundTask.response.muted[request.id] = true;
    }
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
      BackgroundTask.response.state = "need-login";
      return;
    }
    var userId = cookie.value;
    pollForNotifications(userId);
  });
};

chrome.alarms.onAlarm.addListener(updateNotifications);

function pollForNotifications(userId) {
  BackgroundTask.response.state = "requesting";
  var notificationUrl = URL + 'api/notifications';
  var x = new XMLHttpRequest();
  x.open('GET', notificationUrl);
  x.responseType = 'json';
  x.onload = function() {
    var githubResponse = x.response;
    if (!githubResponse) {
      BackgroundTask.response.state = "error";
      BackgroundTask.response.message = 'No response from server.';
      return;
    }
    BackgroundTask.response.notifications = githubResponse.notifications;
    BackgroundTask.response.state = "loaded";

    var count = 0;
    for (var repo in BackgroundTask.response.notifications) {
      var notifications = BackgroundTask.response.notifications[repo];
      for (var i = 0; i < notifications.length; ++i) {
        var notification = notifications[i];
        if (notification.id in BackgroundTask.response.muted) {
          continue;
        }
        count++;
      }
    }
    BackgroundTask.loaded(count);
  };
  x.onerror = function() {
    BackgroundTask.response.state = "error";
    BackgroundTask.response.message = 'Network error.';
    BackgroundTask.noStatus();
  };
  x.send();
}

BackgroundTask.getNextUpdateSecs = function() {
  if (BackgroundTask.response.alarm == null) {
    return null;
  }
  var next = BackgroundTask.response.alarm.scheduledTime;
  var now = new Date().getTime();
  return Math.ceil((next - now) / 1000);
};

BackgroundTask.noStatus = function() {
  var title = 'No notifications loaded, yet.';
  var nextUpdate = BackgroundTask.getNextUpdateSecs();
  if (nextUpdate != null) {
    title += ' (Next update in ' + nextUpdate + ' seconds.)';
  }
  chrome.browserAction.setTitle({title : title});
  chrome.browserAction.setIcon({path: 'assets/icon.png'});
};

BackgroundTask.loaded = function(num) {
  if (num > 0) {
    chrome.browserAction.setTitle({title : num + ' unread notifications.'});
    chrome.browserAction.setIcon({path: 'assets/unread.png'});
  } else {
    chrome.browserAction.setTitle({title : 'All caught up!'});
    chrome.browserAction.setIcon({path: 'assets/read.png'});
  }
};
