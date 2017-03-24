var URL = "https://myearsareburning-159618.appspot-preview.com/";

var notifications = [
  {
    reason : "state_change",
    title : "[Feature request] Bazel should cache already downloaded repositories and dependencies",
    url : "https://github.com/bazelbuild/bazel/issues/1050"
  },
  {
    reason : "mention",
    title : "Add cargo_crate repository rule",
    url : "https://github.com/bazelbuild/rules_rust/issues/2"
  },
  {
    reason : "mention",
    title : "Having trouble compiling bazel from source locally on Redhat",
    url : "https://github.com/bazelbuild/bazel/issues/comments/284363006"
  },
  {
    reason : "mention",
    title : "Move to bazelbuild GitHub org",
    url : "https://github.com/bazelbuild/bazel/issues/comments/284363006"
  }
];

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    sendResponse({notifications: notifications});
  }
);

chrome.browserAction.onClicked.addListener(function(tab) {
  var cookieDetails = {
    url : URL,
    name : "id"
  };
  chrome.cookies.get(cookieDetails, function(cookie) {
    if (cookie == null) {
      handleLogin();
    }
    console.log("User id: " + cookie.value);
    var userId = cookie.value;
    pollForNotifications(userId);
  });
});

function handleLogin() {
  // TODO
}

function pollForNotifications() {
  var notificationUrl = URL + 'api/notifications?id=' + userId;
  var x = new XMLHttpRequest();
  x.open('GET', searchUrl);
  x.responseType = 'json';
  x.onload = function() {
    var response = x.response;
    if (!response || !response.responseData || !response.responseData) {
      errorCallback('No response from ' + URL + '!');
      return;
    }
    console.log(response.responseData);
  };
  x.onerror = function() {
    errorCallback('Network error.');
  };
  x.send();
}
