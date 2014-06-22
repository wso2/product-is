var Gadget = function() {
        var log = new Log('Gadget.module.jaggery');
    
        this.url;
        this.id;
        this.applyDefaultStyles = false;
        this.title;
        this.prefs = {};
        this.container;
        this.owner = 'john.doe';
        this.viewer = 'john.doe';
        this.appId = 0;
        this.view;
        this.country;
        this.language;
        this.specVersion = 2.0;
    
        this.shindigBaseUrl = 'http://localhost:9763/gadgets/ifr';
    
        this.getHTML = function() {
            var iframeUrl;
            if (this.url) {
                iframeUrl = this.shindigBaseUrl + '?url=' + encodeURIComponent(this.url);
            } else {
                log.error('Gadget URL not spesified');
                return;
            }
    
            if (this.container) {
                iframeUrl += '&container=' + this.container;
            }
    
            if (this.country) {
                iframeUrl += '&country=' + this.country;
            }
    
            if (this.language) {
                iframeUrl += '&lang=' + this.language();
            }
    
            if (this.view) {
                iframeUrl += '&view=' + this.view();
            }
    
            if (this.specVersion) {
                iframeUrl += '&v=' + this.specVersion;
            }
    
            if (this.id) {
                iframeUrl += '&mid=' + this.id;
            }
    
            if (this.prefs) {
                var uprefs = this.prefs;
                for (var key in uprefs) {
                    if (uprefs.hasOwnProperty(key)) {
                        iframeUrl += '&up_' + key + '=' + uprefs[key];
                    }
                }
            }
    
            //adding a security token
            iframeUrl += '&st=' + encodeURIComponent(generateSecureToken(this));
    
            return('<iframe src="' + iframeUrl + '" height="100%" width="auto" scrolling="no" frameborder="0"></iframe>');
    
        };

	this.toHTML = function() {
	    print(this.getHTML());
	};
    
        var generateSecureToken = function (that) {
            for (var i = 0; i < that.url.length; i++) {
                that.appId += that.url.charCodeAt(i);
            }
            var fields = [that.owner, that.viewer, that.appId, 'shindig', that.url, '0', 'default'];
            for (var i = 0; i < fields.length; i++) {
                // escape each field individually, for metachars in URL
                fields[i] = encodeURIComponent(fields[i]);
            }
            return fields.join(':');
        };
};
