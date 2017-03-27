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
  user.generateInfo();
};

var User = function(id) {
  this.id_ = id;
};

User.prototype.generateInfo = function() {
  $.getJSON('/api/user').done(function(json) {
    if (!json.ok) {
      // TODO: deal with errors.
      return;
    }
    $('#login')
      .attr('href', '/user/' + json.name)
      .text(json.name);

    if (json.repos.length > 0) {
      $('#next').html('Finally, <a href="TODO">install the chrome extension</a>.');
    }
    for (var i = 0; i < json.repos.length; ++i) {
      var repo = json.repos[i];
      $('<li/>').text('https://github.com/' + repo).appendTo($('#tracked'));
    }

    this.generateRepoList();
  }).fail(failLogger);
};

User.prototype.generateList = function() {
  $.getJSON('/api/repositories').done(function(json) {
    console.log("JSON Data: " + JSON.stringify(json));
    if (!json.ok) {
      // TODO: handle error.
      return;
    }
    for (var i = 0; i < json.repositories.length; ++i) {
      var repo = json.repositories[i];
      $('<li>').text(repo).appendTo($('#repo-list'));
    }
  }).fail(failLogger);
};

/**
 * Log function for debugging.
 */
var failLogger = function( jqxhr, textStatus, error ) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
