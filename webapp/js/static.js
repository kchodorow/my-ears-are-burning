/* global $ */

var loadFromServer = function() {
  // 1. See if the user is logged in.
  var cookieStr = document.cookie;
  if (!cookieStr) {
    return;
  }
  var id = null;
  var cookies = cookieStr.split(';');
  for (var i=0; i < cookies.length; ++i) {
    var cookie = cookies[i];
    var crumbs = cookie.split('=');
    if (crumbs[0] == 'id') {
      id = crumbs[1];
    }
  }
  if (id == null) {
    return;
  }

  // 2. The visitor is logged in, give next steps.
  var user = new User(id);
  user.generateList();
};

var User = function(id) {
  this.id_ = id;
};

User.prototype.generateList = function() {
  $.getJSON('/api/repositories').done(function(json) {
    if (!json.ok) {
      // TODO: handle error.
      return;
    }

    var login = $('#login')
      .attr('href', '/user/' + json.name)
      .text(json.name);
    $('<a/>').attr('href', '/logout').text('Log out')
      .appendTo($('<li/>').appendTo(login.parent().parent()));

    var next = $('#next').empty();
    $('<p/>').text('Choose a repositories to track:').appendTo(next);
    var div = $('<div/>').addClass('list-group').appendTo(next);
    for (var i = 0; i < json.repositories.length; ++i) {
      var repo = json.repositories[i];
      var a = $('<a/>').attr('href', '#')
        .addClass('list-group-item list-group-item-action')
        .text('https://github.com/' + repo.name + ' (' + repo.count + ' notifications)')
        .appendTo(div);
      if (json.tracked.indexOf(repo.name) >= 0) {
        a.addClass('active');
      }
      a.on('click', function() {
        var elem = this;
        $.post('/api/repositories', {track : repo.name}).done(function() {
          $(elem).addClass('active');
        }).fail(failLogger);
        return false;
      });
    }
    $('<div/>').html(
      'Or enter a different repository: <p>'
        + 'https://github.com/'
        + '<input id="repo-user" name="user" type="text" placeholder="User or organization"/>'
        + '/<input id="repo-repo" name="repo" type="text" placeholder="Repository name"/>'
        + '<button type="submit">Track</button></p>')
      .appendTo(next);
    $('<div/>').html('<p><em>If you\'d like to track more than one repository, please '
      + '<a href="/subscribe">subscribe</a> to help cover the costs of '
      + 'running this service.</em></p>').appendTo(next);
  }).fail(failLogger);
};

/**
 * Log function for debugging.
 */
var failLogger = function( jqxhr, textStatus, error ) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
