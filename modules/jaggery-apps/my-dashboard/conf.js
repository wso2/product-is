var config;
(function () {
    config = function () {
        var log = new Log(),
            pinch = require('/modules/pinch.min.js').pinch,
            config = require('/conf.json'),
            process = require('process'),
            localIP = process.getProperty('server.host'),
            httpPort = process.getProperty('http.port'),
            httpsPort = process.getProperty('https.port');
        var carbonLocalIP = process.getProperty('carbon.local.ip');

        pinch(config, /^/, function (path, key, value) {
            if ((typeof value === 'string') && value.indexOf('%https.host%') > -1) {
                return value.replace('%https.host%', 'https://' + localIP + ':' + httpsPort);
            } else if ((typeof value === 'string') && value.indexOf('%http.host%') > -1) {
                return value.replace('%http.host%', 'http://' + localIP + ':' + httpPort);
            } else if ((typeof value === 'string') && value.indexOf('%https.carbon.local.ip%') > -1) {
                return value.replace('%https.carbon.local.ip%', 'https://' + carbonLocalIP + ':' + httpsPort);
            }
            else if ((typeof value === 'string') && value.indexOf('%http.carbon.local.ip%') > -1) {
                return value.replace('%http.carbon.local.ip%', 'http://' + carbonLocalIP + ':' + httpPort);
            }
            return  value;
        });
        return config;
    };
})();