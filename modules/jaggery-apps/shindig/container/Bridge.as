/*
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

import flash.external.ExternalInterface;

class com.google.Bridge extends MovieClip {
  // The lcJS object stores a listening LocalConnection for each SWF on the page.
  // These connections carry messages intentded for JavaScript code.
  private var lcJS;
  // We only need one LocalConnection for sending.
  private var lcSWF;
  
  function log(msg) {
    if (_root.logging) {
      ExternalInterface.call('gadgets.log', msg);
    }
  }
  
  function callSWF(channel, methodName, argv) {
    log('callSWF: ' + methodName + ', ' + lcSWF);
    return {
				channel: channel, 
				methodName: methodName, 
				argv: argv, 
				result: lcSWF.send(channel + "swf", methodName, argv)
			};
  }
    
  function callJS(methodName, argv) {
		log('callJS: ' + methodName);
    return ExternalInterface.call("callJS", methodName, argv);
  }
  
  function registerChannel(channel) {
    log('registerChannel: ' + channel);
    lcJS[channel] = new LocalConnection();
    log('registerChannel: ' + lcJS[channel].connect(channel + "js"));
    lcJS[channel].callJS = callJS;
    lcJS[channel].allowDomain = function (domain) {
      log('allowDomain: ' + domain);
      return true;
    }
    return channel;
  }
  
  // Fires when the movie loads  
  function onLoad() {
    lcJS = [];
    lcSWF = new LocalConnection();

    log('onLoad; domain = ' + lcSWF.domain());
    ExternalInterface.addCallback('callSWF', this, callSWF);
    ExternalInterface.addCallback('registerChannel', this, registerChannel);
    ExternalInterface.call('onFlashBridgeReady');
  }
}
