{
    "welcomeFiles":["index.jag"],
    "themeStat" : ["theme1"],
    "urlMappings" : [
						{
							"url" : "/acs",
							"path" : "/acs.jag"
						},
{
            "url": "/apis/ues/*",
            "path": "/apis/ues.jag"
        },
        {
            "url": "/login",
            "path": "/login.jag"
        },
        {
            "url": "/sso",
            "path": "/sso.jag"
        },
        {
            "url": "/logout",
            "path": "/logout.jag"
        }
				

    ],
    "initScripts": ["app.js"],
    "logLevel": "info",

     "filters":[
          {
             "name":"HttpHeaderSecurityFilter",
             "class":"org.apache.catalina.filters.HttpHeaderSecurityFilter",
             "params" : [{"name" : "hstsEnabled", "value" : "false"}]
          }
       ],
       "filterMappings":[
          {
             "name":"HttpHeaderSecurityFilter",
             "url":"*"
          }
       ]
}
