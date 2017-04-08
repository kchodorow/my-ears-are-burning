/* global $ */

var loadFromServer = function() {
  var cookieParser = new CookieParser();
  if (!cookieParser.isLoggedIn()) {
    return;
  }

  checkForMessage();

  $('#username').text(cookieParser.get('username'));
  $('#login').addClass('active');
  $('#delete-account').on('click', function() {
    $.get('/delete', function() {
      location.href = '/good-bye';
    });
  });
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

var checkForMessage = function() {
  var pieces = location.href.split('?');
  if (pieces.length == 1) {
    return;
  }

  var query = pieces[1];
  var kv = query.split('=');
  if (kv.length != 2 || kv[0] != 'msg') {
    return;
  }

  var msg = kv[1];
  if (msg == 'subscribe') {
    $('#lead').prepend(
      $('<p/>').text(
        'The extension will now automatically track all repositories unless you'
          + ' deselect them below. You can cancel your subscription anytime'
          + ' with the \'Unsubscribe\' button below.'));
    $('#lead').prepend(
      $('<h2/>').text('Thank you for subscribing!'));
  } else if (msg == 'unsubscribe') {
    $('#lead').prepend(
      $('<p/>').html(
        'Please <a href="mailto:k.chodorow@gmail.com">let us know</a> if you'
          + ' have any feedback you\'d like to share.'));
    $('#lead').prepend(
      $('<h2/>').text('Your subscription has been cancelled.'));
  }
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

    var div = $('#tracking-list');
    div.empty();
    for (var i = 0; i < json.repositories.length; ++i) {
      let repo = json.repositories[i];
      var a = $('<a/>')
            .attr('id', User._getRepoId(repo.name))
            .attr('href', '#')
            .addClass('list-group-item list-group-item-action')
            .text('https://github.com/' + repo.name + ' (' + repo.count + ' notifications)')
            .appendTo(div);
      a.on('click', function() {
        var elem = this;
        var request = {
          action : $(elem).hasClass('active') ? 'untrack' : 'track',
          repo : repo.name
        };
        $.post('/api/repositories', request)
          .done(User.updateTracked)
          .fail(failLogger);
        return false;
      });
    }

    User.updateTracked(json);

    if (json.subscribed) {
      var unsubscribe = $('<button/>').text('Unsubscribe');
      unsubscribe.on('click', function() {
        location.href = '/unsubscribe';
      });
      $('#subscribe').empty().append(unsubscribe);
    }
  }).fail(failLogger);
};

User.updateTracked = function(json) {
  var list = $('#tracking-list');
  list.children().each(function() {
    $(this).removeClass('active');
  });
  for (var i = 0; i < json.tracked.length; ++i) {
    var repoId = User._getRepoId(json.tracked[i]);
    $('#' + repoId).addClass('active');
  }
};

User._getRepoId = function(repo) {
  return 'repo-' + repo.replace(/\//g, '-');
};

/**
 * Log function for debugging.
 */
var failLogger = function(jqxhr, textStatus, error) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
