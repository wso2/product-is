package org.wso2.carbon.identity.test.integration.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "um.test.service.component",
        immediate = true)
public class UMTestServiceComponent {

    private static final Log log = LogFactory.getLog(UMTestServiceComponent.class);

    private RegistryService registryService;
    private RealmService realmService;

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        log.info("Test UUID User store manger activated.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.info("Test UUID User store manger deactivated.");;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        this.registryService = null;
    }
}
