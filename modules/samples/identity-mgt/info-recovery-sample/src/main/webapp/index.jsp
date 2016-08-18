<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>


<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


<%--<jsp:include page="includes/header_landing.jsp"></jsp:include>--%>
<jsp:include page="includes/header_login.jsp"></jsp:include>

<!-- Header -->
<div class="intro-header">

    <div class="container">

        <div class="row">
            <div class="span16">
                <div class="intro-message">
                    <h1>WSO2IS Demo Application</h1>
                    <br><br>

                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam vel luctus
                        odio. </p>
                    <hr class="intro-divider">
                    <p>Proin eleifend porttitor ante. Vestibulum accumsan felis in efficitur
                        eleifend. </p>

                </div>
            </div>
        </div>

    </div>
    <!-- /.container -->

</div>
<!-- /.intro-header -->

<!-- Page Content -->

<div class="content-section-a">

    <div class="container">

        <div class="row">
            <div class="span8">
                <hr/>
                <div class="clearfix"></div>
                <h2 class="section-heading">Ut sodales odio turpis, et facilisis sapien feugiat
                    eget.</h2>

                <p class="lead">Ut sodales odio turpis, et facilisis sapien feugiat eget. Ut in
                    blandit ex. Proin eleifend porttitor ante. Vestibulum accumsan felis in
                    efficitur eleifend. In scelerisque vehicula urna, non ornare sapien eleifend
                    eget. </p>
            </div>
            <div class="span8">
                <img class="img-responsive"
                     src="${pageContext.request.contextPath}/assets/img/ipad.png" alt="">
            </div>
        </div>

    </div>
    <!-- /.container -->

</div>


<div class="content-section-b">

    <div class="container">

        <div class="row">
            <div class="span8">
                <img class="img-responsive"
                     src="${pageContext.request.contextPath}/assets/img/phones.png" alt="">
            </div>
            <div class="span8">
                <hr/>
                <div class="clearfix"></div>
                <h2 class="section-heading">Death to the Stock Photo:<br>Special Thanks</h2>

                <p class="lead">A special thanks to <a target="_blank"
                                                       href="http://join.deathtothestockphoto.com/">Death
                    to the Stock Photo</a> for providing the photographs that you see in this
                    template. Visit their website to become a member.</p>
            </div>

        </div>

    </div>
    <!-- /.container -->

</div>

<jsp:include page="includes/footer_landing.jsp"></jsp:include>

