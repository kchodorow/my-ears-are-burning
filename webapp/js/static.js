/* global $ */

var loadFromServer = function() {
  var cookieParser = new CookieParser();
  if (!cookieParser.isLoggedIn()) {
    return;
  }

  $('#username').text(cookieParser.get('username'));
  $('#login').addClass('active');
  $('#step-0').remove();
  var user = new User();
  user.generateList();
};

var loadHeader = function() {
  var cookieParser = new CookieParser();
  if (!cookieParser.isLoggedIn()) {
    return;
  }
  var username = cookieParser.get('username');
  var login = $('#login').attr('href', '/user').text(username);
  $('<a/>').attr('href', '/logout').text('Log out')
    .appendTo($('<li/>').appendTo(login.parent().parent()));
};

var CookieParser = function() {
  this.id_ = this.get('id');
  this.loggedIn_ = this.id_ != null;
};

CookieParser.prototype.isLoggedIn = function() {
  return this.loggedIn_;
};

CookieParser.prototype.get = function(cookieId) {
  var cookieStr = document.cookie;
  if (!cookieStr) {
    return null;
  }
  var id = null;
  var cookies = cookieStr.split(';');
  for (var i=0; i < cookies.length; ++i) {
    var cookie = cookies[i].trim();
    var crumbs = cookie.split('=');
    if (crumbs[0] == cookieId) {
      return crumbs[1];
    }
  }
  return null;
};

var User = function() {
};

User.prototype.generateList = function() {
  $.getJSON('/api/repositories').done(function(json) {
    if (!json.ok) {
      // TODO: handle error.
      return;
    }

    var div = $('#step-1-list');
    for (var i = 0; i < json.repositories.length; ++i) {
      let repo = json.repositories[i];
      var a = $('<a/>').attr('href', '#')
        .addClass('list-group-item list-group-item-action')
        .text('https://github.com/' + repo.name + ' (' + repo.count + ' notifications)')
        .appendTo(div);
      if (json.tracked.indexOf(repo.name) >= 0) {
        a.addClass('active');
      }
      a.on('click', function() {
        var elem = this;
        if ($(elem).hasClass('active')) {
          $.post('/api/repositories', {action : 'untrack', repo : repo.name})
            .done(function() {
            $(elem).removeClass('active');
          }).fail(failLogger);
        } else {
          $.post('/api/repositories', {action : 'track', repo : repo.name})
            .done(function() {
            $(elem).addClass('active');
          }).fail(failLogger);
        }
        return false;
      });
    }

    if (json.subscribed) {
      var unsubscribe = $('<button/>').text('Unsubscribe');
      unsubscribe.on('click', function() {
        $.getJSON('/unsubscribe', function() {
          $('#subscribe').text('Sorry to see you go!');
        });
      });
      $('#subscribe').empty().append(unsubscribe);
    }
  }).fail(failLogger);
};

/**
 * Log function for debugging.
 */
var failLogger = function(jqxhr, textStatus, error) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
