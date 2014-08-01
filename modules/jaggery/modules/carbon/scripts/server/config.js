(function (server) {
    var process = require('process'),
        configDir = 'file:///' + process.getProperty('carbon.config.dir.path').replace(/[\\]/g, '/').replace(/^[\/]/g, '') + '/';
    server.loadConfig = function (path) {
        var content,
            index = path.lastIndexOf('.'),
            ext = (index !== -1 && index < path.length) ? path.substring(index + 1) : '',
            file = new File(configDir + path);
        if (!file.isExists()) {
            throw new Error('Specified config file does not exists : ' + path);
        }
        if (file.isDirectory()) {
            throw new Error('Specified config file is a directory : ' + path);
        }
        file.open('r');
        content = file.readAll();
        file.close();
        switch (ext) {
            case 'xml' :
                return new XML(content);
            case 'json' :
                return parse(content);
            case 'properties' :
            default :
                return content;

        }
    };

    server.home = function() {
        return 'file:///' + require('process').getProperty('carbon.home').replace(/[\\]/g, '/').replace(/^[\/]/g, '');
    };

}(server));