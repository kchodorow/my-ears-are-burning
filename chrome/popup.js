document.addEventListener('DOMContentLoaded', function () {
  var greeting = {greeting: "What's your vector, Victor?"};
  chrome.runtime.sendMessage(greeting, function(response) {
    console.log("response: " + JSON.stringify(response));
    if (response) {
      var elem = document.getElementById("github-notifications");
      elem.textContent = response.farewell;
    }
  });
});
