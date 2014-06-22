/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Default container configuration. To change the configuration, you have two options:
//
// A. If you run the Java server: Create your own "myContainer.js" file and
// modify the value in web.xml.
//
//  B. If you run the PHP server: Create a myContainer.js, copy the contents of container.js to it,
//  change
//		{"gadgets.container" : ["default"],
//  to
//		{"gadgets.container" : ["myContainer"],
// And make your changes that you need to myContainer.js.
// Just make sure on the iframe URL you specify &container=myContainer
// for it to use that config.
//
// All configurations will automatically inherit values from this
// config, so you only need to provide configuration for items
// that you require explicit special casing for.
//
// Please namespace your attributes using the same conventions
// as you would for javascript objects, e.g. gadgets.features
// rather than "features".

// NOTE: Please _don't_ leave trailing commas because the php json parser
// errors out on this.

// Container must be an array; this allows multiple containers
// to share configuration.

// Note that you can embed values directly or you can choose to have values read from a file on disk
// or read from the classpath ("foo-key" : "file:///foo-file.txt" || "foo-key" : "res://foo-file.txt")
// TODO: Move out accel container config into a separate accel.js file.
{"gadgets.container" : ["default", "accel"],

// Set of regular expressions to validate the parent parameter. This is
// necessary to support situations where you want a single container to support
// multiple possible host names (such as for localized domains, such as
// <language>.example.org. If left as null, the parent parameter will be
// ignored; otherwise, any requests that do not include a parent
// value matching this set will return a 404 error.
"gadgets.parent" : null,

// Origins for CORS requests and/or Referer validation
// Indicate a set of origins or an entry with * to indicate that all origins are allowed
"gadgets.parentOrigins" : ["*"],

// Various urls generated throughout the code base.
// iframeBaseUri will automatically have the host inserted
// if locked domain is enabled and the implementation supports it.
// query parameters will be added.
"gadgets.iframeBaseUri" : "${CONTEXT_ROOT}/gadgets/ifr",
"gadgets.uri.iframe.basePath" : "${CONTEXT_ROOT}/gadgets/ifr",

// Callback URL.  Scheme relative URL for easy switch between https/http.
"gadgets.uri.oauth.callbackTemplate" : "//%host%${CONTEXT_ROOT}/gadgets/oauthcallback",

// Config param to load Opensocial data for social
// preloads in data pipelining.  %host% will be
// substituted with the current host.
"gadgets.osDataUri" : "//%host%${CONTEXT_ROOT}/rpc",

// Use an insecure security token by default
"gadgets.securityTokenType" : "insecure",

// Uncomment the securityTokenType and one of the securityTokenKey's to switch to a secure version.
// Note that you can choose to use an embedded key, a filesystem reference or a classpath reference.
// The best way to generate a key is to do something like this:
// dd if=/dev/random bs=32 count=1 | openssl base64
//
//"gadgets.securityTokenType" : "secure",
//"gadgets.securityTokenKey" : "default-insecure-embedded-key",
//"gadgets.securityTokenKey" : "file:///path/to/key/file.txt",
//"gadgets.securityTokenKey" : "res://some-file-on-the-classpath.txt",

// OS 2.0 Gadget DOCTYPE: used in Gadgets with @specificationVersion 2.0 or greater and
// quirksmode on Gadget has not been set.
"gadgets.doctype_qname" : "HTML",  //HTML5 doctype
"gadgets.doctype_pubid" : "",
"gadgets.doctype_sysid" : "",

// In a locked domain config, these can remain as-is in order to have requests encountered use the
// host they came in on (locked host).
"default.domain.locked.client" : "%host%",
"default.domain.locked.server" : "%authority%",

// IMPORTANT: EDITME: In a locked domain configuration, these should be changed to explicit values of
// your unlocked host. You should not use %host% or %authority% replacements or these defaults in a
// locked domain deployment.
// Both of these values will likely be identical in a real locked domain deployment.
"default.domain.unlocked.client" : "${Cur['default.domain.locked.client']}",
"default.domain.unlocked.server" : "${Cur['default.domain.locked.server']}",

// You can change this if you wish unlocked gadgets to render on a different domain from the default.
"gadgets.uri.iframe.unlockedDomain" : "${Cur['default.domain.unlocked.server']}", // DNS domain on which *unlocked* gadgets should render.

// IMPORTANT: EDITME: In a locked domain configuration, this suffix should be provided explicitly.
// It is recommended that it be a separate top-level-domain (TLD) than the unlocked TLD.
// You should not use replacement here (avoid %authority%)
// Example: unlockedDomain="shindig.example.com" lockedDomainSuffix="-locked.example-gadgets.com"
"gadgets.uri.iframe.lockedDomainSuffix" : "${Cur['default.domain.locked.server']}", // DNS domain on which *locked* gadgets should render.

// Should all gadgets be forced on to a locked domain?
"gadgets.uri.iframe.lockedDomainRequired" : false,

// Default Js Uri config: also must be overridden.
// gadgets.uri.js.host should be protocol relative.
"gadgets.uri.js.host" : "//${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.

// If you change the js.path you will need to define window.__CONTAINER_SCRIPT_ID prior to loading the <script>
// tag for container JavaScript into the DOM.
"gadgets.uri.js.path" : "${CONTEXT_ROOT}/gadgets/js",

// Default concat Uri config; used for testing.
"gadgets.uri.concat.host" : "${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.
"gadgets.uri.concat.path" : "${CONTEXT_ROOT}/gadgets/concat",
"gadgets.uri.concat.js.splitToken" : "false",

// Default proxy Uri config; used for testing.
"gadgets.uri.proxy.host" : "${Cur['default.domain.unlocked.server']}", // Use unlocked host for better caching.
"gadgets.uri.proxy.path" : "${CONTEXT_ROOT}/gadgets/proxy",

// Enables/Disables feature administration
"gadgets.admin.enableFeatureAdministration" : "false",

// Enables whitelist checks
"gadgets.admin.enableGadgetWhitelist" : "false",

// Max post size for posts through the makeRequest proxy.
"gadgets.jsonProxyUrl.maxPostSize" : 5242880, // 5 MiB

// This config data will be passed down to javascript. Please
// configure your object using the feature name rather than
// the javascript name.

// Only configuration for required features will be used.
// See individual feature.xml files for configuration details.
"gadgets.features" : {
  "core.io" : {
    // Note: ${Cur['gadgets.uri.proxy.path']} is an open proxy. Be careful how you expose this!
    // Note: These urls should be protocol relative (start with //)
    "proxyUrl" : "//${Cur['default.domain.unlocked.client']}${Cur['gadgets.uri.proxy.path']}%filename%?container=%container%&refresh=%refresh%&url=%url%%authz%%rewriteMime%",
    "jsonProxyUrl" : "//${Cur['default.domain.locked.client']}${CONTEXT_ROOT}/gadgets/makeRequest",
    // Note: this setting MUST be supplied in every container config object, as there is no default if it is not supplied.
    "unparseableCruft" : "throw 1; < don't be evil' >",

    // This variable is needed during the config init to parse config augmentation
    "jsPath" : "${Cur['gadgets.uri.js.path']}",

    // interval in milliseconds used to poll xhr request for the readyState
    "xhrPollIntervalMs" : 50
  },
  "views" : {
    "profile" : {
      "isOnlyVisible" : false,
      "urlTemplate" : "http://localhost${CONTEXT_ROOT}/gadgets/profile?{var}",
      "aliases": ["DASHBOARD", "default"]
    },
    "canvas" : {
      "isOnlyVisible" : true,
      "urlTemplate" : "http://localhost${CONTEXT_ROOT}/gadgets/canvas?{var}",
      "aliases" : ["FULL_PAGE"]
    },
    "default" : {
      "isOnlyVisible" : false,
      "urlTemplate" : "http://localhost${CONTEXT_ROOT}/gadgets/default?{var}",
      "aliases" : ["home", "profile", "canvas"]
    }
  },
  "tabs": {
    "css" : [
      ".tablib_table {",
      "width: 100%;",
      "border-collapse: separate;",
      "border-spacing: 0px;",
      "empty-cells: show;",
      "font-size: 11px;",
      "text-align: center;",
    "}",
    ".tablib_emptyTab {",
      "border-bottom: 1px solid #676767;",
      "padding: 0px 1px;",
    "}",
    ".tablib_spacerTab {",
      "border-bottom: 1px solid #676767;",
      "padding: 0px 1px;",
      "width: 1px;",
    "}",
    ".tablib_selected {",
      "padding: 2px;",
      "background-color: #ffffff;",
      "border: 1px solid #676767;",
      "border-bottom-width: 0px;",
      "color: #3366cc;",
      "font-weight: bold;",
      "width: 80px;",
      "cursor: default;",
    "}",
    ".tablib_unselected {",
      "padding: 2px;",
      "background-color: #dddddd;",
      "border: 1px solid #aaaaaa;",
      "border-bottom-color: #676767;",
      "color: #000000;",
      "width: 80px;",
      "cursor: pointer;",
    "}",
    ".tablib_navContainer {",
      "width: 10px;",
      "vertical-align: middle;",
    "}",
    ".tablib_navContainer a:link, ",
    ".tablib_navContainer a:visited, ",
    ".tablib_navContainer a:hover {",
      "color: #3366aa;",
      "text-decoration: none;",
    "}"
    ]
  },
  "minimessage": {
    "css": [
      ".mmlib_table {",
      "width: 100%;",
      "font: bold 9px arial,sans-serif;",
      "background-color: #fff4c2;",
      "border-collapse: separate;",
      "border-spacing: 0px;",
      "padding: 1px 0px;",
      "}",
      ".mmlib_xlink {",
        "font: normal 1.1em arial,sans-serif;",
        "font-weight: bold;",
        "color: #0000cc;",
        "cursor: pointer;",
      "}"
    ]
  },
  "rpc" : {
    // Path to the relay file. Automatically appended to the parent
    // parameter if it passes input validation and is not null.
    // This should never be on the same host in a production environment!
    // Only use this for TESTING!
    "parentRelayUrl" : "/container/rpc_relay.html",

    // If true, this will use the legacy ifpc wire format when making rpc
    // requests.
    "useLegacyProtocol" : false,

    // Path to the cross-domain enabling SWF for rpc's Flash transport.
    "commSwf": "/xpc.swf",
    "passReferrer": "c2p:query"
  },
  // Skin defaults
  "skins" : {
    "properties" : {
      "BG_COLOR": "",
      "BG_IMAGE": "",
      "BG_POSITION": "",
      "BG_REPEAT": "",
      "FONT_COLOR": "",
      "ANCHOR_COLOR": ""
    }
  },
  "opensocial" : {
    // Path to fetch opensocial data from
    // Must be on the same domain as the gadget rendering server
    "path" : "http://%host%${CONTEXT_ROOT}/rpc",
    // Path to issue invalidate calls
    "invalidatePath" : "http://%host%${CONTEXT_ROOT}/rpc",
    "domain" : "shindig",
    "enableCaja" : false,
    "supportedFields" : {
       "person" : ["id", {"name" : ["familyName", "givenName", "unstructured"]}, "thumbnailUrl", "profileUrl"],
       "group" : ["id", "title", "description"],
       "activity" : ["appId", "body", "bodyId", "externalId", "id", "mediaItems", "postedTime", "priority",
                     "streamFaviconUrl", "streamSourceUrl", "streamTitle", "streamUrl", "templateParams", "title",
                     "url", "userId"],
       "activityEntry" : ["actor", "content", "generator", "icon", "id", "object", "published", "provider", "target",
                          "title", "updated", "url", "verb", "openSocial", "extensions"],
       "album" : ["id", "thumbnailUrl", "title", "description", "location", "ownerId"],
       "mediaItem" : ["album_id", "created", "description", "duration", "file_size", "id", "language", "last_updated",
                      "location", "mime_type", "num_comments", "num_views", "num_votes", "rating", "start_time",
                      "tagged_people", "tags", "thumbnail_url", "title", "type", "url"]
    }
  },
  "osapi.services" : {
    // Specifying a binding to "container.listMethods" instructs osapi to dynamicaly introspect the services
    // provided by the container and delay the gadget onLoad handler until that introspection is
    // complete.
    // Alternatively a container can directly configure services here rather than having them
    // introspected. Simply list out the available servies and omit "container.listMethods" to
    // avoid the initialization delay caused by gadgets.rpc
    // E.g. "gadgets.rpc" : ["activities.requestCreate", "messages.requestSend", "requestShareApp", "requestPermission"]
    "gadgets.rpc" : ["container.listMethods"]
  },
  "osapi" : {
    // The endpoints to query for available JSONRPC/REST services
    "endPoints" : [ "//%host%${CONTEXT_ROOT}/rpc" ]
  },
  "osml": {
    // OSML library resource.  Can be set to null or the empty string to disable OSML
    // for a container.
    "library": "config/OSML_library.xml"
  },
  "shindig-container": {
    "serverBase": "${CONTEXT_ROOT}/gadgets/"
  },
  "container" : {
    "relayPath": "${CONTEXT_ROOT}/gadgets/files/container/rpc_relay.html",

    //Enables/Disables the RPC arbitrator functionality in the common container
    "enableRpcArbitration": false,

    // This variable is needed during the container feature init.
    "jsPath" : "${Cur['gadgets.uri.js.path']}"
  }
}}
