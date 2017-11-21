<?php include "/opt/lampp/htdocs/password/layout/header.php"; ?>

    <body>
    <br>
    <div class="col-md-offset-3 col-md-5 "><h3>Password TB</h3></div>
    <form name="myForm" action="https://localhost/oauth2/token" method="POST"
          enctype="application/x-www-form-urlencoded" class="col-md-offset-3 col-md-5">
        <div class="form-group">
            <label>Client ID</label>
            <div><input type="text" name="client_id" class="form-control" placeholder="client ID"></br> </div>

        </div>
        <div class="form-group">
            <label>Client Secret</label>
            <div><input type="text" name="client_secret" class="form-control" placeholder="client secret"></br> </div>

        </div>
        <div class="form-group">
            <label>Grant type</label>
            <div><input type="text" name="grant_type" class="form-control" placeholder="grant type"
                        value="password"></br> </div>

        </div>
        <div class="form-group">
            <label>Username</label>
            <div><input type="text" name="username" class="form-control" placeholder="username" value="admin"></br>
            </div>

        </div>
        <div class="form-group">
            <label>Password</label>
            <div><input type="text" name="password" class="form-control" placeholder="password" value="admin"></br>
            </div>

        </div>
        <div class="form-group">
            <label>Redirect URI</label>
            <div><input type="text" name="redirect_uri" class="form-control" placeholder="redirect uri"></br> </div>

        </div>
        <button id="BB" type="submit" class="btn btn-primary" value="Submit">Submit</button>

    </form>
<?php include "/opt/lampp/htdocs/password/layout/footer.php"; ?>

