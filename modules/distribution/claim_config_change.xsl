<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Dialect[@dialectURI='urn:ietf:params:scim:schemas:extension:enterprise:2.0:User']">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="not(Claim/ClaimURI='urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:accountLocked')">
                <Claim>
                    <ClaimURI>urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:accountLocked</ClaimURI>
                    <DisplayName>Account Locked</DisplayName>
                    <AttributeID>accountLocked</AttributeID>
                    <Description>Account Locked</Description>
                    <Required />
                    <DisplayOrder>1</DisplayOrder>
                    <SupportedByDefault />
                    <MappedLocalClaim>http://wso2.org/claims/identity/accountLocked</MappedLocalClaim>
                </Claim>
            </xsl:if>
            <xsl:if test="not(Claim/ClaimURI='urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:forcePasswordReset')">
                <Claim>
                    <ClaimURI>urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:forcePasswordReset</ClaimURI>
                    <DisplayName>Force Password Reset</DisplayName>
                    <AttributeID>forcePasswordReset</AttributeID>
                    <Description>Temporary claim to invoke forced password reset feature</Description>
                    <Required />
                    <DisplayOrder>1</DisplayOrder>
                    <SupportedByDefault />
                    <MappedLocalClaim>http://wso2.org/claims/identity/adminForcedPasswordReset</MappedLocalClaim>
                </Claim>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
