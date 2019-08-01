var showAlert = function(msg, type, target) {
	type = type || 'info';
	$(target)
		.html(msg)
		.removeClass()
		.addClass('alert-bar').addClass(type)
		.stop()
		.fadeIn()
		.delay(2000)
		.fadeOut()
	;
}