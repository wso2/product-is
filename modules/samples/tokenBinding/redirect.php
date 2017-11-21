<?php

$c_id = $_POST["client_id"];
$c_sec = $_POST["client_secret"];
$g_type = $_POST["grant_type"];
$redir = $_POST["redirect_uri"];

$body = "client_id=" . $c_id . "&client_secret=" . $c_sec . "&grant_type=" . $g_type . "&redirect_uri=" . $redir;
$code = "client_id=" . $c_id . "&response_type=code&code_challenge=referred_tb&code_challenge_method=referred_tb&redirect_uri=" . $redir;
header("Location: https://localhost/oauth2/authorize?" . $code, TRUE, 302);
header("Include-Referred-Token-Binding-ID: true");
exit();

?>

