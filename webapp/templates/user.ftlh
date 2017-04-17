<div class="jumbotron">
  <h2><img src="" id="profile-pic" />User settings for <span id="username">(loading)</span></h3>

  <div id="lead"></div>

  <div>
    <h3>Repositories</h3>
    <p>Choose repositories to track:</p>
    <div class="list-group" id="tracking-list"></div>

    <div id="subscribe">
      <p>
        By default, "My ears are burning" will track one repository. If you'd like to track
        multiple repositories, please subscribe to help support running this service. $1.99/month,
        cancel anytime.
      </p>
      <form action="/subscribe" method="POST">
        <script
           src="https://checkout.stripe.com/checkout.js" class="stripe-button"
           data-key="pk_live_t63MSyivfNGjxJfKs0j1NNAz"
           data-amount="199"
           data-name="My ears are burning"
           data-description="Monthly subscription"
           data-image="/assets/ear.png"
           data-locale="auto"
           data-zip-code="true"
           data-label="Subscribe">
        </script>
      </form>
    </div>
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
