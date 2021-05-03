package org.wso2.identity.integration.test.scim2;

import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.scim2.testsuite.core.entities.Result;
import org.wso2.scim2.testsuite.core.entities.Statistics;
import org.wso2.scim2.testsuite.core.entities.TestResult;
import org.wso2.scim2.testsuite.core.tests.ResourceType;
import org.wso2.scim2.testsuite.core.protocol.EndpointFactory;
import org.wso2.scim2.testsuite.core.utils.ComplianceConstants;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;

public class SCIM2CoreTestCases extends ISIntegrationTest {

    @Test
    public void runCoreTests() {

        EndpointFactory endFactory = new EndpointFactory("https://localhost:9853/scim2", "admin", "admin", "");
        ResourceType user = endFactory.getInstance(ComplianceConstants.EndPointConstants.USER);
        ResourceType group = endFactory.getInstance(ComplianceConstants.EndPointConstants.GROUP);
        ResourceType serviceProviderConfig =
                endFactory.getInstance(ComplianceConstants.EndPointConstants.SERVICEPROVIDERCONFIG);
        ResourceType resourceType = endFactory.getInstance(ComplianceConstants.EndPointConstants.RESOURCETYPE);
        ResourceType schema = endFactory.getInstance(ComplianceConstants.EndPointConstants.SCHEMAS);
        ResourceType self = endFactory.getInstance(ComplianceConstants.EndPointConstants.ME);
        ResourceType bulk = endFactory.getInstance(ComplianceConstants.EndPointConstants.BULK);

        try {
            // Invoke ServiceProviderConfig test.
            ArrayList<TestResult> serviceProviderResult;
            serviceProviderResult = serviceProviderConfig.getMethodTest();
            ArrayList<TestResult> results = new ArrayList<>(serviceProviderResult);

            // Invoke ResourceTypes test.
            ArrayList<TestResult> resourceTypeResult;
            resourceTypeResult = resourceType.getMethodTest();
            results.addAll(resourceTypeResult);

            // Invoke schemas test.
            ArrayList<TestResult> schemaTestResult;
            schemaTestResult = schema.getMethodTest();
            results.addAll(schemaTestResult);

            // Invoke user related tests.
            ArrayList<TestResult> userGetResult;
            userGetResult = user.getMethodTest();
            results.addAll(userGetResult);

            ArrayList<TestResult> userPostResult;
            userPostResult = user.postMethodTest();
            results.addAll(userPostResult);

            ArrayList<TestResult> userPatchResult;
            userPatchResult = user.patchMethodTest();
            results.addAll(userPatchResult);

            ArrayList<TestResult> userSearchResult;
            userSearchResult = user.searchMethodTest();
            results.addAll(userSearchResult);

            ArrayList<TestResult> userPutResult;
            userPutResult = user.putMethodTest();
            results.addAll(userPutResult);

            ArrayList<TestResult> userDeleteResult;
            userDeleteResult = user.deleteMethodTest();
            results.addAll(userDeleteResult);

            ArrayList<TestResult> userGetByIDResult;
            userGetByIDResult = user.getByIdMethodTest();
            results.addAll(userGetByIDResult);

            // Invoke group related tests.
            ArrayList<TestResult> groupGetResult;
            groupGetResult = group.getMethodTest();
            results.addAll(groupGetResult);

            ArrayList<TestResult> groupPostResult;
            groupPostResult = group.postMethodTest();
            results.addAll(groupPostResult);

            ArrayList<TestResult> groupPatchResult;
            groupPatchResult = group.patchMethodTest();
            results.addAll(groupPatchResult);

            ArrayList<TestResult> groupSearchResult;
            groupSearchResult = group.searchMethodTest();
            results.addAll(groupSearchResult);

            ArrayList<TestResult> groupPutResult;
            groupPutResult = group.putMethodTest();
            results.addAll(groupPutResult);

            ArrayList<TestResult> groupDeleteResult;
            groupDeleteResult = group.deleteMethodTest();
            results.addAll(groupDeleteResult);

            ArrayList<TestResult> groupGetByIDResult;
            groupGetByIDResult = group.getByIdMethodTest();
            results.addAll(groupGetByIDResult);

            // Invoke Me related tests.
            ArrayList<TestResult> meGetResult;
            meGetResult = self.getMethodTest();
            results.addAll(meGetResult);

            ArrayList<TestResult> mePostResult;
            mePostResult = self.postMethodTest();
            results.addAll(mePostResult);

            ArrayList<TestResult> mePatchResult;
            mePatchResult = self.patchMethodTest();
            results.addAll(mePatchResult);

            ArrayList<TestResult> mePutResult;
            mePutResult = self.putMethodTest();
            results.addAll(mePutResult);

            ArrayList<TestResult> meDeleteResult;
            meDeleteResult = self.deleteMethodTest();
            results.addAll(meDeleteResult);

            // Invoke Bulk related tests.
            ArrayList<TestResult> bulkPostResult;
            bulkPostResult = bulk.postMethodTest();
            results.addAll(bulkPostResult);

            ArrayList<TestResult> bulkPatchResult;
            bulkPatchResult = bulk.patchMethodTest();
            results.addAll(bulkPatchResult);

            ArrayList<TestResult> bulkPutResult;
            bulkPutResult = bulk.putMethodTest();
            results.addAll(bulkPutResult);

            ArrayList<TestResult> bulkDeleteResult;
            bulkDeleteResult = bulk.deleteMethodTest();
            results.addAll(bulkDeleteResult);

            // Calculate test statistics.
            Statistics statistics = new Statistics();
            for (TestResult result : results) {

                switch (result.getStatus()) {
                    case TestResult.ERROR:
                        statistics.incFailed();
                        break;
                    case TestResult.SUCCESS:
                        statistics.incSuccess();
                        break;
                    case TestResult.SKIPPED:
                        statistics.incSkipped();
                        break;
                }
            }
            long time = 0;
            for (TestResult result : results) {
                time += result.getElapsedTime();
            }
            statistics.setTime(time);

            Result finalResults = new Result(statistics, results);

            assertEquals(statistics.getSuccess(), statistics.getTotal(), "CoreTests run successfully");
        } catch (Exception ee) {
            assertEquals(0, 0, "CoreTests Failed");
        }
    }

}
