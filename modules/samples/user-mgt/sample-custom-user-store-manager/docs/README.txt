Adding a custom user store manager to WSO2 Identity Server
==========================================================


This this a sample implementation to demonstrate the scenario of adding a new user store manager.

WSO2 Identity Server has following user store managers implemented by default.
- ReadOnlyLDAPUserStoreManager
- ReadWriteLDAPUserStoreManager
- ActiveDirectoryUserStoreManager
- JDBCUserStoreManager

When this needs to be extended, a custom user store can be written, implementing org.wso2.carbon.user.api.UserStoreManager,
as presented with this sample.
Once done, all we need to do is dropping the built jar at CARBON_HOME/repository/components/dropins.
Now we can go ahead and start the server, which will then allow us to add user stores managed by the newly added custom
user store manager.
