<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <!-- Identity transform -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Insert new node after the match -->
    <xsl:template match="//wso2registry/staticConfiguration">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>

        <!-- Inject the new configuration -->
        <indexingConfiguration>
            <startIndexing>false</startIndexing>
            <startingDelayInSeconds>35</startingDelayInSeconds>
            <indexingFrequencyInSeconds>5</indexingFrequencyInSeconds>
            <batchSize>40</batchSize>
            <indexerPoolSize>40</indexerPoolSize>
            <lastAccessTimeLocation>/_system/local/repository/components/org.wso2.carbon.registry/indexing/lastaccesstime</lastAccessTimeLocation>
            <indexers>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.MSExcelIndexer" mediaTypeRegEx="application/vnd.ms-excel"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.MSPowerpointIndexer" mediaTypeRegEx="application/vnd.ms-powerpoint"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.MSWordIndexer" mediaTypeRegEx="application/msword"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.PDFIndexer" mediaTypeRegEx="application/pdf"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.XMLIndexer" mediaTypeRegEx="application/xml"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.XMLIndexer" mediaTypeRegEx="application/(.)+\+xml"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.PlainTextIndexer" mediaTypeRegEx="application/swagger\+json"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.PlainTextIndexer" mediaTypeRegEx="application/(.)+\+json"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.PlainTextIndexer" mediaTypeRegEx="text/(.)+"/>
                <indexer class="org.wso2.carbon.registry.indexing.indexer.PlainTextIndexer" mediaTypeRegEx="application/x-javascript"/>
            </indexers>
            <exclusions>
                <exclusion pathRegEx="/_system/config/repository/dashboards/gadgets/swfobject1-5/.*[.]html"/>
                <exclusion pathRegEx="/_system/local/repository/components/org[.]wso2[.]carbon[.]registry/mount/.*"/>
            </exclusions>
        </indexingConfiguration>
    </xsl:template>
</xsl:stylesheet>
