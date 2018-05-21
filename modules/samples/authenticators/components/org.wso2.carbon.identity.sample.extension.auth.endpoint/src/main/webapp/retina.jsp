<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page language="java" session="true" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils" %>
<%

    String retinaValue = request.getParameter("retinaValue");
    String callbackUrl = request.getParameter("callbackUrl");

    if (retinaValue != null && callbackUrl != null) {
        String cbURL = URLDecoder.decode(callbackUrl, StandardCharsets.UTF_8.name());
        cbURL = FrameworkUtils.appendQueryParamsStringToUrl(cbURL, "success=true");
        /* This is for demonstration purposes only.
        You need to properly encode the parameters to avoid security breaches. */
        response.sendRedirect(cbURL);
    }
%>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WSO2 Identity Server Sample</title>

    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->

</head>

<body>

<!-- page content -->
<div class="container-fluid body-wrapper">

    <div class="row">
        <div class="col-md-12">

            <!-- content -->
            <div
                    class="container-face-detection col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title blue-bg padding-double white boarder-bottom-blue margin-none">Face Detection
                    </h2>
                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">

                        <form action="" method="post" id="loginForm">
                            <div class="videoContainer Aligner">
                                <div id="container" class="Aligner-item">
                                    <video id="videoel" height="300" width="400" preload="auto" loop playsinline autoplay>
                                    Please wait while loading...
                                    </video>
                                    <canvas id="overlay" height="300" width="400"></canvas>
                                </div>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <!--This is for demonstration purposes only.
                                You need to properly encode the query parameters before adding to the page source to
                                avoid security breaches. -->
                                <input id="callbackUrl" name="callbackUrl" hidden="hidden" value="<%=callbackUrl%>"/>
                                <input id="retinaValue" name="retinaValue" hidden="hidden" value="retinaValue"/>
                            </div>

                            <br>

                            <div class="form-actions">
                                <button
                                        class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="button" id="faceDetectionBtn"
                                        data-loading-text="<span class='glyphicon glyphicon-repeat fast-right-spinner'></span> processing...">
                                        Scan
                                </button>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">

                                <br/>

                            </div>

                            <div class="clearfix"></div>
                        </form>

                        <div class="clearfix"></div>
                    </div>
                </div>
                <!-- /content -->
            </div>
            <div class="container-face-detection col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <div class="demo-warn">
                    <div class="alert alert-danger" role="alert">
                        <div class="glyphicon glyphicon-warning-sign"></div>
                        <p><b>This is only for demonstration...!</b></p>
                        <p>There is no real functionality provided.</p>
                    </div>
                </div>
            </div>
        </div>
        <!-- /content/body -->

    </div>
</div>

<script src="libs/jquery-2.2.4/jquery-2.2.4.min.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<script src="libs/clmtracker-demo/utils.js"></script>
<script src="libs/clmtracker-demo/clmtrackr.js"></script>
<script>
    // getUserMedia only works over https in Chrome 47+, so we redirect to https. Also notify user if running from file.
    if (window.location.protocol == "file:") {
        alert("You seem to be running this example directly from a file. Note that these examples only work when served from a server or localhost due to canvas cross-domain restrictions.");
    } else if (window.location.hostname !== "localhost" && window.location.protocol !== "https:"){
        window.location.protocol = "https";
    }
</script>
<script>
    var fdb = $("#faceDetectionBtn");
    fdb.click(function () {
        $(this).button('loading');
        $(this).data("loading-text", "<span class='glyphicon glyphicon-repeat fast-right-spinner'></span> processing...");
        setTimeout(identify, 3000);
        setTimeout(done, 6000);
        setTimeout(submitPage, 7000);
    });

    function identify() {
        fdb.text("Identifying...");
    }
    function done() {
        fdb.text("Done.");
    }
    function submitPage() {
        $("#loginForm").submit();
    }

    var vid = document.getElementById('videoel');
    var vid_width = vid.width;
    var vid_height = vid.height;
    var overlay = document.getElementById('overlay');
    var overlayCC = overlay.getContext('2d');

    /*********** Setup of video/webcam and checking for webGL support *********/
    var cWidth = $("#container").width();
    $("#videoel").prop("width", cWidth);
    $("#overlay").prop("width", cWidth);
    var insertAltVideo = function (video) {
        // insert alternate video if getUserMedia not available
        if (supports_video()) {
            if (supports_webm_video()) {
                video.src = "./media/cap12_edit.webm";
            } else if (supports_h264_baseline_video()) {
                video.src = "./media/cap12_edit.mp4";
            } else {
                return false;
            }
            return true;
        } else return false;
    }

    function adjustVideoProportions() {
        // resize overlay and video if proportions of video are not 4:3
        // keep same height, just change width
        var proportion = vid.videoWidth / vid.videoHeight;
        vid_width = Math.round(vid_height * proportion);
        vid.width = vid_width;
        overlay.width = vid_width;
    }

    function gumSuccess(stream) {
        // add camera stream if getUserMedia succeeded
        if ("srcObject" in vid) {
            vid.srcObject = stream;
        } else {
            vid.src = (window.URL && window.URL.createObjectURL(stream));
        }
        vid.onloadedmetadata = function () {
            adjustVideoProportions();
            vid.play();
        }
        vid.onresize = function () {
            adjustVideoProportions();
            if (trackingStarted) {
                ctrack.stop();
                ctrack.reset();
                ctrack.start(vid);
            }
        }
    }

    function gumFail() {
        // fall back to video if getUserMedia failed
        insertAltVideo(vid);
        document.getElementById('gum').className = "hide";
        document.getElementById('nogum').className = "nohide";
        alert("There was some problem trying to fetch video from your webcam, using a fallback video instead.");
    }

    navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;
    window.URL = window.URL || window.webkitURL || window.msURL || window.mozURL;

    // set up video
    if (navigator.mediaDevices) {
        navigator.mediaDevices.getUserMedia({video: true}).then(gumSuccess).catch(gumFail);
    } else if (navigator.getUserMedia) {
        navigator.getUserMedia({video: true}, gumSuccess, gumFail);
    } else {
        insertAltVideo(vid);
        document.getElementById('gum').className = "hide";
        document.getElementById('nogum').className = "nohide";
        alert("Your browser does not seem to support getUserMedia, using a fallback video instead.");
    }

    /*********** Code for face tracking *********/

    var ctrack = new clm.tracker();
    ctrack.init();
    var trackingStarted = false;
    startVideo();
    function startVideo() {
        // start video
        vid.play();
        // start tracking
        ctrack.start(vid);
        trackingStarted = true;
        // start loop to draw face
        drawLoop();
    }

    function drawLoop() {
        requestAnimFrame(drawLoop);
        overlayCC.clearRect(0, 0, vid_width, vid_height);
        //psrElement.innerHTML = "score :" + ctrack.getScore().toFixed(4);
        if (ctrack.getCurrentPosition()) {
            ctrack.draw(overlay);
        }
    }

</script>

</body>
</html>
