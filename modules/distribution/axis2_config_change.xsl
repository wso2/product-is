<?xml version="1.0" encoding="UTF-8"?>
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
