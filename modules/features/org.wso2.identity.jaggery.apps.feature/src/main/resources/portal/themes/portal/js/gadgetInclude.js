// html snipits needed for including gadgets.
var includeHeader = "<!-- imports needed for gadget rendering -->";
var includePoint = /<head[^>]*>/g;
var includeBody = '\n' + includeHeader + '\n  <% var ues = require("ues"); %> \n \n <script type="text/javascript" src="/portal/lib/gadget-rendering/js/external/head.min.js"></script> \n <script type="text/javascript" src="/portal/lib/gadget-rendering/js/main.js"></script>  \n \n <!-- styles --> \n <link rel="stylesheet" type="text/css" href="/portal/lib/gadget-rendering/css/style.css"> \n <link rel="stylesheet" type="text/css" href="/portal/lib/gadget-rendering/css/bootstrap.min.css"> \n <link rel="stylesheet" type="text/css" href="/portal/lib/gadget-rendering/css/font-awesome.css"> \n <!-- END imports needed for gadget rendering --> \n';
var includeDivId = /id=.gadgetarea\-([0-9]+)/g;
var includeDiv = function (id, url) {
    return '\n<%\n var optionsG' + id + ' = { id: "g' + id +
        '", container: "gadgetarea-' + id + '", userPrefs: {} };\n  ' +
        'ues.gadgets.addGadget("' + url + '", optionsG' + id + ');\n%>\n<div id="gadgetarea-' + id + '" class="gadgetarea">\n</div>'
};
var getIndentCount = function (section) {
    return section.match(/\n/g).length
};
