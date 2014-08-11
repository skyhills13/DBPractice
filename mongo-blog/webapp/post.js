var updateButton = document.querySelector(".update-button");
var deleteButton = document.querySelector(".delete-button");

updateButton.addEventListener("click", updatePost);
deleteButton.addEventListener("click", deletePost);


function xhr(requestData, method, url, callback){
	var request = new XMLHttpRequest();
	request.open(method, url, true );
	request.onload = function(){
		callback(JSON.parse(request.responseText));
	}
	request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
	request.send(requestData);
}

function updatePost(e) {
	var updateDivId = e.target.parentNode.id;
	var requestData = JSON.stringify({id: updatedDivId, title: updatedTitle, body: updatedBody}); 
	
	//완료, 취소 버튼을 놓고, 완료 누를경우
	var updatedTitle = document.querySelector(".post-title").value;
	var updatedBody = document.querySelector(".post-content").value;
	var callback = function() {
		window.location = "localhost:3000";
	};
	xhr(requestData, "POST", "http://10.73.45.54/update", callback);
}

function deletePost(e) {
	var updateDiv = e.target.parentNode;
	var callback = function(){
		updateDiv.parentNode.removeChild(updateDiv);
	};
	xhr(updateDiv.id, DELETE, "http://localhost:3000" ,callback);
}