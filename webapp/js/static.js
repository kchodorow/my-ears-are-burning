/* global $ */

var loadFromServer = function() {
  // 1. See if the user is logged in.
  var cookieStr = document.cookie;
  if (!cookieStr) {
    $('#step-0').removeClass('invisible').addClass('visible');
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
    $('#step-0').removeClass('invisible').addClass('visible');
    return;
  }

  // 2. The visitor is logged in, give next steps.
  $('#step-0').remove();
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

    var next = $('#step-1').removeClass('invisible').addClass('visible');
    var div = $('#step-1-list');
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
  }).fail(failLogger);
};

/**
 * Log function for debugging.
 */
var failLogger = function(jqxhr, textStatus, error) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
