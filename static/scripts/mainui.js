$(document).ready(function(){
	$("#btnCategorizeUs").click(function(event){
		location.reload(true);
	});	
	$("#btnNotifications").click(function(event){

	});
	$("#btnSearch").click(function(event){

	});
	$("#btnPost").click(function(event){

	});
	$("#btnAbout").click(function(event){
		$(".aside").toggle("unseen");
		$(".nav").toggle("unseen");
	});
});

var tmplBasicDocument;
var tmplBasicDocumentEdit;
var tmplLogin;
var tmplRegister;
var tmplFullMessage;
var currentThread;
var tmplIndividualComment;
var tmplNavigation;
var threadRelations = {};
var threadMessages = {};
var lastStartingId = null;
var currentUser = null;

var initialize = function(){
	tmplBasicDocument = Handlebars.compile($("#tmplBasicDocument").html());
	tmplBasicDocumentEdit = Handlebars.compile($("#tmplBasicDocumentEdit").html());//notice the pattern, probably put these in an object and generalize
	tmplLogin = Handlebars.compile($("#tmplLogin").html());
	tmplFullMessage = Handlebars.compile($("#tmplFullMessage").html());
	tmplIndividualComment = Handlebars.compile($("#tmplIndividualComment").html());
	tmplNavigation= Handlebars.compile($("#tmplNavigation").html());
	fetchCurrentUser(function(err, user){
		if(err!=null){
			console.log("Nobody is logged in");
			console.log(err);
			return; 
		}
		currentUser = user;
		$("#btnShowLogin").prop("value", "logout");
		console.log(user);
		$("#status").html("<h1>Welcome back "+user.userName+"</h1>");
	});
	tagSearchThread(["top"], displayMessageThread);
	$("#btnShowLogin").click(function(){
		if(currentUser==null){
			console.log("Clicking Login Button");
			displayLoginForm("#editor");
		}else{
			logoutUser(function(err, user){
				if(err!=null){
					$("#status").html("<h1> "+err+"</h1>");
					return;
				}
				currentUser = null;
				$("#btnShowLogin").prop("value", "login");
				$("#status").html("<h1>Logout is complete</h1>");
			});

		}
	});
	$("#btnPost").click(function(){
		if(currentUser==null){
			displayLoginForm("#editor");
		}else{
			displayEditForm("#editor", {});
		}
	});

	$("#btnSearch").click(function(){
		var tags = $("#txtTagSearch").val();
		var allTags = tags.split(" ");
		var tagArray = ["top"];
		for(var i=0; i<allTags.length;i++){
			if(allTags[i].length>0){
				tagArray.push(allTags[i]);
			}
		}
		tagSearchThread(tagArray, displayMessageThread);	
	});

	$("#btnTag").click(function(){

		var tags = $("#txtTagSearch").val();
		var allTags = tags.split(" ");
		var tagArray = [];
		for(var i=0; i<allTags.length;i++){
			if(allTags[i].length>0){
				tagArray.push(allTags[i]);
			}		}

		var whichTagged = [];
		$('.basicDocument.selected').each(function () {
			whichTagged.push(this.id);
		});
	
		if(tagArray.length==0){
			$("#status").html("<h1>Please provide tags when tagging</h1>");
			return;
		}
		if(whichTagged.length==0){
			$("#status").html("<h1>Please select messages when tagging</h1>");
			return;
		}
		tagMessages(tagArray, whichTagged,function(err, message){
			if(err!=null){
				$("#status").html(err);
			}else{
				$("#status").html("Tagged Messages Successfully");	
			}
		});
	});

}


var displayEditForm = function(container, sourceMsg){//#TODO don't just replace
	var controls = $(container).html(tmplBasicDocumentEdit(sourceMsg));
	controls.find(".inputMsgBtn").click(dynamicEditSubmit(controls));
	controls.find(".closeButton").click(function(event){
		controls.remove();
	});
}

var displayLoginForm = function(container){ //#TODO hey we are seeing a template pattern here, let's generalize it
	var controls = $(container).html(tmplLogin({}));
	controls.find(".btnLogin").click(dynamicLogin(controls));
	controls.find(".closeButton").click(function(){
		controls.empty();
	});
}
var displayMessageComments = function(message, messageView){
	if(threadRelations[message.id]!=null){
		for(var relatedMessage in threadRelations[message.id]){
			var replyId = threadRelations[message.id][relatedMessage];
			console.log("Need to display " + replyId);
			var appliedTemplate = $(tmplIndividualComment(threadMessages[replyId]));
			var newComment = $("#content").find(".replies.categorizeus"+message.id);
			newComment.append(appliedTemplate);
			var newCommentView = $("#content").find(".comment.categorizeus"+replyId);
			displayMessageComments(threadMessages[replyId], newCommentView);//DANGER infinite loop possible

		}
	}
}
var displayFullMessage = function(message){
	console.log("View " + JSON.stringify(message));
	var appliedTemplate = $(tmplFullMessage(message));
	var newFullMessage = $("#content").append(appliedTemplate);
	var newMessageView = $("#content").find(".fullMessage.categorizeus"+message.id);
	newMessageView.find(".closeButton").click((function(message, messageView){
			return function(event){
				messageView.remove();
			};
	})(message, newMessageView));
	displayMessageComments(message, newMessageView);
}

