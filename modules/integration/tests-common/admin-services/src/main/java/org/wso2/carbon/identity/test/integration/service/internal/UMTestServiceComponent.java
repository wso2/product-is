package org.wso2.carbon.identity.test.integration.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(
        name = "um.test.service.component",
        immediate = true)
public class UMTestServiceComponent {

    private static final Log log = LogFactory.getLog(UMTestServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {
        log.info("Test UUID User store manger activated.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.info("Test UUID User store manger deactivated.");;
    }
}
