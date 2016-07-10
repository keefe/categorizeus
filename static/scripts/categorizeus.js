var tmplBasicDocument;
var tmplBasicDocumentEdit;
var tmplLogin;
var tmplRegister;


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
		console.log(response);
		if(cb){
			cb(response);
		}
	});
};

var loginUser = function(user, cb){
	$.ajax({
		url:'/user/',
		method:'POST',
		contentType:"application/json",
		data:JSON.stringify(user)
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!="success"){
			$("#status").html("<h1>Error logging in! Please try again</h1>");
		}else{
			$("#status").html("<h1>Logged in successfully, welcome!</h1>");
		}
		if(cb){
			cb(response);
		}
	});
}

var registerUser = function(user, cb){
	$.ajax({
		url:'/user/',
		method:'PUT',
		contentType:"application/json",
		data:JSON.stringify(user)
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!="success"){//TODO why is this not just 200?
			$("#status").html("<h1>Error registering user! Please try again</h1>");
			$("#status").append(response);
		}else{
			$("#status").html("<h1>Registered user successfully, welcome!</h1>");
		}
		if(cb){
			cb(response);
		}
	});
}

var dynamicLogin = function(el){
	return function(){
		var username = el.find(".txtUsername").val();
		var password = el.find(".txtPassword").val();
		var user = {
			username:username,
			password:password
		};
		loginUser(user);
	};
}

var dynamicRegister = function(el){
	return function(){
		var username = el.find(".txtUsername").val();
		var password = el.find(".txtPassword").val();
		var user = {
			username:username,
			password:password
		};
		registerUser(user);
	};
}

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

var displayLoginForm = function(container){ //#TODO hey we are seeing a template pattern here, let's generalize it
	var controls = $(container).html(tmplLogin({}));
	controls.find(".btnLogin").click(dynamicLogin(controls));
}

var displayRegisterForm = function(container){ //#TODO hey we are seeing a template pattern here, let's generalize it
	var controls = $(container).html(tmplRegister({}));
	controls.find(".btnRegister").click(dynamicRegister(controls));
}

$(document).ready(function(){
	console.log("We're up");
	tmplBasicDocument = Handlebars.compile($("#tmplBasicDocument").html());
	tmplBasicDocumentEdit = Handlebars.compile($("#tmplBasicDocumentEdit").html());//notice the pattern
	tmplLogin = Handlebars.compile($("#tmplLogin").html());
	tmplRegister = Handlebars.compile($("#tmplRegister").html());

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

	$("#btnLogin").click(function(){
		displayLoginForm("#editor");
	});

	$("#btnRegister").click(function(){
		displayRegisterForm("#editor");
	});


});
