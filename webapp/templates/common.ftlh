<html>
  <head>
    <title>My ears are burning</title>
    <script src="https://code.jquery.com/jquery-3.2.1.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>
    <script src="js/static.js"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
  </head>
  <body>
    <div class="container">
      <div class="header clearfix">
        <nav>
          <ul class="nav nav-pills pull-right">
            <li role="presentation" <#if page == "index.ftlh">class="active"</#if>><a href="/">Home</a></li>
            <li role="presentation"><a href="https://github.com/kchodorow/my-ears-are-burning/issues">File a bug</a></li>
            <li role="presentation"><a id="login" href="/login">Login</a></li>
          </ul>
        </nav>
        <h3 class="text-muted">
          <img src="/assets/64x64.png" />
          <a href="/">My ears are burning</a>
        </h3>
      </div>

      <#include "${page}">

      <footer class="footer">
        <p><a href="https://github.com/kchodorow/my-ears-are-burning/issues">Contact</a></p>
        <p>&copy; 2017 Kristina Chodorow.</p>
      </footer>

      <script>
        loadHeader();
      </script>

    </div> <!-- /container -->
    <script>
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

        ga('create', 'UA-34508473-2', 'auto');
        ga('send', 'pageview');

    </script>
  </body>
</html>
