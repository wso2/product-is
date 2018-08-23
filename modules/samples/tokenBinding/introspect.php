<?php include "/opt/lampp/htdocs/password/layout/header.php"; ?>

<?php
error_reporting(E_ALL ^ E_NOTICE);
if ($_REQUEST['work'] == "sent") {
    $curl = curl_init();

    curl_setopt_array($curl, array(
        CURLOPT_URL => "https://localhost/oauth2/introspect",
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => "",
        CURLOPT_SSL_VERIFYPEER => false,
        CURLOPT_SSL_VERIFYHOST => 0,
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => "POST",
        CURLOPT_POSTFIELDS => "token=" . $_POST['token'],
        CURLOPT_HTTPHEADER => array(
            'authorization: Basic ' . base64_encode($_POST['name'] . ":" . $_POST['password']),
            "cache-control: no-cache",
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
} else {
    ?>

    <body>
    <h3 class="col-md-offset-3 col-md-5">Introspection TB</h3>
    <br>
    <form name="myForm" action="introspect.php?work=sent" method="POST" class="col-md-offset-3 col-md-5">
        <div class="form-group">
            <label>Name</label>
            <div><input type="text" name="name" class="form-control" placeholder="Name" value="admin"></br> </div>

        </div>
        <div class="form-group">
            <label>Password</label>
            <div><input type="text" name="password" class="form-control" placeholder="Password" value="admin"></br>
            </div>

        </div>
        <div class="form-group">
            <label>Token</label>
            <div><input type="text" name="token" class="form-control" placeholder="token"></br> </div>
        </div>
        <button id="BB" type="submit" class="btn btn-primary" value="Submit">Submit</button>

    </form>
<?php }
include "/opt/lampp/htdocs/password/layout/footer.php"; ?>

