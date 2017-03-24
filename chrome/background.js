var URL = "https://myearsareburning-159618.appspot-preview.com/";

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    sendResponse({farewell: "Roger that, Roger."});
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
