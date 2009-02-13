// JS added after entire body. At this point, it should be relatively safe to
// tamper with window.onload and other global signals.

(function() {

window.BODY_END_TIME = new Date();

var original_window_onload = window.onload;
function http_mangler_window_onload() {
	window.ONLOAD_START_TIME = new Date();
	
	window.setTimeout(http_mangler_window_onload_after_settimeout);

	if (original_window_onload) original_window_onload.call(this);
	
	window.ONLOAD_END_TIME = new Date();
}
window.onload = http_mangler_window_onload();

function http_mangler_window_onload_after_settimeout() {
	var head_time = window.HEAD_END_TIME - window.HEAD_START_TIME;
	var body_time = window.BODY_END_TIME - window.BODY_START_TIME;
	var onload_time = window.ONLOAD_END_TIME - window.ONLOAD_START_TIME;
	var body_total_time = window.BODY_END_TIME - window.HEAD_START_TIME;
	var between_body_and_onload_time = window.ONLOAD_START_TIME - window.BODY_END_TIME;
	var total_time = window.ONLOAD_END_TIME - window.HEAD_START_TIME;
	
	var s = "To complete <head>: " + head_time + "ms\n" +
			"To complete <body>: " + body_time + "ms\n" +
			"To complete window.onload: " + onload_time + "ms\n" +
			"\n" +
			"Aggregates:\n" +
			"From start to end of <body>: " + body_total_time + "ms\n" +
			"From end of <body> to start of window.onload: " + between_body_and_onload_time + "ms\n" +
			"From start to end of window.onload: " + total_time + "ms\n" +
			"(we do not consider timeouts from within window.onload)";
	if (window.console && window.console.log) {
		window.console.log(s);
	} else {
		alert(s);
	}
}

})();