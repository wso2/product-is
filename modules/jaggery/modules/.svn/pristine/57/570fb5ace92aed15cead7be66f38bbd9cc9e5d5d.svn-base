var log = new Log();

var Registry = function (server) {
    this.server = server;
};

var Resource = function (name) {

};

var Collection = function (name) {

};

Registry.prototype.invoke = function (action, payload) {
    var options,
        ws = require('ws'),
        client = new ws.WSRequest(),
        server = this.server;

    options = {
        useSOAP: 1.2,
        useWSA: 1.0,
        action: action,
        HTTPHeaders: [
            { name: 'Cookie', value: server.cookie }
        ]
    };

    try {
        client.open(options, server.url + '/services/WSRegistryService', false);
        client.send(payload);
        return client.responseXML;
    } catch (e) {
        log.error(e.toString());
        throw new Error('Error while invoking action in WSRegistryService : ' +
            action + ', user : ' + server.user.username);
    }
};

Registry.prototype.putResource = function (path, resource) {

};

Registry.prototype.getResource = function (path) {
    var res, payload,
        base64 = require('/modules/base64.js');

    payload =
        <api:getContent xmlns:api="http://api.ws.registry.carbon.wso2.org">
            <api:path>{path}</api:path>
        </api:getContent>;

    res = this.invoke('urn:getContent', payload);
    return base64.decode(String(res.*::['return'].text()));
};