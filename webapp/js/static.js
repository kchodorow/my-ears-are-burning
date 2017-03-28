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
    console.log("JSON Data: " + JSON.stringify(json));
    if (!json.ok) {
      // TODO: handle error.
      return;
    }

    $('#login')
      .attr('href', '/user/' + json.name)
      .text(json.name);

    $('#next').text("Choose a repository to track.");
    var div = $('<div/>').addClass('list-group').appendTo($('#next'));
    for (var i = 0; i < json.repositories.length; ++i) {
      var repo = json.repositories[i];
      $('<a/>').attr('href', 'https://github.com/' + repo)
        .addClass('list-group-item list-group-item-action')
        .text('https://github.com/' + repo)
        .appendTo(div);
    }
    $('<div/>').html(
      'If you\'d like to track more than one repository, please <a href="/subscribe">subscribe</a> to help cover the costs of running this service.').appendTo('#next');
  }).fail(failLogger);
};

/**
 * Log function for debugging.
 */
var failLogger = function( jqxhr, textStatus, error ) {
  var err = textStatus + ", " + error;
  console.log( "Request Failed: " + err );
};
