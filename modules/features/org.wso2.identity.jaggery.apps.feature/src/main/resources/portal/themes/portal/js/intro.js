
var filterPath, scrollableElement, cc, range, scrollorama, responsivegadget;

$(document).ready(function() {
		
	filterPath = function(string) {
		return string.replace(/^\//, '').replace(/(index|default).[a-zA-Z]{3,4}$/, '').replace(/\/$/, '');
	}
	
	var locationPath = filterPath(location.pathname);

	scrollableElement = function(els) {
		for (var i = 0, argLength = arguments.length; i < argLength; i++) {
			var el = arguments[i], $scrollElement = $(el);
			if ($scrollElement.scrollTop() > 0) {
				return el;
			} else {
				$scrollElement.scrollTop(1);
				var isScrollable = $scrollElement.scrollTop() > 0;
				$scrollElement.scrollTop(0);
				if (isScrollable) {
					return el;
				}
			}
		}
		return [];
	}
	var scrollElem = scrollableElement('html', 'body');

	var animateFeatures = function() {

		scrollorama = scrollorama || $.scrollorama({
			blocks : '.scrollblock'
		});
		/*
		scrollorama.animate('#feature-dashboard', {
			delay : 100,
			duration : 200,
			property : 'top',
			start : -400,
			end : 0
		});
		
		scrollorama.animate('#feature-jaggery', {
			delay : 100,
			duration : 200,
			property : 'top',
			start : -400,
			end : 0
		});
		*/
	}
	
	
	/*----------------function calls---------------------*/


	$("#SliderSingle").slider({
		from : 1,
		to : 4,
		step : 1,
        format: { format: '####'},
        scale : ['JPN' ,'AUS', 'USA', 'CHN'],
		skin : "round_plastic",
        calculate: function( value ){
            var country;
            switch(value) {

                case 1:
                    country = "JPN";
                    break;

                case 2:
                    country = "AUS";
                    break;

                case 3:
                    country = "USA";
                    break;

                case 4:
                    country = "CHN";
                    break;

            }
            return country;
        },
		callback : function(value) {
			$("#SliderSingle").slider('value', value);

			updateIntroGadgets(value);

		}
	});
	$("#SliderSingle").slider('value', 1);
	
	$('.jslider-pointer').append($('<span id="pulse"></span>'));

	$('a[href*=#]').each(function() {
		var thisPath = filterPath(this.pathname) || locationPath;
		if (locationPath == thisPath && (location.hostname == this.hostname || !this.hostname) && this.hash.replace(/#/, '')) {
			var $target = $(this.hash), target = this.hash;
			if (target) {
				var targetOffset = $target.offset().top;
				$(this).click(function(event) {
					event.preventDefault();
					$(scrollElem).animate({
						scrollTop : targetOffset
					}, 400, function() {
						location.hash = target;
					});
				});
			}
		}
	});

	animateFeatures();

});

