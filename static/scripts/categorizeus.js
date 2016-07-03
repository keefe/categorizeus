var tmplBasicDocument;
var tmplBasicDocumentEdit;

var loadMessage = function(id, cb){
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

var createMessage = function(message, cb){
	$.ajax({
		url:'/msg/',
		method:'POST',
		contentType:"application/json",
		data:JSON.stringify(message)
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		if(cb){
			cb(response);
		}
	});

};

var dynamicEditSubmit = function(el){
	
	return function(){
		console.log("Dynamically bound control OK");
		var title = el.find(".inputMsgTitle").val();
		var body = el.find(".inputMsgBody").val();
		var id = el.find(".inputMsgId").val();
		var isNew = (!id) || id.length==0;
		console.log(id + " is new? " + isNew + " " + title + " & " + body);
		if(isNew){
			var newMessage = {
				body:body,
				title:title
			};
			createMessage(newMessage, function(response){
				$("#status").append("<p>Created new document with id " + response + "</p>");
			});
			
		}else{
			$("#status").append("<p>Currently, editing existing docs not supported. Clear and try again.</p>");
		}

	};
}

var displayEditForm = function(container, sourceMsg){//#TODO don't just replace
	var controls = $(container).html(tmplBasicDocumentEdit(sourceMsg));
	controls.find(".inputMsgBtn").click(dynamicEditSubmit(controls));
}

$(document).ready(function(){
	console.log("We're up");
	tmplBasicDocument = Handlebars.compile($("#tmplBasicDocument").html());
	tmplBasicDocumentEdit = Handlebars.compile($("#tmplBasicDocumentEdit").html());//notice the pattern
	displayEditForm("#editor", {});

	$("#btnLoadDoc").click(function(){
		var id = $("#txtDocId").val();
		loadMessage(id, function(doc){
			$("#content").html(tmplBasicDocument(doc));
			displayEditForm("#editor", doc);
		});
	});


	$("#btnClearDoc").click(function(){
		displayEditForm("#editor", {});
	});


});
