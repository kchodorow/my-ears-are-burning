      <div class="jumbotron">
        <h1 id="username"></h1>

        <div>
          <p><a href="https://chrome.google.com/webstore/detail/my-ears-are-burning/phkokjkjpdmdeongeaaddaiocjpcmpjb">Download the Chrome extension</a>.</p>
        </div>

        <div id="step-1">
          <p>Choose repositories to track:</p>
          <div class="list-group" id="tracking-list">
          Loading...
          </div>
        </div>

      <center>
        <div id="subscribe">
          <p>
            By default, "My ears are burning" will track one repository. If you'd like to track
            multiple repositories, please subscribe to help support this service:
          </p>
          <form action="/subscribe" method="POST">
            <script
               src="https://checkout.stripe.com/checkout.js" class="stripe-button"
               data-key="pk_test_YaPfGrJsgo9Y1euMO0keOgS4"
               data-amount="299"
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

      </div>

      <div>
        <button id="delete-account">Delete account</button>
      </div>

      <script>
        loadFromServer();
      </script>
