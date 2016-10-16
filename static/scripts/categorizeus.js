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
		if(cb){//TODO check for status code here?
			cb(null, message);
		}
	}).fail(function(){
		if(cb){
			cb("Can't tag messages for some reason");
		}
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
	}).done(function(messages, statusCode){
		if(statusCode!='success'){
			if(cb){
				cb("Error doing tag search!");
			}
		}else if(cb){
			cb(null, messages);
		}
	});
};

var tagSearchThread = function(tagArray, cb){
	var threadCriteria = {
		searchTags:tagArray,
		transitiveTags:["repliesTo"]
	};
	searchThreadCriteria(threadCriteria, cb);
};

var searchThreadCriteria = function(threadCriteria, cb){
	$.ajax({
		url:'/thread/',
		accepts:'application/json',
		method:'POST',
		contentType:"application/json",
		data:JSON.stringify(threadCriteria)
	}).done(function(messageThread, statusCode){//TODO fail handler
		if(statusCode!='success'){
			if(cb){
				cb("Error doing tag search!");
			}
		}else if(cb){
			cb(null, messageThread);
		}
	});
};

var loadMessage = function(id, cb){
	$.ajax({
		url:'/msg/'+id,
		accepts:'application/json'
	}).done(function(message, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!='success'){
			if(cb){
				cb("Error doing doc load!", response);
			}
		}else if(cb){
			cb(null, message);
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
			if(cb){
				cb("Please Login to Post", response);
			}
		}else if(cb){
			cb(null, response);
		}
	}).fail(function(){
		cb("Please Login to Post");
	});
};

var uploadMessage = function(message, files, cb){
	var formData = new FormData();
	formData.append('body', message.body);
	formData.append('title', message.title);
	formData.append('tags', message.tags);
	if(message.repliesToId!=null){
		formData.append('repliesToId', message.repliesToId);
	}
	formData.append('attachment', files[0]);//TODO this must be last, think about how to handle this better
	$.ajax({
		url:'/msg/upload/',
		method:'POST',
		contentType:false,
		processData:false,
		data: formData
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		console.log(response);
		if(statusCode!='success'){
			if(cb){
				cb("Please Login to Post", response);
			}
		}else if(cb){
			cb(null, response);
		}
	}).fail(function(){
		cb("Please Login to Post");
	});
};


var loginUser = function(username, password, cb){
	var user = {
		username:username,
		password:password
	};
	$.ajax({
		url:'/user/',
		method:'POST',
		contentType:"application/json",
		data:JSON.stringify(user)
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!="success"){
			if(cb){
				cb("Error logging in! Please try again");
			}
		}else{
			if(cb){
				cb(null, response);//TODO what should this response be?
			}
		}

	});
};

var fetchCurrentUser = function(cb){
	$.ajax({
		url:'/user/',
		accepts:'application/json'
	}).done(function(currentUser, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!='success'){
			if(cb){
				cb("User is not Logged In", response);
			}
		}else if(cb){
			cb(null, currentUser);
		}
	}).fail(function(){
		if(cb!=null){
			cb("User is not Logged In");
		}
	});	
};

var logoutUser = function(cb){
	$.ajax({
		url:'/user/',
		method:'DELETE',
		accepts:'application/json'
	}).done(function(currentUser, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!='success'){
			if(cb){
				cb("User is not Logged In", response);
			}
		}else if(cb){
			cb(null, currentUser);
		}
	}).fail(function(){
		if(cb!=null){
			cb("User is not Logged In");
		}
	});	
};


var registerUser = function(username, password, cb){
	var user = {
		username:username,
		password:password
	};
	$.ajax({
		url:'/user/',
		method:'PUT',
		contentType:"application/json",
		data:JSON.stringify(user)
	}).done(function(response, statusCode){
		console.log("In Response " + statusCode);
		if(statusCode!="success"){
			if(cb){
				cb("Error registering! Please try again");
			}
		}else{
			if(cb){
				cb(null, response);//TODO what should this response be?
			}
		}
	});
}
