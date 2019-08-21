var caramel = require('caramel');
include("util/constants.jag");
include("util/utility.jag");

var lg = new Log();

//var configs = require('/config.json');
var server = require('/modules/server.js');
//server.init(configs);

//var user = require('/modules/user.js');
//user.init(configs);



function init(){
	var path = "ui-components.json";
	var file = new File(path);
	file.open("r");
	var json = "";
	
	json = file.readAll();
	file.close();
	json = parse(json);
		
	var jsonArray = sortNumber(json.pages, "displayorder", true);
	json.pages = jsonArray;
	
	lg.debug(json);
	
	application.put(UI_COMPONENTS, json);
		
	var jagjsPath = "jaggery.conf";
	file = new File(jagjsPath);
	file.open("r");
	var jagJson = "";
	
	jagJson = file.readAll();
	file.close();
	jagJson = parse(jagJson);
	var serverUrl = jagJson['serverUrl'];
	
	if(serverUrl == null || serverUrl.length <= 0){
		lg.info("server url was not found under jaggery.conf file. Therefore connecting to the services same as the deployed carbon server");
	}else{
		lg.info("Connecting to	" + serverUrl);
		application.put("serverUrl", serverUrl);
	}
	
	caramel.configs({
	    context: json.context,
	    cache: true,
	    negotiation: true,
	    themer: function () {
	        //return 'theme1';
			return jagJson.themeStat;
	    }
	});

}


init();
