
var opIFrame = document.getElementById('opIFrame').contentWindow;

/**
 * periodically invoking the Endpoint at OP for every six seconds
 */
setInterval(function () {
    console.log('Sending polling Request From RP Client:' + mes + ',' + sessionState);
    document.all.opIFrame.src = targetOrigin;
    opIFrame.postMessage(mes, targetOrigin);
},3000);

+window.addEventListener("message", receiveMessage, false);

/**
 * Fires when a message arrives to the RP
 */
function receiveMessage(event) {
    console.log('Receiving the Session Status From OP: ' + event.data);
    if (targetOrigin.indexOf(event.origin)>-1) {
        if (event.data == "changed") {
           //Should invoke authorize endpoint with prompt=none to verify logout
            //sample:https://localhost:9443/oauth2/authorize?prompt=none&client_id=qy6Q3U4bLhkOf9fnUvk29CWHZE0a&redirect" +
            //"_uri=http://localhost:8080/playground2/oauth2client&scope=openid&response_type=code
            console.log("The state has been changed....");

        }
        else if (event.data == "unchanged") {

            console.log("The state not has been changed....");
        }
        else {

        }
    }
    else {
        return;
    }

}
