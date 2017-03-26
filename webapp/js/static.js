/* global $ */

var loadFromServer = function() {
  // 1. See if the user is logged in.
  var user = checkLogin();
  if (user == null) {
    return;
  }
  $('#login')
    .attr('href', '/user/' + user.name)
    .text(user.name);

  // 2. See if the repository name has been set.
  if (user.repository) {
    var parts = user.repository.split('/');
    $('#repo-user').attr('value', parts[0]);
    $('#repo-repo').attr('value', parts[1]);
  }
};

var checkLogin = function() {
  return {name : 'kchodorow', repository : 'bazelbuild/bazel'};
};
