<div class="jumbotron">
  <h2><img src="" id="profile-pic" />User settings for <span id="username">(loading)</span></h3>

  <div id="lead"></div>

  <div>
    <h3>Repositories</h3>
    <p>Show or hide notifications from the repositories below (by default, all repositories will
    be tracked):</p>
    <div class="list-group" id="tracking-list"></div>
  </div>

  <div>
    <h3>Extension</h3>
    <a href="https://chrome.google.com/webstore/detail/my-ears-are-burning/phkokjkjpdmdeongeaaddaiocjpcmpjb">
      <p>Download the Chrome extension.</p>
    </a>
  </div>

  <div>
    <h3>Manage account</h3>
    <button id="delete-account">Delete account</button>
  </div>
</div>

<script>
loadFromServer();
</script>
