var server = {};

(function (server) {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
        context = PrivilegedCarbonContext.getCurrentContext(),
        Class = java.lang.Class;

    server.osgiService = function (clazz) {
        return context.getOSGiService(Class.forName(clazz));
    };
}(server));