/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var appDataPersonalValue2 = 'personalValue2 ' + new Date().getTime();
var appDataPersonalValue1 = 'personalValue1 ' + new Date().getTime();
function runAppDataSuite(){
	
	module("OpenSocial JavaScript AppData Tests 1.1");
	
	asyncTest("osapi.appdata.(update, get) defaults" , function(){

		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var pairs = {"testKey1" : appDataPersonalValue1};
        ok(pairs,"Setting testKey1 to " + appDataPersonalValue1);
        var params = {data : pairs};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");

                var params = {keys : ["testKey1"]};
                var req2 = osapi.appdata.get(params);
                ok(req2 != null, "Req not null");
                req2.execute(function(response){
                		ok(!response.error,"No error in response");
                		for(var person in response){
                			ok(response[person]["testKey1"],"Response contains personalValue1");
                			ok(response[person]["testKey1"] == appDataPersonalValue1, "personalValue1 matches expected value");
                		}
                		start();
                	});
        	});
        }, 1000);
      
	});
	
	asyncTest("osapi.appdata.(update, get) @me" , function(){

		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var pairs = {"testKey2" : appDataPersonalValue2};
        ok(pairs,"Setting personalValue to " + appDataPersonalValue2);
        var params = {userId : "@me", data : pairs};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
                var params = {userId : "@me" , keys : ["testKey2"]};
                var req2 = osapi.appdata.get(params);
                ok(req2 != null, "Req not null");
                req2.execute(function(response){
                		ok(!response.error,"No error in response");
                		var i = 0;
                		for(var person in response){
                			i++;
                			ok(response[person]["testKey2"] == appDataPersonalValue2, 
                					appDataPersonalValue2 + " matches retreived value "+ response[person]["testKey2"]);
                		}
                		ok(i == 1, "Expect 1 Person in response, found " + i);
                		start();
                	});
        	});
        }, 1000);
      
	});
	
	asyncTest("osapi.appdata.get w/ wildcard", function(){
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var params = {keys : ["*"]};
        var req = osapi.appdata.get(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
//        		for(var person in response){
//TODO VALIDATE RESPONSE
//        		}
        		start();
        	});
        }, 1000);
	});
	
	//TODO fully validate response against compliance db
	asyncTest("osapi.appdata.get @viewer", function(){
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var params = {userId : "@viewer"};
        var req = osapi.appdata.get(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
        		var count = 0;
        		for(var person in response){
        			ok(person, person + " in response");
        			count ++;
        		}
        		ok(count == 1, "Expected 1 person in response");
        		start();
        	});
        }, 1000);
	});
	
	
	//TODO fully validate response against compliance db
	asyncTest("osapi.appdata.get @owner", function(){
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var params = {userId : "@owner"};
        var req = osapi.appdata.get(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
        		var count = 0;
        		for(var person in response){
        			ok(person, person + " in response");
        			count ++;
        		}
        		ok(count == 1, "Expected 1 person in response");
        		start();
        	});
        }, 1000);
	});
	
	asyncTest("osapi.appdata.get w/ DOES_NOT_EXIST property", function(){
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var params = {keys : ["DOES_NOT_EXIST"]};
        var req = osapi.appdata.get(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error, "No error in response");
        		for(var person in response){
        			ok(person, person +" in response");
        			for(var data in response[person]){
        				ok(false,person+" response should not contain data, but found \'" + data+"\'");
        			}
        		}
        		start();
        	});
        }, 1000);
	});
	
    
	asyncTest("osapi.appdata.UPDATE without data property (Expect Error)", function(){
		ok(osapi.appdata.update, "osapi.appdata.update exists");
        var params = {};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(response.error,"Expecting error in UPDATE response");
        		start();
        	});
        }, 1000);
      
	});
	
	asyncTest("osapi.appdata.DELETE without keys property (Expect Error)", function(){
		ok(osapi.appdata.delete, "osapi.appdata.delete exists");
        var params = {};
        var req = osapi.appdata.delete(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(response.error,"Expecting error in DELETE response");
        		start();
        	});
        }, 1000);
      
	});
        
	asyncTest("osapi.appdata.GET without keys property (Expect Error)", function(){
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var params = {};
        var req = osapi.appdata.get(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(response.error,"Expecting error in GET response");
        		start();
        	});
        }, 1000);
      
	});
	
	asyncTest("osapi.appdata.(update, get) @viewer  w/ key array" , function(){

		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var pairs = {"testKey2" : appDataPersonalValue2, "testKey1" : appDataPersonalValue1};
        var params = {userId : "@viewer", data : pairs};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");

                var params = {userId : "@viewer" , keys : ["testKey2", "testKey1"]};
                var req2 = osapi.appdata.get(params);
                ok(req2 != null, "Req not null");
                req2.execute(function(response){
                		ok(!response.error,"No error in response");
                		var i = 0;
                		for(var person in response){
                			i++;
                			var j = 0;
                			for(var data in response[person]){
                				j++;
                				if(data == "testKey1"){
                        			ok(response[person]["testKey1"] == appDataPersonalValue1, 
                        					"testKey1 value = " + response[person]["testKey1"] + ", expected " + appDataPersonalValue1);
                				} else if(data == "testKey2"){
                        			ok(response[person]["testKey2"] == appDataPersonalValue2, 
                        					"testKey2 value = " + response[person]["testKey2"] + ", expected " + appDataPersonalValue2);
                				} else {
                					ok(false, "Found unexpected key \'" + data + "\', value = " + response[person][data]);
                				}
                			}
                		}
                		ok(i == 1, "Expect 1 Person in response, found " + i);
                		start();
                	});
        	});
        }, 1000);
      
	});
	
	
	//Assumes that one of the @viewer's friends is jane.doe
	asyncTest("osapi.appdata.(update, get) Viewer Friends" , function(){
		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var pairs = {"testKey1" : "friendDataZZ"};
        ok(pairs,"Setting \'jane.doe\' testKey1 to \'friendDataZZ\'");
        var params = {userId : "jane.doe", groupId : "@self", data : pairs};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
                var params = {userId : "@viewer", groupId : "@friends", keys : ["testKey1"]};
                var req2 = osapi.appdata.get(params);
                ok(req2 != null, "Req not null");
                req2.execute(function(response){
                		ok(!response.error,"No error in response");
                		for(var person in response){
                			ok(person != "john.doe", person + " is not john.doe");
                			if(person == "jane.doe"){
                    			ok(response[person]["testKey1"], person + " appdata contains contains testKey1");
                    			ok(response[person]["testKey1"] == "friendDataZZ",
                    					person + " testKey1 value = " + response[person]["testKey1"] + ", expected = friendDataZZ");
                			}
                		}
                		start();
                	});
        	});
        }, 1000);
      
	});
	
	//Assumes that one of the @owner's friends is jane.doe
	asyncTest("osapi.appdata.(update, get) Owner Friends" , function(){
		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.get, "osapi.appdata.get exists");
        var pairs = {"testKey1" : "friendDataZZ"};
        ok(pairs,"Setting \'jane.doe\' testKey1 to \'friendDataZZ\'");
        var params = {userId : "jane.doe", groupId : "@self", data : pairs};
        var req = osapi.appdata.update(params);
        ok(req != null, "Req not null");
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"No error in response");
                var params = {userId : "@owner", groupId : "@friends", keys : ["testKey1"]};
                var req2 = osapi.appdata.get(params);
                ok(req2 != null, "Req not null");
                req2.execute(function(response){
                		ok(!response.error,"No error in response");
                		for(var person in response){
                			ok(person != "john.doe", person + " is not john.doe");
                			if(person == "jane.doe"){
                    			ok(response[person]["testKey1"], person + " appdata contains contains testKey1");
                    			ok(response[person]["testKey1"] == "friendDataZZ",
                    					person + " testKey1 value = " + response[person]["testKey1"] + ", expected = friendDataZZ");
                			}
                		}
                		start();
                	});
        	});
        }, 1000);
      
	});
	
	asyncTest("osapi.appdata.(update, delete) Create TO_DELETE appdata then delete it" , function(){
		ok(osapi.appdata.update, "osapi.appdata.update exists");
		ok(osapi.appdata.delete, "osapi.appdata.delete exists");
        var pairs = {"TO_DELETE" : "value"};
        ok(pairs,"Setting TO_DELETE");
        var params = {data : pairs};
        var req = osapi.appdata.update(params);
        setTimeout(function(){
        	req.execute(function(response){
        		ok(!response.error,"TO_DELETE set successfully");
                var params = {keys : ["TO_DELETE"]};
                var req2 = osapi.appdata.delete(params);
                ok(req2 != null, "Delete request not null");
                req2.execute(function(response){
            		ok(!response.error,"No error in DELETE response");
                    var params = {keys : ["*"]};
                    var req2 = osapi.appdata.get(params);
                    req2.execute(function(response){
                		ok(!response.error,"No error in GET response");
                		for(var person in response){
                			ok(!response[person]["TO_DELETE"], person + " does not have TO_DELETE appdata");
                		}
                		start();
                	});
            	});
        	});
        }, 1500);
      
	});
	
	
	//Assumes the existence of john.doe and jane.doe
	asyncTest("osapi.batch appdata requests (multi-user multi-key)", function(){
		ok(osapi.newBatch,"osapi.newBatch exists");
		var time = new Date().getTime();
		var johnvalue1 = "john.doe value1 " + time;
		var johnvalue2 = "john.doe value2 " + time;
		var janevalue1 = "jane.doe value1 " + time;
		var janevalue2 = "jane.doe value2 " + time;
        var batchUpdate = osapi.newBatch().add("john.doe", osapi.appdata.update({userId : "john.doe", 
		        	data : {
		        		"batchKey1" : johnvalue1, 
		        		"batchKey2" : johnvalue2}
        		})).add("jane.doe", osapi.appdata.update({userId: "jane.doe", 
	        		data : {
	        			"batchKey1" : janevalue1,
	        			"batchKey2" : janevalue2}}));
        		
        
        var batchGet = osapi.newBatch().add("john.doe", osapi.appdata.get({userId : "john.doe", "keys" : ["batchKey1", "batchKey2"]}))
				.add("jane.doe", osapi.appdata.get({userId: "jane.doe", "keys" : ["batchKey1", "batchKey2"]}));
        
        setTimeout(function(){
    	    batchUpdate.execute(function(result) {
    	    	ok(!result["john.doe"].error,"No error in john.doe update response");
    	    	ok(!result["jane.doe"].error,"No error in jane.doe update response");
    	    	if (!result["jane.doe"].error && !result["jane.doe"].error) {
    	    	    batchGet.execute(function(result) {
    	    	    	ok(!result["john.doe"].error,"No error in john.doe get response");
    	    	    	ok(!result["jane.doe"].error,"No error in jane.doe get response");
    	    	    	if (!result["jane.doe"].error && !result["jane.doe"].error) {
    	    	    		ok(result["john.doe"]["john.doe"]["batchKey1"] == johnvalue1,"john.doe key1 = " + johnvalue1 + " (expected "+johnvalue1+")");
    	    	    		ok(result["jane.doe"]["jane.doe"]["batchKey1"] == janevalue1,"jane.doe key1 = " + janevalue1 + " (expected "+janevalue1+")");
    	    	    		ok(result["john.doe"]["john.doe"]["batchKey2"] == johnvalue2,"john.doe key2 = " + johnvalue2 + " (expected "+johnvalue2+")");
    	    	    		ok(result["jane.doe"]["jane.doe"]["batchKey2"] == janevalue2,"jane.doe key2 = " + janevalue2 + " (expected "+janevalue2+")");
    	    	    	}
    	    	    	start();
    	    	     });
    	    	}
    	     });
        }, 1000);
		
	});
	
	asyncTest("osapi.batch mixed appdata & people requests", function(){
		var batch = osapi.newBatch().
		    add("viewer", osapi.people.getViewer()).
		    add("appdata", osapi.appdata.get({ userId : '@viewer', groupId : '@self', "keys" : ["*"]}));
		setTimeout(function(){
			batch.execute(function(result) {
				    ok(!result.viewer.error, "osapi.people.getViewer() succeeded");
				    if(result.viewer.error){
				    	ok(false, result.viewer.error.message);
				    }
				    ok(!result.appdata.error, "osapi.appdata.get() succeeded");
				    if(result.appdata.error){
				    	ok(false, result.appdata.error.message);
				    }
					ok(result.viewer.id,"Got viewer "+result.viewer.id);
					//TODO validate that appdata values match expected
					for(var person in result.appdata){
						for(var data in result.appdata[person]){
							ok(result.appdata[person][data], person + " - key: " + data + ", value: " + result.appdata[person][data]);
						}
					}
				    start();
				});
		}, 1000);


		
	});
	

	
}
