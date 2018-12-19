package org.wso2.identity.scenarios.commons;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class ProcessJsonFile {

    private static String fileName;
    private static String configFile;
    private static String rootPath;
    private static String filePath = "/payload/";
    static JSONObject rootObject;


    /**
     *
     * @param jsonFileName the file name read from the folder locatiom
     * @throws Exception
     */
    public static void readFile(String jsonFileName) throws Exception {

        fileName = jsonFileName;
        rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        configFile = rootPath + filePath + fileName;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public static JSONObject getJsonObject() throws ParseException, IOException {

        JSONParser parser = new JSONParser();
        rootObject = (JSONObject) parser.parse(new FileReader(configFile));
        return rootObject;
    }
}
