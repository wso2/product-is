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

function runActivitiesSuite(){
	
	module("OpenSocial JavaScript Activities Tests 1.1");
	
	asyncTest("osapi.activities.get defaults", function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get();
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				ok(response.totalResults == 1,"total results = " + response.totalResults + ", expected 1");
				var count = 0;
				for(var i in response.list){
					ok(response.list[i], "Activity " + i);
					ok(response.list[i].id, "Activity " + i + " id: " + response.list[i].id);
					ok(response.list[i].title, "Activity " + i + " title: " + response.list[i].title);
					ok(response.list[i].userId, "Activity " + i + " userId: " + response.list[i].userId);
					count ++;
				}
				ok(count == response.totalResults,"Number of activities in list should match totalResults");
				start();
			});			
		},1000);

	});
	
	asyncTest("osapi.activities.get viewer", function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get({userId: "@viewer"});
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				ok(response.totalResults == 1,"total results = " + response.totalResults + ", expected 1");
				var count = 0;
				for(var i in response.list){
					ok(response.list[i], "Activity " + i);
					ok(response.list[i].id, "Activity " + i + " id: " + response.list[i].id);
					ok(response.list[i].title, "Activity " + i + " title: " + response.list[i].title);
					ok(response.list[i].userId, "Activity " + i + " userId: " + response.list[i].userId);
					count ++;
				}
				ok(count == response.totalResults,"Number of activities in list should match totalResults");
				start();
			});			
		},1000);

	});
	
	asyncTest("osapi.activities.get owner", function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get({userId: "@owner"});
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				ok(response.totalResults == 1,"total results = " + response.totalResults + ", expected 1");
				var count = 0;
				for(var i in response.list){
					ok(response.list[i], "Activity " + i);
					ok(response.list[i].id, "Activity " + i + " id: " + response.list[i].id);
					ok(response.list[i].title, "Activity " + i + " title: " + response.list[i].title);
					ok(response.list[i].userId, "Activity " + i + " userId: " + response.list[i].userId);
					count ++;
				}
				ok(count == response.totalResults,"Number of activities in list should match totalResults");
				start();
			});			
		},1000);

	});
	
	asyncTest("osapi.activities.get viewer friends",function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get({userId : "@viewer", groupId : "@friends"});
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				ok(response.totalResults == 2,"total results = " + response.totalResults + ", expected 2");
				var count = 0;
				for(var i in response.list){
					ok(response.list[i], "Activity " + i);
					ok(response.list[i].id, "Activity " + i + " id: " + response.list[i].id);
					ok(response.list[i].title, "Activity " + i + " title: " + response.list[i].title);
					ok(response.list[i].userId, "Activity " + i + " userId: " + response.list[i].userId);
					count ++;
				}
				ok(count == response.totalResults,"Number of activities in list should match totalResults");
				start();
			});			
		},1000);

	});
	
	asyncTest("osapi.activities.get owner friends",function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get({userId : "@owner", groupId : "@friends"});
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				ok(response.totalResults == 2,"total results = " + response.totalResults + ", expected 2");
				var count = 0;
				for(var i in response.list){
					ok(response.list[i], "Activity " + i);
					ok(response.list[i].id, "Activity " + i + " id: " + response.list[i].id);
					ok(response.list[i].title, "Activity " + i + " title: " + response.list[i].title);
					ok(response.list[i].userId, "Activity " + i + " userId: " + response.list[i].userId);
					count ++;
				}
				ok(count == response.totalResults,"Number of activities in list should match totalResults");
				start();
			});			
		},1000);

	});
	
	asyncTest("osapi.activities.get with non-existant ID",function(){
		ok(osapi.activities.get, "osapi.activities.get exists");
		var req = osapi.activities.get({userId : "DOES_NOT_EXIST"});
		ok(req != null,"Req not null");
		setTimeout(function(){
			req.execute(function(response){
				ok(!response.error, "No error in get response");
				console.log(response);
				ok(response.totalResults == 0);
				start();
			});			
		},1000);

	});
	
	
}
