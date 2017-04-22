/* global $, chrome */

var Extension = {
  URL : 'https://meab.kchodorow.com/'
};

var fetchNotifications = function(alarm) {
  chrome.runtime.sendMessage({get:'notifications'}, receiveNotifications);
};

document.addEventListener('DOMContentLoaded', fetchNotifications);

var receiveNotifications = function(response) {
  // This is just a bug.
  if (response == null) {
    console.log('Got null response from background tab.');
    return;
  }

  var popup = new Popup(response);
  switch (response.state) {
  case 'startup':
    popup.startup();
    break;
  case 'need-login':
    popup.login();
    break;
  case 'need-track':
    popup.track();
    break;
  case 'error':
    popup.error();
    break;
  case 'requesting':
    popup.requesting();
    break;
  case 'loaded':
    popup.loaded();
    break;
  default:
    console.log("Nothing matched.");
  }
};

var Popup = function(response) {
  this.response_ = response;
  this.div_ = $('#github-notifications');
  this.div_.empty();
};

Popup.API_PREFIX = 'https://api.github.com/repos/';
Popup.HTML_PREFIX = 'https://github.com/';

Popup.prototype.startup = function() {
  this.div_.text('Still loading...' );
};

Popup.prototype.error = function() {
  this.div_.text(this.response_.message);
};

Popup.prototype.requesting = function() {
  this.div_.text('Requesting notifications! Please stand by...');
};

Popup.prototype.login = function() {
  var a = $('<a/>').attr('href', '#').text('Please login to get started.');
  a.on('click', function() {
    chrome.tabs.create({url:Extension.URL + 'login'});
    return false;
  });
  a.appendTo(this.div_);
};

Popup.prototype.track = function() {
  var a = $('<a/>').attr('href', '#').text('Please choose a repository to track.');
  a.on('click', function() {
    chrome.tabs.create({url:Extension.URL + 'user'});
    return false;
  });
  a.appendTo(this.div_);
};

Popup.prototype.loaded = function() {
  var notificationMap = this.response_.notifications;
  var list = $('<div/>').addClass('list-group');
  var total = 0;
  for (var repo in notificationMap) {
    var notifications = notificationMap[repo];
    if (notifications.length == 0) {
      continue;
    }
    var trs = [];
    for (var i = 0; i < notifications.length; ++i) {
      var notification = notifications[i];
      if (notification.id in this.response_.muted) {
        continue;
      }

      var section = $('<a/>')
            .addClass('list-group-item list-group-item-action flex-column align-items-start')
            .attr('id', notification.id)
            .attr('href', '#');
      var controls = $('<div/>')
            .addClass('d-flex w-100 justify-content-between');
      controls.appendTo(section);

      let url = Popup.getUrl(notification.url);
      if (notification.reason == 'mention'
          && 'mention' in notification) {
        var mention = notification.mention;
        url = mention.url;
        var text = '"' + mention.body + '" - @' + mention.username;
        $('<p/>').addClass('mb-1').text(text).appendTo(section);
      }
      $('<small/>').addClass('text-muted').text(notification.title).appendTo(section);

      section.on('click', function() {
        chrome.tabs.create({url:url});
        return false;
      });
      var mute = $('<button/>').attr('type', 'button')
            .addClass('btn btn-outline-primary btn-sm')
            .text('Done');
      mute.on('click', function() {
        var a = $(this).parent().parent().parent();
        var list = a.parent();
        chrome.runtime.sendMessage(
          {post:'mute', id:a.attr('id')}, receiveNotifications);
        a.remove();
      });
      var reason = Popup.getReasonSymbol(notification.reason);
      $('<h5/>').addClass('mb-1').html(reason).appendTo(controls);
      $('<small/>').html(mute).appendTo(controls);
      trs.push(section);
    }

    // Only display this section if there's at least one notification.
    if (trs.length > 0) {
      for (i = 0; i < trs.length; ++i) {
        trs[i].appendTo(list);
      }
    }
    total += trs.length;
  }

  if (total == 0) {
    this.div_.text("All caught up!");
  } else {
    list.appendTo(this.div_);
    this.div_.addClass('container');
  }
};

Popup.getReasonSymbol = function(reason) {
  var div = $('<div/>');
  switch (reason) {
  case "mention":
    return div.attr('title', 'Mention').html('@');
  case "state_change":
    return div.attr('title', 'State change').html('&#916;');  // Delta.
  case "comment":
    return div.attr('title', 'New comment').html('&#128172;');  // Speech bubble.
  case "assign":
    return div.attr('title', 'Assigned').html('&#8618;');  // Arrow.
  case "review_requested":
    return div.attr('title', 'Code review').html('&#128591;'); // Prayer.
  }
  return div.html("?");
};

// There's probably a better way to get these, but so far they seem
// pretty consistently formatted.
Popup.getUrl = function(apiUrl) {
  return Popup.HTML_PREFIX + apiUrl.substring(Popup.API_PREFIX.length);
};
