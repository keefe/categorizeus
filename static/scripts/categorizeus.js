var tmplBasicDocument;
var tmplBasicDocumentEdit;
var tmplLogin;
var tmplRegister;


var tagMessages = function(tagArray, messageArray, cb){
	var payload = {
		tags:tagArray,
		messages:messageArray
	};
	$.ajax({
		url:'/tag/',
		accepts:'application/json',
		method:'PUT',
		contentType:"application/json",
		data:JSON.stringify(payload)
	}).done(function(message, statusCode){
		$("#status").html("Tagged Messages Successfully");
		if(cb){
			cb(message);
		}
	}).fail(function(){
		$("#status").html("Can't tag messages for some reason");
	});
};

var tagSearch = function(tagArray, cb){
	var payload = {
		tags:tagArray
	};
	$.ajax({
		url:'/tag/',
		accepts:'application/json',
		method:'POST',
		contentType:"application/json",
		data:JSON.stringify(payload)
	}).done(function(message, statusCode){
		if(cb){
			cb(message);
		}
	});
};

var displayMessages = function(messages){
	$("#content").empty();
	for(var i=0; i<messages.length;i++){
		console.log(messages[i]);
		$("#content").append(tmplBasicDocument(messages[i]))
	}
};

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
		if(statusCode!='success'){
			$("#status").html("Please Login to Post");
		}else if(cb){
			cb(response);
		}
	}).fail(function(){
		$("#status").html("Please Login to Post");
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
