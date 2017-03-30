/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(window).load(function(){

    var animating = false;
    var menu = 'navigation';

    $(".navigation ul li").not('.navigation ul li ul li').click(function(e){
        e.preventDefault();

        if(animating){
            return;
        }

        var delay = 0,
            el = $(this),
            color = $(this).find('.left-menu-item').css('background-color'),
            prev = $(".navigation ul li:first"),
            distance = el.offset().top  - $(".navigation").offset().top,
            isLastElClicked = el.next().length > 0 ? false : true,
            hasSubMenu = el.find('.sublevel-menu').length == 1 ? true : false,
            isActiveItem = el.hasClass('active');

        if($('.sublevel-menu').is(':visible')){
            $('.sublevel-menu').slideUp();
            delay = 700;
        }

        if(isActiveItem && hasSubMenu){
            if(el.find('.sublevel-menu').is(':visible')){
                el.find('.sublevel-menu').slideUp();
            }else{
                el.find('.sublevel-menu').slideDown();
            }
            return;
        }

        if (el.prev().length > 0) {
            animating = true;
            $(el).find('.left-menu-item').css('background-color',color);
            $.when(
                el.delay(delay).animate({
                    top: -distance + 'px'
                },{
                    duration:700,
                    step:function(now,fx){
                        if(now === 0){
                            fx.end = -($(fx.elem).offset().top  - $(".navigation").offset().top);
                        }
                    }
                })
            ).done(function () {
                el.insertBefore(prev).css('top','0px').addClass('active');
                el.find('.left-menu-item').removeAttr('style');
                if(isLastElClicked){
                    prev.removeClass('active');

                }else{
                    prev.removeClass('active');
                }
                animating = false;

                if(hasSubMenu){
                    el.find('.sublevel-menu').slideDown();
                }

                setTimeout(function(){
                    //location.replace(el.attr('href'));
                },500);

            });
        }
    }).children('.sublevel-menu').find('li').click(function(e){
        e.stopImmediatePropagation();
    });
    
});