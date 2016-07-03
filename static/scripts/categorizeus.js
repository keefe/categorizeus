var tmplBasicDocument;

var loadDocument = function(id, cb){
	$.ajax({
		url:'/msg/'+id,
		accepts:'application/json'
	}).done(function(message, statusCode){
		console.log("In Response " + statusCode);
		if(cb){
			cb(message);
		}
	});

};

$(document).ready(function(){
	console.log("We're up");
	tmplBasicDocument = Handlebars.compile($("#tmplBasicDocument").html());

	$("#btnLoadDoc").click(function(){
		var id = $("#txtDocId").val();
		loadDocument(id, function(doc){
			$("#content").html(tmplBasicDocument(doc));
		});
	});
});