var displayRegisterForm = function(container){ //#TODO hey we are seeing a template pattern here, let's generalize it
	var controls = $(container).html(tmplRegister({}));
	controls.find(".btnRegister").click(dynamicRegister(controls));
}
var displayMessageThread = function(err, messageThread){
	currentThread = messageThread;
	threadRelations = {};
	threadMessages = {};
	for(var message of currentThread.thread){
		threadMessages[message.id] = message;
	}
	for(var message of currentThread.relatedMessages){
		threadMessages[message.id] = message;
	}
	for(var relation of currentThread.relations){
		console.log(relation);
		if(threadRelations[relation.sink.id]==null){//TODO source/sink vocab here is iffy at best
			threadRelations[relation.sink.id] = [];
		}
		threadRelations[relation.sink.id].push(relation.source.id);//this is obviously assuming one predicate a.t.m.
	}
	displayMessages(err, currentThread.thread);
}
var displayMessages = function(err, messages){
	$("#content").empty();
	for(var i=0; i<messages.length;i++){

		var appliedTemplate = $(tmplBasicDocument(messages[i]));
		var newMessage = $("#content").append(appliedTemplate);

		appliedTemplate.bind('click',
		   (function(template, message){ 
			return function(event){
				if(event.target.tagName != "IMG" && event.target.tagName != "INPUT"){
					console.log(message);
					template.toggleClass('selected');
				}

			}
		   })(appliedTemplate, messages[i])
		);
		var newMessageView = $("#content").find(".basicDocument.categorizeus"+messages[i].id);
		newMessageView.find(".viewButton").click((function(message){
			return function(event){
				displayFullMessage(message);
			};
		})(messages[i]));
	}
	$("#content").append($(tmplNavigation({})));
	$("#content").find(".nextLink").click(function(event){
		if(currentThread!=null && currentThread.thread!=null && currentThread.thread.length>0){
			console.log("Next Link Click, let's look after : " + currentThread.thread[currentThread.thread.length-1].id);
			currentThread.searchCriteria.startingId = currentThread.thread[currentThread.thread.length-1].id;
			currentThread.searchCriteria.reverse = false;
			searchThreadCriteria(currentThread.searchCriteria, displayMessageThread);
		}else{
			alert("Next Link Click, not sure what to do");
		}
	});
	$("#content").find(".previousLink").click(function(event){

		var startingId = 10;//TODO think about when the if block could fall through to this
		if(currentThread!=null && currentThread.thread!=null && currentThread.thread.length>0){
			startingId = currentThread.thread[0].id;
		}else{
			if(currentThread!=null && !currentThread.searchCriteria.reverse && currentThread.searchCriteria.startingId!=null){
				startingId = currentThread.searchCriteria.startingId;						
			}
		}
		console.log("Previous Link Click " + startingId);
		currentThread.searchCriteria.startingId = startingId;
		currentThread.searchCriteria.reverse = true;
		searchThreadCriteria(currentThread.searchCriteria, displayMessageThread);
	});
};
var dynamicLogin = function(el){
	return function(){
		var username = el.find(".txtUsername").val();
		var password = el.find(".txtPassword").val();
		loginUser(username, password, function(err, user){
			if(err!=null){
				$("#status").html("<h1>"+err+"</h1>");
			}else{
				currentUser = user;
				$("#btnShowLogin").prop("value", "logout");
				$("#status").html("<h1>Logged in successfully, welcome! " + currentUser.userName+"</h1>");
			}
			el.remove();
		});
	};
}

var dynamicRegister = function(el){
	return function(){
		var username = el.find(".txtUsername").val();
		var password = el.find(".txtPassword").val();
		registerUser(username, password,function(err, user){
			if(err!=null){
				$("#status").html("<h1>"+err+"</h1>");
			}else{
				$("#status").html("<h1>Registered successfully, welcome!</h1>");
			}
		});
	};
}

var dynamicEditSubmit = function(el){

	return function(){
		console.log("Dynamically bound control OK");
		var title = el.find(".inputMsgTitle").val();
		var body = el.find(".inputMsgBody").val();
		var id = el.find(".inputMsgId").val();
		var file = el.find(".inputFileAttachment");
		var isNew = (!id) || id.length==0;
		console.log(id + " is new? " + isNew + " " + title + " & " + body);
		if(isNew){
			var newMessage = {
				body:body,
				title:title
			};
			if(file.val()!==''){//file[0].files.length?
				console.log("Found an attached file");
				console.log(file[0].files);
				uploadMessage(newMessage, file[0].files, function(err, response){
					if(err!=null){
						$("#status").append("<p>Error: " + err + "</p>");
					}else{
						$("#status").append("<p>Created new document with id " + response + "</p>");
					}
				});
				return;
			}
			createMessage(newMessage, function(err, response){
				if(err!=null){
					$("#status").append("<p>Error: " + err + "</p>");
				}else{
					$("#status").append("<p>Created new document with id " + response + "</p>");
				}
			});

		}else{
			$("#status").append("<p>Currently, editing existing docs not supported. Clear and try again.</p>");
		}

	};
}
