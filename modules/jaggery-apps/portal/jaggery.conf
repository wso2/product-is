{
    "listeners" : [
                {
                     "class" : "org.owasp.csrfguard.CsrfGuardServletContextListener"
                },
                {
                     "class" : "org.owasp.csrfguard.CsrfGuardHttpSessionListener"
                }
    ],
    "filters" :[
        {
            "name":"HttpHeaderSecurityFilter",
            "class":"org.apache.catalina.filters.HttpHeaderSecurityFilter",
            "params" : [{"name" : "hstsEnabled", "value" : "false"}]
        },
        {
             "name" : "CSRFGuard",
             "class" : "org.owasp.csrfguard.CsrfGuardFilter"
        }
    ],
    "filterMappings" :[
        {
            "name":"HttpHeaderSecurityFilter",
            "url":"*"
        },
        {
             "name" : "CSRFGuard",
             "url" : "/*"
        }
    ],
    "servlets" : [
         {
              "name" : "JavaScriptServlet",
              "class" : "org.owasp.csrfguard.servlet.JavaScriptServlet"
         }
    ],

    "servletMappings" : [
         {
              "name" : "JavaScriptServlet",
              "url" : "/csrf.js"
         }
    ],
    "contextParams" : [
         {
              "name" : "Owasp.CsrfGuard.Config",
              "value" : "/repository/conf/security/Owasp.CsrfGuard.dashboard.properties"
         }
    ]
}
