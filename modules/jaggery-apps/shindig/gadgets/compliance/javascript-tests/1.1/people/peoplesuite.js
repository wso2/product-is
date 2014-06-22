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

//TODO Verify person objects against compliance person data
function personResponse(dataResponse) {
	  
    ok(!dataResponse.error, "No error in response");
    if (!dataResponse.error) {
      var viewerData = dataResponse;
      for (var field in opensocial.Person.Field) {
        try {
          var fieldValue = viewerData[opensocial.Person.Field[field]];
	      	var req = requiredBySpec(opensocial.Person.Field[field]);
	    	var msg;
	    	if(req){
	    		msg = "REQUIRED: ";
	    	} else {
	    		msg = "OPTIONAL: ";
	    	}
           if(fieldValue != null) {
              ok(fieldValue != null, msg + (opensocial.Person.Field[field] + " is set."));
            } else if (opensocial.getEnvironment().supportsField(opensocial.Environment.ObjectType.PERSON, opensocial.Person.Field[field])) {
          	  	ok(!req, msg + (opensocial.Person.Field[field] +" is empty."));
            } else {
          	  ok(fieldValue == null, 'Container non-supported field - ' + opensocial.Person.Field[field]);
            }
        } catch (ex) {
          ok(false,ex);
        }
      }
    }
  start();
}

/**
 * Returns TRUE if field is a required Person field
 * 
 * http://opensocial-resources.googlecode.com/svn/spec/1.1/Social-Data.xml#Person
 * 
 * @param field Person Field 
 * @returns {Boolean}  TRUE if required, FALSE if not required (see spec).
 */
function requiredBySpec(field){
	if(field == "id" ||
		field == "name" ||
		field == "thumbnailUrl"){
		return true;
	}
	return false;
}

