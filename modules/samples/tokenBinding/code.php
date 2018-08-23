<?php include "/opt/lampp/htdocs/password/layout/header.php"; ?>


<h2 class="col-md-offset-3 col-md-5">Authorization Code TB</h2>
<form name="myForm" action="redirect.php" method="POST" enctype="application/x-www-form-urlencoded"
      class="col-md-offset-3 col-md-5">
    <div class="form-group">
        <label>Client ID</label>
        <div><input type="text" name="client_id" class="form-control" placeholder="client ID"></br> </div>

    </div>
    <div class="form-group">
        <label>Grant type</label>
        <div><input type="text" name="grant_type" class="form-control" placeholder="grant type" value="code"></br>
        </div>

        <div class="form-group">
            <label>Redirect URI</label>
            <div><input type="text" name="redirect_uri" class="form-control" placeholder="redirect uri"></br> </div>

        </div>
        <button id="BB" type="submit" class="btn btn-primary" value="Submit">Submit</button>

</form>
<?php include "/opt/lampp/htdocs/password/layout/footer.php"; ?>


