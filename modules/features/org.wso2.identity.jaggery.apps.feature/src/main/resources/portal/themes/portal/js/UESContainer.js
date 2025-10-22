var UESContainer;
(function () {

    /**
     * @return {boolean} always true.
     */
    var TRUE_FUNC = function () {
        return true;
    };

    var containerConfig = {};
    containerConfig[osapi.container.ContainerConfig.RENDER_DEBUG] = '1';

    var gadgetInfoMap = {};
    UESContainer = new osapi.container.Container(containerConfig);

    UESContainer.managedHub = new OpenAjax.hub.ManagedHub({
        onSubscribe: TRUE_FUNC,
        onUnsubscribe: TRUE_FUNC,
        onPublish: TRUE_FUNC
    });
    gadgets.pubsub2router.init({
        hub: UESContainer.managedHub
    });

    UESContainer.getGadgetInfo = function (elId) {
        return gadgetInfoMap[elId];
    };

    UESContainer.renderGadget = function (elId, url, opt, callback) {
        var params = {};
        params[osapi.container.RenderParam.WIDTH] = '100%';
        params[osapi.container.RenderParam.HEIGHT] = '100%';
        params[osapi.container.RenderParam.USER_PREFS] = opt && opt.prefs;
        params[osapi.container.RenderParam.VIEW] = opt && opt.view || 'default';
        var gadgetInfo = gadgetInfoMap[elId];
        if (!gadgetInfo) {
            gadgetInfo = {};
            gadgetInfo.site = UESContainer.newGadgetSite(document.getElementById(elId));
            gadgetInfoMap[elId] = gadgetInfo;
        }
        gadgetInfo.opt = opt || {};
        UESContainer.navigateGadget(gadgetInfo.site, url, {}, params, function (r) {
            gadgetInfo.meta = r;
            callback && callback(gadgetInfo);
        });
    };

    UESContainer.removeGadget = function (elId) {
        var gadgetInfo = gadgetInfoMap[elId];
        if (gadgetInfo) {
            UESContainer.closeGadget(gadgetInfo.site);
            delete gadgetInfoMap[elId];
        }
    };

    UESContainer.redrawGadget = function (elId, opt) {
        var gadgetInfo = gadgetInfoMap[elId];
        if (gadgetInfo && (!opt || !opt.view || gadgetInfo.meta.views[opt.view])) {
            for (var optName in opt){
                gadgetInfo.opt[optName] = opt[optName];
            }
            UESContainer.renderGadget(elId, gadgetInfo.meta.url, gadgetInfo.opt);
        }
    };

    UESContainer.maximizeGadget = function (elId) {
        UESContainer.redrawGadget(elId, {view: "home"});
    };

    UESContainer.restoreGadget = function (elId) {
        UESContainer.redrawGadget(elId, {view: "default"});
    };

    window.onload = function () {
        try {
            // Connect to the ManagedHub
            UESContainer.inlineClient = new OpenAjax.hub.InlineContainer(UESContainer.managedHub, 'container',
                {
                    Container: {
                        onSecurityAlert: function (source, alertType) { /* Handle client-side security alerts */
                        },
                        onConnect: function (container) {/* Called when client connects */
                        },
                        onDisconnect: function (container) { /* Called when client connects */
                        }
                    }
                });
            //connect to the inline client
            UESContainer.inlineClient.connect();

        } catch (e) {
            console && console.error(e.message);
        }
    };
})();



