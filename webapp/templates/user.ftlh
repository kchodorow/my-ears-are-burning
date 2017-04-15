<div class="jumbotron">
  <h3 id="username"></h3>

  <div id="lead"></div>

    <div id="step-1">
      <p>Choose repositories to track:</p>
      <div class="list-group" id="tracking-list"></div>
    </div>

    <center>
      <div id="subscribe">
        <p>
          By default, "My ears are burning" will track one repository. If you'd like to track
          multiple repositories, please subscribe to help support running this
          service. $1.99/month, Cancel anytime.
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
    </center>

    <center>
      <a href="https://chrome.google.com/webstore/detail/my-ears-are-burning/phkokjkjpdmdeongeaaddaiocjpcmpjb">
        <img src="/assets/screenshot2.png" />
        <p>Download the Chrome extension.</p>
      </a>
    </center>

  </div>

  <div>
    <button id="delete-account">Delete account</button>
  </div>
</div>

<script>
loadFromServer();
</script>
