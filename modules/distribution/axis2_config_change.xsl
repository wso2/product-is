<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
  ~
  ~ WSO2 LLC. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <!-- Identity transform -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Insert mail transportSender -->
    <xsl:template match="//axisconfig/transportSender">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>

        <transportSender name="mailto" class="org.apache.axis2.transport.mail.MailTransportSender">
            <parameter name="mail.smtp.from">{{output_adapter.email.from_address}}</parameter>
            <parameter name="mail.smtp.user">{{output_adapter.email.username}}</parameter>
            <parameter name="mail.smtp.password">{{output_adapter.email.password}}</parameter>
            <parameter name="mail.smtp.host">{{output_adapter.email.hostname}}</parameter>
            <parameter name="mail.smtp.port">{{output_adapter.email.port}}</parameter>
            <parameter name="mail.smtp.starttls.enable">{{output_adapter.email.enable_start_tls}}</parameter>
            <parameter name="mail.smtp.auth">{{output_adapter.email.enable_authentication}}</parameter>
        </transportSender>
    </xsl:template>
</xsl:stylesheet>
