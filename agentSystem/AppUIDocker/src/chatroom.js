//
var messagesTableBody = document.getElementById('messagesTableBody');
var thinkingRow = document.createElement('tr');
thinkingRow.setAttribute('id', 'thinking');
thinkingRow.innerHTML = '<td><p class=\"thinking-msg\">thinking...</p></td>' +
    '<td></td>';

function getTime() {
    var now = new Date();
    var hours = now.getHours();
    hours = hours < 10 ? '0' + hours : hours;
    var minutes = now.getMinutes();
    minutes = minutes < 10 ? '0' + minutes : minutes;
    var seconds = now.getSeconds();
    seconds = seconds < 10 ? '0' + seconds : seconds;
    var time = hours + ":" + minutes + ":" + seconds;
    return time;
}

function sendMessage() {
    var myMessageRow = document.createElement('tr');
    var myMessage = document.getElementById('myMessage').value;
    myMessageRow.innerHTML = '<td><p class=\"my-msg\">' + myMessage + '</p></td>' +
        '<td>' + getTime() + '</td>';
    messagesTableBody.appendChild(myMessageRow);
    messagesTableBody.appendChild(thinkingRow);
    document.getElementById('myMessage').value = "";

    userAction(myMessage);
}

//uri = "http://karaf-agent/cxf/ai/ask"; uri = "http://localhost/cxf/ai/ask";
uri = "http://localhost:8182/cxf/ai/ask";

// Make a POST call
const userAction = async (myMessage) => {
    const response = await fetch(uri, {
        method: 'POST',
        body: myMessage, // string or object
        headers: {
            'Content-Type': 'text/plain'
        }
    });
    const myReply = await response.text();
    messagesTableBody.removeChild(thinkingRow);
    var agentMessageRow = document.createElement('tr');
    agentMessageRow.innerHTML = '<td><p class=\"agent-msg\">' + myReply + '</p></td>' +
        '<td>' + getTime() + '</td>';
    messagesTableBody.appendChild(agentMessageRow);
}