function runPeopleSuite(){
	module("OpenSocial JavaScript People/Person Tests 1.1");
	
	asyncTest("osapi.people.getViewer() - (no parameters)", function(){
          var req = osapi.people.getViewer();
		  ok(osapi.people.getViewer,"osapi.people.getViewer exists");
          ok(req != null, "Req not null");
          ok(req.execute != null, "Req has execute method");
          setTimeout(function(){ 
        	  req.execute(personResponse);
    	  }, 1000);
        
      });
	
	asyncTest("osapi.people.getOwner() - (no parameters)", function(){
        var req = osapi.people.getOwner();
        ok(osapi.people.getOwner,"osapi.people.getOwner exists");
        ok(req != null, "Req not null");
        ok(req.execute != null, "Req has execute method");
        setTimeout(function(){ 
      	  req.execute(personResponse);
  	  	}, 1000);
      
    });
	
	asyncTest("osapi.people.get() - VIEWER (with params)", function(){

	  var params = { userId : "@me", groupId : "@self"};
      var req = osapi.people.getViewer(params);
      ok(req != null, "osapi request not null");
      ok(req.execute != null, "Request has execute method");
      setTimeout(function(){ 
    	  req.execute(personResponse);
	  	}, 1000);

	});
	
	asyncTest("osapi.people.get() - OWNER (with params)", function(){

	      var params = { userId : "@me", groupId : "@self"};
	      var req = osapi.people.getOwner(params);
	      ok(req != null, "osapi request not null");
	      ok(req.execute != null, "Request has execute method");
	      setTimeout(function(){ 
	    	  req.execute(personResponse);
		  	}, 1000);

		});
	
	asyncTest("osapi.people.get() - (by id 'john.doe')", function(){

		var params = {userId : "john.doe", groupId : "@self"};
        var req = osapi.people.get(params);
        setTimeout(function(){
        	
        	req.execute(function(dataResponse){

            	ok(!dataResponse.error,"no error in data response");
                if (!dataResponse.error) {
                  ok(dataResponse.id == "john.doe","ID is john.doe");
                  ok(dataResponse.name.givenName == "John", "given name is John");
                  ok(dataResponse.name.familyName == "Doe", "family name is Doe");
                }
                start();
              });
        	
      }, 1000);
	});
	
	test("opensocial.hasPermission(VIEWER)", function(){
        var hasViewerPermission;
        try {
          ok(opensocial.hasPermission,"hasPermission exists");
          ok(opensocial.Permission.VIEWER,"opensocial.Permission.VIEWER exists");
          hasViewerPermission = opensocial.hasPermission(opensocial.Permission.VIEWER);
          ok(hasViewerPermission != null,"User permission not null.")
          ok(!hasViewerPermission,"Gadget should not have Viewer permission");
        } catch (ex) {
        	ok(false,ex);
        }
	});
	
	asyncTest("opensocial.requestPermission(VIEWER)", function(){
		expect(2);
		ok(opensocial.requestPermission,"opensocial.requestPermission exists");
        opensocial.requestPermission(opensocial.Permission.VIEWER, 'test',
                function(dataResponse) {
	        		ok(dataResponse instanceof opensocial.ResponseItem,"dataResponse is a opensocial.ResponseItem");
        		});
		setTimeout(function(){
	    	        start();
		},1000);
	});
	
	
	asyncTest("opensocial.requestShareApp(VIEWER/VIEWER_FRIENDS/OWNER/OWNER_FRIENDS)", function(){

		var ids = [ 'VIEWER', 'OWNER'];
		expect(2 + (ids.length*2));
		ok(opensocial.requestShareApp,"opensocial.requestShareApp exists");
		ok(opensocial.newMessage,"opensocial.newMessage exists");
		
        for (var i = 0; i < ids.length; i++) {
          var idSpec = {userId : ids[i], 
          			  groupId : '@friends', 
          			  networkDistance : 1};
          opensocial.requestShareApp(idSpec, opensocial.newMessage("test"), function(dataResponse) {
        	  /* Does this have to be called? */
        	  ok(dataResponse instanceof opensocial.ResponseItem, ids[i]+" response is a opensocial.ResponseItem");
          });
        }
        
        for (i = 0; i < ids.length; i++) {
            var idSpec = {userId : ids[i], 
            			  groupId : '@self', 
            			  networkDistance : 1};
            opensocial.requestShareApp(idSpec, opensocial.newMessage("test"), function(dataResponse) {
          	  /* Does this have to be called? */
          	  ok(dataResponse instanceof opensocial.ResponseItem, ids[i]+" response is a opensocial.ResponseItem");
            });
        }
        
		setTimeout(function(){
			start();
	    },3000);
		
	});
	
	asyncTest("osapi.people.getViewer() (profile_details: addresses)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.ADDRESSES);
      
	});
	
	
	asyncTest("osapi.people.getViewer() (profile_details: urls)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.URLS);
      
	});
	
	asyncTest("osapi.people.getViewer() (profile_details: name)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.NAME);
      
	});
	
	asyncTest("osapi.people.getViewer() (profile_details: currentLocation)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.CURRENT_LOCATION);
      
	});

	asyncTest("osapi.people.getViewer() (profile_details: gender)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.GENDER);
      
	});
	
	asyncTest("osapi.people.getViewer() (profile_details: bodyType)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.BODY_TYPE);
      
	});
	
	asyncTest("osapi.people.getViewer() (profile_details: schools)", function(){

		testAsyncViewerFieldResult(opensocial.Person.Field.SCHOOLS);
      
	});
	
	asyncTest("osapi.people.getOwner() (profile_details: addresses)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.ADDRESSES);
      
	});
	
	
	asyncTest("osapi.people.getOwner() (profile_details: urls)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.URLS);
      
	});
	
	asyncTest("osapi.people.getOwner() (profile_details: name)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.NAME);
      
	});
	
	asyncTest("osapi.people.getOwner() (profile_details: currentLocation)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.CURRENT_LOCATION);
      
	});

	asyncTest("osapi.people.getOwner() (profile_details: gender)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.GENDER);
      
	});
	
	asyncTest("osapi.people.getOwner() (profile_details: bodyType)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.BODY_TYPE);
      
	});
	
	asyncTest("osapi.people.getOwner() (profile_details: schools)", function(){

		testAsyncOwnerFieldResult(opensocial.Person.Field.SCHOOLS);
      
	});
	
	asyncTest("osapi.people.get() - String ID",function(){
		
        var req = osapi.people.getViewer();
        var id;
        setTimeout(function(){ 
        	req.execute(function(dataResponse){ 
        		id = dataResponse['id']
        		ok(id != null && id != undefined, "Viewer id is " + id);

	      		var params = { userId : id, groupId : "@self"};
	      		var req = osapi.people.get(params);

	                req.execute(function(dataResponse) {
	                  ok(!dataResponse.error,"No error in data response");
	                  if (!dataResponse.error) {
	                    var actual = dataResponse['id'];
	                    ok(actual == id, "Expected " + id + " got " + actual);
	                  }
	                  start();
	                });

        	});
	  	}, 1000);
        
	});
	
	asyncTest("osapi.people.get() w/bad parameters - Error expected", function(){


        var req = osapi.people.get("bad_param");

        setTimeout(function(){
        	
        	req.execute(function(dataResponse){
                ok(dataResponse.error,"Error in data response");
        		start();
        	});
        	
        	
          }, 1000);

      
    });
	
	asyncTest("osapi.people.get() - viewer\'s friends  (default) ", function(){
        
        var params = {userId : "@viewer", groupId : "@friends", networkDistance : 1};
        var req = osapi.people.get(params);
        testAsyncPeopleCollection(req,getSupportedPersonFields());
      
    });
	
	asyncTest("osapi.people.get() - john.doe\'s friends  (default) ", function(){
        
        var params = {userId : "john.doe", groupId : "@friends", networkDistance : 1};
        var req = osapi.people.get(params);
        testAsyncPeopleCollection(req,getSupportedPersonFields());
      
    });
	
	asyncTest("osapi.people.getViewerFriends() - (default) ", function(){

        testAsyncPeopleCollection(osapi.people.getViewerFriends(),getSupportedPersonFields());
      
    });
	
	asyncTest("osapi.people.getOwnerFriends() - (default) ", function(){
		var supportedPersonFields = getSupportedPersonFields();
        testAsyncPeopleCollection(osapi.people.getOwnerFriends(),supportedPersonFields);
        
	});
	
	asyncTest("osapi.people.getViewerFriends() - w/fields ", function(){
		var fieldIds = ['id','thumbnailUrl'];
        var params = {fields : fieldIds};
        var req = osapi.people.getViewerFriends(params);
        testAsyncPeopleCollection(req, fieldIds);
        
	});
	
	asyncTest("osapi.people.getViewerFriends() - (paginated 1 per page, start index 1)", function(){

        var params = {networkDistance : 1, count : 1};

        var req = osapi.people.getViewerFriends(params);
        
        var params2 = {networkDistance : 1, startIndex : 1, count : 1};

        var req2 = osapi.people.getViewerFriends(params2);
        setTimeout(function(){
        	var count = 0;
        	var name;
        	
        	req.execute(function(dataResponse){

            	ok(!dataResponse.error,"no error in data response");
                if (!dataResponse.error) {
                  var dataCollection = dataResponse;
                  ok(dataResponse.startIndex == 0,"OpenSocial collections are zero indexed");
                  ok(dataResponse.itemsPerPage == 1, "1 item per page");
                  ok(dataResponse.list.length == 1,"list contains 1 item");
                  ok(dataResponse.list[0].id,"Person id at 0 is " + dataResponse.list[0].id);
                  if(!name){
                	  name = dataResponse.list[0].id;
                  } else {
                	  ok(dataResponse.list[0].id != name,"Different indices returned different names");
                  }
                }
                if(count == 1){
                	start();
                }
                
                count ++;
              });
        	
        	req2.execute(function(dataResponse){

            	ok(!dataResponse.error,"no error in data response");
                if (!dataResponse.error) {
                  var dataCollection = dataResponse;
                  ok(dataResponse.startIndex == 1,"Expect a start index of 1");
                  ok(dataResponse.itemsPerPage == 1, "1 item per page");
                  ok(dataResponse.list.length == 1,"list contains 1 item");
                  ok(dataResponse.list[0].id,"Person id at 1 is " + dataResponse.list[0].id);
                  if(!name){
                	  name = dataResponse.list[0].id;
                  } else {
                	  ok(dataResponse.list[0].id != name,"0 and 1 indices point to different Person objects");
                  }
                }
                if(count == 1){
                	start();
                }
                count ++;
              });
        	
      }, 1000);
        
      
	});
	
	
	asyncTest("osapi.people.get() - (viewer's friends sorted by id)", function(){

		var params = {userId : "@viewer", groupId : "@friends", networkDistance : 1, sortBy : "id"};
        var req = osapi.people.get(params);
        setTimeout(function(){
        	
        	req.execute(function(dataResponse){

            	ok(!dataResponse.error,"no error in data response");
                if (!dataResponse.error) {
                  var dataCollection = dataResponse;
                  ok(dataCollection.sorted,"Collection is marked as sorted");
                  //TODO Verify sorting

                }
                start();
              });
        	
      }, 1000);
	});
        
	asyncTest("osapi.people.get() - (viewer's friends fitered - id contains 'doe')", function(){

		var params = {  userId : "@viewer", 
						groupId : "@friends", 
						networkDistance : 1, 
						filterBy : "id",
						filterValue : "doe"
					 };
        var req = osapi.people.get(params);
        setTimeout(function(){
        
        	req.execute(function(dataResponse){

            	ok(!dataResponse.error,"no error in data response");
                if (!dataResponse.error) {
                  var dataCollection = dataResponse;
                  ok(dataCollection.filtered,"Collection is marked as filtered");
                  for(var i in dataCollection.list){
                	  ok(dataCollection.list[i].id.indexOf("doe") != -1,dataCollection.list[i].id+" contains \"doe\"");
                  }

                }
                start();
              });
        	
      }, 1000);
        
      
	});
	
	asyncTest("osapi.people.getViewerFriends() - (Paging out of bounds, startIndex = 9999)", function(){

        var params = {networkDistance : 1, count : 10, startIndex : 9999};

        var req = osapi.people.getViewerFriends(params);
        setTimeout(function(){
        
        	req.execute(function(dataResponse){
            	ok(dataResponse.error,"Error in data response");
                start();
              });
        	
      }, 1000);
        
      
	});

}

