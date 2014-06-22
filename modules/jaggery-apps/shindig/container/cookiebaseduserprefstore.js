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

/**
 * @fileoverview Implements the gadgets.UserPrefStore interface using a cookies
 * based implementation. Depends on cookies.js. This code should not be used in
 * a production environment.
 */

/**
 * Cookie-based user preference store.
 * @constructor
 */
gadgets.CookieBasedUserPrefStore = function() {
  gadgets.UserPrefStore.call(this);
};

gadgets.CookieBasedUserPrefStore.inherits(gadgets.UserPrefStore);

gadgets.CookieBasedUserPrefStore.prototype.USER_PREFS_PREFIX =
    'gadgetUserPrefs-';

gadgets.CookieBasedUserPrefStore.prototype.getPrefs = function(gadget) {
  var userPrefs = {};
  var cookieName = this.USER_PREFS_PREFIX + gadget.id;
  var cookie = shindig.cookies.get(cookieName);
  if (cookie) {
    var pairs = cookie.split('&');
    for (var i = 0; i < pairs.length; i++) {
      var nameValue = pairs[i].split('=');
      var name = decodeURIComponent(nameValue[0]);
      var value = decodeURIComponent(nameValue[1]);
      userPrefs[name] = value;
    }
  }

  return userPrefs;
};

gadgets.CookieBasedUserPrefStore.prototype.savePrefs = function(gadget) {
  var pairs = [];
  for (var name in gadget.getUserPrefs()) {
    var value = gadget.getUserPref(name);
    var pair = encodeURIComponent(name) + '=' + encodeURIComponent(value);
    pairs.push(pair);
  }

  var cookieName = this.USER_PREFS_PREFIX + gadget.id;
  var cookieValue = pairs.join('&');
  shindig.cookies.set(cookieName, cookieValue);
};

gadgets.Container.prototype.userPrefStore =
    new gadgets.CookieBasedUserPrefStore();
