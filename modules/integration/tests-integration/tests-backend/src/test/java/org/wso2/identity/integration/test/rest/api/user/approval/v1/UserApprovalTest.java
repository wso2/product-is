package org.wso2.identity.integration.test.rest.api.user.approval.v1;

import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.restassured.SwaggerValidationFilter;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class UserApprovalTest {

    private static final String SWAGGER_JSON_URL =
//            "http://petstore.swagger.io/v2/swagger.json";
                "/Users/ayesha/Downloads/swagger-client-generated/swagger.json";
    private static final int PORT = 9853;
    private static final String SERVER_URL = "https://localhost:9853";
    //    private final SwaggerValidationFilter validationFilter = new SwaggerValidationFilter(SWAGGER_JSON_URL);
    SwaggerRequestResponseValidator swaggerRequestResponseValidator;
    private SwaggerValidationFilter validationFilter;

    @BeforeTest(alwaysRun = true)
    public void testinit() {
        swaggerRequestResponseValidator = SwaggerRequestResponseValidator
                .createFor("/Users/ayesha/Downloads/swagger-client-generated/swagger.json")
                .withAuthHeaderData("Authorization", "Basic YWRtaW46YWRtaW4=").build();
//                .createFor("/Users/ayesha/WORK/WORK1/IAM/5.9.0-Release/REST-API/samples/swagger.json").build();
        validationFilter = new SwaggerValidationFilter(swaggerRequestResponseValidator);

        RestAssured.baseURI = SERVER_URL;
    }

    @Test
    public void testGetValidPet() {

        given()
                .port(PORT)
                .filter(validationFilter)
                .when()
                .get("/pet/1")
                .then()
                .assertThat()
                .statusCode(200);
    }
}
