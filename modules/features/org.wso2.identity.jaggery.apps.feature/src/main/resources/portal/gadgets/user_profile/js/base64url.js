(function(root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['base64js'], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory(require('base64js'));
    } else {
        root.base64url = factory(root.base64js);
    }
})(this, function(base64js) {

    function ensureUint8Array(arg) {
        if (arg instanceof ArrayBuffer) {
            return new Uint8Array(arg);
        } else {
            return arg;
        }
    }

    function base64UrlToMime(code) {
        return code.replace(/-/g, '+').replace(/_/g, '/') + '===='.substring(0, (4 - (code.length % 4)) % 4);
    }

    function mimeBase64ToUrl(code) {
        return code.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    function fromByteArray(bytes) {
        return mimeBase64ToUrl(base64js.fromByteArray(ensureUint8Array(bytes)));
    }

    function toByteArray(code) {
        return base64js.toByteArray(base64UrlToMime(code));
    }

    return {
        fromByteArray: fromByteArray,
        toByteArray: toByteArray,
    };

});