var _supportedPersonFields;

function getSupportedPersonFields(){
    if(!_supportedPersonFields){
    	_supportedPersonFields = new Array();
	    for(var field in opensocial.Person.Field){
	    	if(opensocial.getEnvironment().supportsField(opensocial.Environment.ObjectType.PERSON, opensocial.Person.Field[field])){
	    		_supportedPersonFields.push(opensocial.Person.Field[field]);
	    	}
	    }
	    ok(_supportedPersonFields,"Got list of supported Person fields");
	}
    return _supportedPersonFields;
}

//TODO Verify field values against compliance data
function testAsyncPeopleCollection(req, fields){


    setTimeout(function(){
    	
    	req.execute(function(dataResponse){
    		ok(!dataResponse.error,"No error in data response");
    		ok(dataResponse.totalResults,"Response has "+dataResponse.totalResults+" results");
    		ok(dataResponse.list.length == dataResponse.totalResults,"Response has a result list of size " + dataResponse.list.length);
    		var list = dataResponse.list;
    		for(var i=0; i<list.length; i++){
    			for(var j=0; j<fields.length; j++){
    				ok(requiredBySpec(fields[j]) && list[i][fields[j]] != null, 
    						"Friend " + i +" : " +fields[j] + " - " + list[i][fields[j]]);
    			}
    			
    		}
    		start();
    	});
    	
    	
      }, 1000);
}


