package org.wso2.carbon.is.migration.util;

public enum Schema{
    IDENTITY("identity"),
    UM("um");

    private String schemaName ;
    private Schema(String schemaName){
        this.schemaName = schemaName ;
    }

    public String getName(){
        return schemaName ;
    }
}