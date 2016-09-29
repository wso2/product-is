/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.mb.osgi.kernel;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.mb.osgi.test.util.MBOSGiTestConfigurationUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

/**
 * Test case to check whether andes bundle started up properly.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KernelServerStartupTestCase {
    @Inject
    private BundleContext bundleContext;

    /**
     * Creating the OSGi environment with the bundle dependencies needed for andes kernel.
     */
    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = MBOSGiTestConfigurationUtils.getAndesOSGiConfigurationList();

        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");

        CarbonSysPropConfiguration sysPropConfiguration = new CarbonSysPropConfiguration();
        sysPropConfiguration.setCarbonHome(carbonHome.toString());
        sysPropConfiguration.setServerKey("carbon-mb");
        sysPropConfiguration.setServerName("WSO2 Message Broker Server");
        sysPropConfiguration.setServerVersion("3.5.0");

        optionList = OSGiTestConfigurationUtils.getConfiguration(optionList, sysPropConfiguration);

        return optionList.toArray(new Option[optionList.size()]);
    }

    /**
     * Checks if a bundle exists in the bundle context by bundle symbolic name.
     *
     * @param name The symbolic name of the bundle.
     * @return The found bundle.
     */
    private Bundle getBundle(String name) {
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(name)) {
                bundle = b;
                break;
            }
        }
        Assert.assertNotNull(bundle, "Bundle should be available. Name: " + name);
        return bundle;
    }

    /**
     * Checks if andes bundle is in active.
     */
    @Test
    public void testAndesBundle() {
        Bundle coreBundle = getBundle("andes");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }
}