function testAsyncViewerFieldResult(field){
    if (opensocial.getEnvironment().supportsField(opensocial.Environment.ObjectType.PERSON,field)) {
        	var params = {fields: [opensocial.Person.Field[field]]};
        	var req = osapi.people.getViewer(params);

			setTimeout(function(){
				req.execute(function(dataResponse) {
					ok(!dataResponse.error,"dataResponse without an error");
		            if (!dataResponse.error) {
		              var actual = dataResponse[opensocial.Person.Field[field]];
		              ok(actual != null, opensocial.Person.Field[field] + " value is " + actual);
		            }
		            
		        });
				
				start();
			}, 1000);

    } else {
    	ok(!requiredBySpec(field),"Container does not declare support for "+ field +" field.")
    	start();
    }
}

function testAsyncOwnerFieldResult(field){
    if (opensocial.getEnvironment().supportsField(opensocial.Environment.ObjectType.PERSON,field)) {
        	var params = {"fields": [opensocial.Person.Field[field]]};
        	var req = osapi.people.getOwner(params);

			setTimeout(function(){
				req.execute(function(dataResponse) {
					ok(!dataResponse.error,"dataResponse without an error");
		            if (!dataResponse.error) {
		              var actual = dataResponse[opensocial.Person.Field[field]];
		              ok(actual != null, opensocial.Person.Field[field] + " value is " + actual);
		            }
		
		        });
				
				start();
			},1000);

    } else {
    	ok(!requiredBySpec(field),"Container does not declare support for  "+ field +" field.")
    	start();
    }
}

