<?php include "/opt/lampp/htdocs/password/layout/header.php"; ?>
<?php
error_reporting(E_ALL ^ E_NOTICE);
if ($_REQUEST['work'] == "sent"){
    $curl = curl_init();
    curl_setopt_array($curl, array(
        CURLOPT_URL => "https://localhost/oauth2/token",
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => "",
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_SSL_VERIFYHOST => 0,
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => "POST",
        CURLOPT_POSTFIELDS => "grant_type=" . $_POST['grant_type'] . "&code_verifier=" . $_POST['code_verifier'] . "&code=" . $_POST['code'] . "&redirect_uri=" . $_POST['redirect_uri'] . "&client_id=" . $_POST['client_id'],
        CURLOPT_HTTPHEADER => array(
            'authorization: Basic ' . base64_encode($_POST['client_id'] . ":" . $_POST['client_secret']),
            "cache-control: no-cache",
            "code_verifier: " . $_POST['code_verifier'],
            "content-type: application/x-www-form-urlencoded"
        ),
    ));

    $response = curl_exec($curl);
    $err = curl_error($curl);

    curl_close($curl);

    if ($err) {
        echo "cURL Error #:" . $err;
    } else {
        echo $response;
    }
}
else{
?>
<body>
<!--this is sending curl request so it won't support Token binding-->
<h3 class="col-md-offset-3 col-md-5">Authorization Token Request TB</h3>
<br>
<form name="myForm" action="authorization.php?work=sent" method="POST" class="col-md-offset-3 col-md-5">
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
                    value="authorization_code"></br> </div>

    </div>
    <div class="form-group">
        <label>Code</label>
        <div><input type="text" name="code" class="form-control" placeholder="code"></br> </div>

    </div>
    <div class="form-group">
        <label>Code Verifier</label>
        <div><input type="text" name="code_verifier" class="form-control" placeholder="code_verifier"></br> </div>

    </div>

    <div class="form-group">
        <label>Redirect URI</label>
        <div><input type="text" name="redirect_uri" class="form-control" placeholder="redirect uri"></br> </div>

    </div>
    <button id="BB" type="submit" class="btn btn-primary" value="Submit">Submit</button>

</form>
<?php }
include "/opt/lampp/htdocs/password/layout/footer.php"; ?>
