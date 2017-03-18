chrome.browserAction.onClicked.addListener(function(tab) {
  var cookieDetails = {
    url : "https://myearsareburning-159618.appspot-preview.com/api/notifications",
    name : "id",
  };
  chrome.cookies.get(cookieDetails, function(cookie) {
    console.log("User id: " + cookie.value);
  });
});
