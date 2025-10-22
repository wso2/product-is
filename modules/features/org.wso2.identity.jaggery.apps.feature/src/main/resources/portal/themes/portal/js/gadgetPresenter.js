//GadgetID
var curId = 0;

var host;

var testGadgets = [];

$(function () {
    //initializing the common container
    CommonContainer.init();
    host = resolveHost();
    // TODO: we need to integrate the REST api get the following gadgets, in milestone 2
    testGadgets = [host + '/portal/gadgets/co2-emission/co2-emission.xml', host + '/portal/gadgets/agricultural-land/agricultural-land.xml', host + '/portal/gadgets/electric-power/electric-power.xml', host + '/portal/gadgets/energy-use/energy-use.xml', host + '/portal/gadgets/greenhouse-gas/greenhouse-gas.xml'];
    drawGadgets();
});

var drawGadgets = function () {
    CommonContainer.preloadGadgets(testGadgets, function (result) {
        for (var gadgetURL in result) {
            if (!result[gadgetURL].error) {
                buildGadget(result, gadgetURL);
                curId++;
            }
        }
    });
};

var gadgetTemplate = '<div class="portlet">' +
    '<div class="portlet-header"></div>' +
    '<div id="gadget-site" class="portlet-content"></div>' +
    '</div>';

var buildGadget = function (result, gadgetURL) {
    result = result || {};
    var element = getNewGadgetElement(result, gadgetURL);
    $(element).data('gadgetSite', CommonContainer.renderGadget(gadgetURL, curId));

    //determine which button was click and handle the appropriate event.
    $('.portlet-header .ui-icon').click(function () {
        handleNavigateAction($(this).closest('.portlet'), $(this).closest('.portlet').find('.portlet-content').data('gadgetSite'), gadgetURL, this.id);
    });
};

var getNewGadgetElement = function (result, gadgetURL) {
    result[gadgetURL] = result[gadgetURL] || {};

    var newGadgetSite = gadgetTemplate;
    newGadgetSite = newGadgetSite.replace(/(gadget-site)/g, '$1-' + curId);

    $(newGadgetSite).appendTo($('#gadgetArea-' + curId));
    
    var gadgetHeader = '<div class="gadget-header"> \
								<a href="#"><i class="icon-cog icon-large"></i></a> \
						</div>';
	$('#gadgetArea-' + curId).parents('li').prepend(gadgetHeader);
	
    return $('#gadget-site-' + curId).get([0]);
}

var resolveHost = function () {
    //http://<domain>:<port>/
    return document.location.protocol + "//" + document.location.host;

}



