<?php include "/opt/lampp/htdocs/password/layout/header.php"; ?>


<h2 class="col-md-offset-3 col-md-5">GRANT TYPE</h2>

<div class="container  col-md-offset-3 col-md-5">
    <div>
        <button id="code" class="btn btn-info" style="margin-top:50px">AUTHORIZATION_CODE</button>
    </div>
    <div>
        <button id="auth" class="btn btn-primary" style="margin-top:25px">AUTHORIZATION</button>
    </div>
    <div>
        <button id="password" class="btn btn-success" style="margin-top:25px">PASSWORD</button>
    </div>
    <div>
        <button id="refresh" class="btn btn-warning" style="margin-top:25px">REFRESH</button>
    </div>
    <div>
        <button id="introspect" class="btn btn-danger" style="margin-top:25px">INTROSPECT</button>
    </div>
</div>

<script>
    var password = document.getElementById('password');
    password.addEventListener('click', function () {
        document.location.href = 'password.php';
    });
    var code = document.getElementById('code');
    code.addEventListener('click', function () {
        document.location.href = 'code.php';
    });
    var auth = document.getElementById('auth');
    auth.addEventListener('click', function () {
        document.location.href = 'authorization.php';
    });
    var refresh = document.getElementById('refresh');
    refresh.addEventListener('click', function () {
        document.location.href = 'refresh.php';
    });

    var introspect = document.getElementById('introspect');
    introspect.addEventListener('click', function () {
        document.location.href = 'introspect.php';
    });

</script>


<?php include "/opt/lampp/htdocs/password/layout/footer.php"; ?>
