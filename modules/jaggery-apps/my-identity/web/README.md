#wso2-samples-store
==================

This is an example Jaggery application to demonstrate the features of caramel framework.

##Installing

* Copy `wso2-samples-store` webapp into a Jaggery server which has caramel module installed.
* You can straightaway try this on [WSO2 UES 1.0.0](http://wso2.com/products/user-engagement-server).
* If you change the webapp context, you need to update the value of `context` object key in`wso2-samples-store/app.js`
to match you context.

##Documentation

* This sample contains 2 different themes for the same webapp. All themes resides on `themes` directory and you can add
any number of themes as you want.
* Theme for a particular HTTP request is determined by evaluating the theme callback that you specified in `caramel`
configuration.
* `caramel` configuration is set when the app get deployed using the init script `app.js`.
* According to the logic in this particular caramel configuration, theme can be switched by specifying `theme` url
parameter with desired theme name. Then, selected theme will be stored in user session and will be used for subsequent
requests.
* `theme0` is a very basic theme which use only the core apis of `caramel`.
* `theme1` is a theme based on the `handlebars` engine which is an extension on top of `caramel` core.
* A theme can have their own theme engine and each theme has complete control over the data been rendered for each
request.
* In order to switch to `theme0`, use [http://localhost:9763/wso2-samples-store/?theme=theme0](http://localhost:9763/wso2-samples-store/?theme=theme0)
* In order to switch to `theme1`, use [http://localhost:9763/wso2-samples-store/?theme=theme1](http://localhost:9763/wso2-samples-store/?theme=theme1)
* Once you switched to a theme, you can void `theme` parameter.
* Please refer architecture diagrams in `docs` directory along with the app source to learn more.
* There are several more features of caramel like i18n, client side rendering etc. which will be added to this sample
in the future.

