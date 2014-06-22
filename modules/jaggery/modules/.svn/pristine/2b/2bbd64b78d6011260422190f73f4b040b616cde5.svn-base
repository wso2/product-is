(function (server) {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
        MultitenantConstants = Packages.org.wso2.carbon.utils.multitenancy.MultitenantConstants,
        TenantUtils = Packages.org.wso2.carbon.utils.TenantUtils,
        context = PrivilegedCarbonContext.getCurrentContext(),
        realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
        tenantManager = realmService.getTenantManager();

    server.tenantDomain = function (options) {
        if (!options) {
            return context.getTenantDomain();
        }
        if (options.username) {
            return TenantUtils.getTenantDomain(options.username);
        }
        if (options.url) {
            return TenantUtils.getTenantDomainFromRequestURL(options.url);
        }
        return null;
    };

    server.tenantId = function (options) {
        var domain = options ? (options.domain || server.tenantDomain(options)) : server.tenantDomain();
        return domain ? tenantManager.getTenantId(domain) : null;
    };

    server.superTenant = {
        tenantId: MultitenantConstants.SUPER_TENANT_ID,
        domain: MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
    };

}(server));