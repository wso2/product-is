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

	$(".search-wrap .dropdown-menu li a").click(function(){
	  	$(".search-wrap .dropdown-toggle").html($(this).text() + ' <span class="caret"></span>');
	});

	$('.btn-launch').hover(function(){
		$(this).parent().find('.image-container .after').show();
	},function(){
		$(this).parent().find('.image-container .after').hide();
	});

    $('input[name=recover-option],[name=recover-option-email],[name=recover-option-question]').click(function(){
        $('.recover-option-container').hide();
        $('.'+$(this).val()).closest('.recover-option-container').fadeIn();
    });


	$('.rating:not(.half) .icon').click(function(){
        var elem = $(this);

        elem.siblings('.icon').removeClass('active');
        elem.addClass('active');

        var rating = ($(this).closest('.rating').find('.icon').length - $(this).index());
        elem.trigger('clicked.rate', [rating]);
    });

    $('.rating:not(.half) .icon').on('clicked.rate', function(e, data){
        console.log(data);
    });

    $('.rating.half .icon').children().click(function(){
        var elem = $(this);

        elem.closest('.icon').siblings().removeClass('active');
        elem.siblings().removeClass('active');
        elem.closest('.icon').addClass('active');
        elem.addClass('active');

        var rating = function(){
            if(elem.index() > 0){
                return (elem.closest('.rating').find('.icon').length - elem.closest('.icon').index() - 1) + '.5';
            }
            else{
                return (elem.closest('.rating').find('.icon').length - elem.closest('.icon').index()) + '.0';
            }
        };

        elem.trigger('clicked.rate.half', [rating()]);
    });

    $('.rating.half .icon').children().on('clicked.rate.half', function(e, data){
        console.log(data);
    });

    /**
     * Affix spacer when media left is affixed
     */
    $('.sidebar-wrapper').on('affix.bs.affix',function(){
        $('.media').prepend('<div class="affix-spacer" style="width:'+ $(this).width() +'px;float:left;height: 100vh"></div>');
    }).on('affixed-top.bs.affix',function(){
        $('.affix-spacer').remove();
    });

    $('.sign-up-additional').click(function(){
        var el = $('.extended-form'),
            triggerEl = $('.sign-up-additional');
        if(el.is(':hidden')){
            el.slideDown(function(){
                triggerEl.text('Hide Additional Details');
            });
        }else{
            el.slideUp(function(){
                triggerEl.text('Show Additional Details');
            });
        }
    });

});
