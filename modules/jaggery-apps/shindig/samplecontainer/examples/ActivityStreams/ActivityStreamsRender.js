/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

function ActivityStreamsRender() {

	// Private member that wraps the OpenSocial API
	var social = new OpenSocialWrapper();

	// =================== PUBLIC ====================

	// Renders the welcome text (viewer, owner, and friends)
	this.renderWelcome = function(div, callback) {
		social.loadPeople(function(response) {
			viewer = response.viewer;
			owner = response.owner;
			var viewerFriends = response.viewerFriends;
			var ownerFriends = response.ownerFriends;

			var html = '<h1>Welcome ' + viewer.name.formatted + '!</h1>';
			html += 'You are viewing ' + owner.name.formatted + "'s data. <br><br>";
			html += 'Here is a list of your friends: <br>';
			html += '<lu>';
			for (i = 0; i < viewerFriends.list.length; i++) {
				html += '<li>' + viewerFriends.list[i].name.formatted + '</li>';
			}
			html += '</lu>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	// Renders the activities
	this.renderActivities = function(div, callback) {
		social.loadActivities(function(response) {
			var viewerActivities = response.viewerActivities.list;
			var ownerActivities = response.ownerActivities.list;
			var friendActivities = response.friendActivities.list;

			var html = '<h1>Activities</h1>';
			html += 'Demonstrates use of the Activities service in Apache Shindig.  The ActivityStreams service does not interfere with this service.<br><br>';
			html += 'Activities for you and ' + owner.name.formatted + ':<br>';
			html += "<table border='1'>";
			html += '<tr>';
			html += '<td>Name</td>';
			html += '<td>Title</td>';
			html += '<td>Body</td>';
			html += '<td>Images</td>';
			html += '</tr>';
			html += processActivities(viewerActivities);
			html += processActivities(ownerActivities);
			html += processActivities(friendActivities);
			html += '</table>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	// Renders activity entries
	this.renderActivityEntries = function(div, callback) {
		social.loadActivityEntries(function(response) {
			var html = '';
			viewerEntries = response.viewerEntries.list;
			//ownerEntries = response.ownerEntries.list;
			//friendEntries = response.friendEntries.list;
			html = '<h2>ActivityEntries</h2>';
			html += processActivityEntries(viewerEntries);
			//html += processActivityEntries(ownerEntries);
			//html += processActivityEntries(friendEntries);
			if (viewerEntries.length == 0) {
				html += '<tr><td>No entries to show!</td></tr>';
			}
			html += '</table><br><br>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	// ================== PRIVATE =====================

	// Processes activities and returns the rendered HTML
	function processActivities(activities) {
		var html = '';
		for (idx = 0; idx < activities.length; idx++) {
			html += '<tr>';
			html += '<td>' + activities[idx].userId + '</td>';
			html += '<td>' + activities[idx].title + '</td>';
			html += '<td>' + activities[idx].body + '</td>';
			var mediaItems = activities[idx].mediaItems;
			if (mediaItems != null) {
				for (itemIdx = 0; itemIdx < mediaItems.length; itemIdx++) {
					if (mediaItems[itemIdx].type == 'image') {
						html += "<td><img src='" + mediaItems[itemIdx].url + "' width=150 height=150/></td>";
					}
				}
			}
			html += '</tr>';
		}
		return html;
	}

	// Processes activity entries and returns the rendered HTML
	function processActivityEntries(entries) {
		var html = '';
		for (idx = 0; idx < entries.length; idx++) {
			if (entries[idx].object.url && entries[idx].object.url != 'null') {
				html += "<h3><a href='" + entries[idx].object.url + "'>" + entries[idx].title + '</a></h3>';
			} else {
				html += '<h3>' + entries[idx].title + '</h3>';
			}
			html += 'ID: ' + entries[idx].id + '<br>';
			html += 'Actor: ' + entries[idx].actor.displayName + '<br>';
			html += 'Posted: ' + entries[idx].published + '<br>';
			if (entries[idx].content && entries[idx].content != 'null') {
				html += 'Content: ' + entries[idx].content + '<br>';
			}
		}
		return html;
	}
}
