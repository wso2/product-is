/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
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

$(document).ready(function () {
    var id_token = $("#id-token").val();
    var tokens = id_token.split('.');
    var decoded_token1 = decodeToken(tokens[0]);
    var decoded_token2 = decodeToken(tokens[1]);

    $('.id-token').append("<span class='token1'> " + tokens[0] + "</span>.<span class='token2'> " + tokens[1] +
        "</span>.<span class='token3'>" + tokens[2] + "</span> ");

    var decoded_token1_str = JSON.stringify(decoded_token1, null, 4);
    var decoded_token2_str = JSON.stringify(decoded_token2, null, 4);

    $('.formatted-token pre').append("<span class='token1'>" + decoded_token1_str + "</span>");
    $('.formatted-token pre').append("<br><br><span class='token2'>" + decoded_token2_str + "</span>");

    var payload = JSON.parse(window.atob(id_token.split(".")[1]));
    $("span.user-name").text(payload.sub + ' ');
    $(".welcome-text").text('Welcome ' + payload.sub);

    $('.nav-toggle').click(function() {
        //get collapse content selector
        var collapse_content_selector = $(this).attr('href');

        var toggle_switch = $(this);
        $(collapse_content_selector).toggle(function() {
          if ($(this).css('display') == 'none') {
            toggle_switch.html('Show Authentication Details');
          } else {
            toggle_switch.html('Hide Authentication Details');
          }
        });
      });
});

function decodeToken(token) {
    var base64 = token.replace('-', '+').replace('_', '/');
    return JSON.parse(window.atob(base64));
};
