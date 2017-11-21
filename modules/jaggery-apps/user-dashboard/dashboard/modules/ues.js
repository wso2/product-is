// *** declare public functions. ***

var getDashboardLayout, setDashboardLayout;

(function() {

	// *** private const ***
	var USER_REGISTRY_KEY = 'server.user.registry';
	var CARBON = require('carbon');

	// *** private functions ***

	var getRegistry = function(userName) {
		var cachedReg = session.get(USER_REGISTRY_KEY);
		if (!cachedReg) {
			//print('new');
			var server = new CARBON.server.Server(null);
			var newReg = new CARBON.registry.Registry(server, {
				username : userName,
				tenantId : CARBON.server.tenantId()
			});
			session.put(USER_REGISTRY_KEY, newReg);
			return newReg;
		} else {
			return cachedReg;
		}
	};

	var registryPath = function(dashboardName, userName) {
		return '/_system/governance/users/' + userName + '/' + dashboardName + '/layout.json';
	};

	// *** public functions ***

	getDashboardLayout = function(dashboardName, userName) {

		var registry = getRegistry(userName);

		var dashboardName = registry.get(registryPath(dashboardName, userName));
		if(dashboardName){
			return dashboardName.content;
		} 
		return false;

	};

	setDashboardLayout = function(dashboardName, userName, layout) {
		var registry = getRegistry(userName);
		
		new Log().debug(registry);

		registry.put(registryPath(dashboardName, userName), {
			content : layout,
			mediaType : 'application/json'
		});
		return true;
	};

})();
