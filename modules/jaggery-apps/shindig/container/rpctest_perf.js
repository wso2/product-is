/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var perfStats = null;
var currentRun = {};

function perfService(message) {
  if (perfStats.numResults++ === 0) {
    perfStats.firstMsg = message; // stored since it has "real" start time
  }
  perfStats.bytesReceived += message.length;
}

function clearPerfStats(inOrOut) {
  perfStats = {
    numResults: 0,
    bytesReceived: 0,
    firstMsg: null
  };

  document.getElementById('in_or_out').innerHTML = inOrOut;

  // hide results fields
  document.getElementById('results').style.display = 'none';
}

function completePerfStats() {
  perfStats.timeEnded = new Date().getTime();

  // get time started from the first sent message
  perfStats.timeStarted = perfStats.firstMsg.substr(0, perfStats.firstMsg.indexOf(':'));

  var timeUsedMs = perfStats.timeEnded - perfStats.timeStarted;

  // fill in fields
  document.getElementById('results_num_received').innerHTML = perfStats.numResults;
  document.getElementById('results_bytes_received').innerHTML = perfStats.bytesReceived;
  document.getElementById('results_time_used').innerHTML = timeUsedMs + 'ms';
  document.getElementById('results_msgs_per_sec').innerHTML = (perfStats.numResults / (timeUsedMs / 1000));
  document.getElementById('results_bytes_per_sec').innerHTML = (perfStats.bytesReceived / (timeUsedMs / 1000));
  document.getElementById('results_referrer').innerHTML = (this['referer'] || 'n/a') + ' -- config: ' +
      (gadgets.config.get('rpc')['passReferrer'] || '<empty>');
  document.getElementById('test_running').style.display = 'none';
  document.getElementById('results').style.display = '';
}

function syncCallbackService(toEcho) {
  return toEcho;
}

function asyncCallbackService(toEcho) {
  var self = this;
  window.setTimeout(function() {
    self.callback(toEcho);
  }, 0);
}

function initPerfTest() {
  clearPerfStats();
  gadgets.rpc.register('perf_service', perfService);
  gadgets.rpc.register('clear_perf_stats', clearPerfStats);
  gadgets.rpc.register('complete_perf_stats', completePerfStats);
  gadgets.rpc.register('sync_callback_service', syncCallbackService);
  gadgets.rpc.register('async_callback_service', asyncCallbackService);
}

var alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-*&(){}'";

function sendPerfMessage() {
  var msgToSend = currentRun.msg;
  if (currentRun.curMsgId++ <= 1) {
    var nowString = new Date().getTime() + ':';
    msgToSend = nowString + currentRun.msg.substring(nowString.length);
  }

  gadgets.rpc.call(currentRun.targetId, 'perf_service', null, msgToSend);
  if (currentRun.curMsgId < currentRun.endMsgId) {
    // loop, giving up execution in case rpc technique demands it
    window.setTimeout(sendPerfMessage, 0);
  } else {
    // send finisher
    window.setTimeout(function() { gadgets.rpc.call(currentRun.targetId, 'complete_perf_stats', null); }, 0);
  }
}

function runPerfTest(targetId) {
  document.getElementById('test_running').style.display = '';

  // initialize the current run
  var num_msgs = document.getElementById('num_msgs').value;
  var msg_size = document.getElementById('msg_size').value;

  currentRun.targetId = targetId;
  currentRun.curMsgId = 0;
  currentRun.endMsgId = num_msgs;

  var msg = [];
  for (var i = 0; i < msg_size; ++i) {
    msg[i] = alphabet.charAt(Math.round(Math.random(alphabet.length)));
  }
  currentRun.msg = msg.join('');

  // clear local perf stats
  clearPerfStats('(outbound)');

  // clear target perf stats
  gadgets.rpc.call(targetId, 'clear_perf_stats', null, '(inbound)');

  // kick off the send loop
  sendPerfMessage();
}

function runCallbackTest(targetId, isSync) {
  document.getElementById('echo_test_result').innerHTML = '';
  var service = (isSync ? '' : 'a') + 'sync_callback_service';
  var echoValue = document.getElementById('echo_test_input').value;
  var callback = function(response) {
    document.getElementById('echo_test_result').innerHTML = response + ' at ' + new Date().toUTCString() + ' from referer: ' + this['referer'];
  };
  gadgets.rpc.call(targetId, service, callback, echoValue);
}
