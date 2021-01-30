class Message {
    constructor(type, timestamp, sender, payload) {
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
        this.payload = payload;
    }
}

class ChatMessage {
    static TYPE = 'CHAT';
    constructor(receiver, text) {
        this.receiver = receiver;
        this.text = text;
    }
}

class ParticipantMessage {
    static TYPE = 'PARTICIPANT';
    static ACTION_JOIN = 'JOIN';
    static ACTION_LEAVE = 'LEAVE';

    constructor(action, user) {
        this.action = action;
        this.user = user;
    }
}

var username;

var clientWebSocket;

var protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
var host = window.location.host;
var pathname = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/') + 1);
var endpoint = 'chat';
var url = protocol + host + pathname + endpoint;

document.getElementById('login-form').addEventListener('submit', function(event) {
    var loginform = document.getElementById('login-form');
    var logoutform = document.getElementById('logout-form');
    var chatform = document.getElementById('chat-form');
    var welcomeIcon = document.getElementById('welcome-icon');
    var welcomeText = document.getElementById('welcome-text');
    loginform.style.display='none';
    logoutform.style.display='block';
    chatform.style.display='grid';
    chatform.msg.focus();
    username = loginform.username.value;
    welcomeIcon.innerHTML='(^̮^)';
    welcomeText.innerHTML='Welcome, ' + username;
    clientWebSocket = new WebSocket(url);
    clientWebSocket.onmessage = function(conn) {
        handleMsg(JSON.parse(conn.data));
    }
    clientWebSocket.onopen = function() {
        var payload = new ParticipantMessage(ParticipantMessage.ACTION_JOIN, {username:username});
        var msg = new Message(ParticipantMessage.TYPE, Date.now(), username, JSON.stringify(payload));
        clientWebSocket.send(JSON.stringify(msg));
    }
    event.preventDefault();
    return false;
}, false);

function handleMsg(msg) {
    var payload = JSON.parse(msg.payload);
    var chatform = document.getElementById('chat-form');
    if (msg.type === ChatMessage.TYPE) {
        chatform.chat.value += '\n['+ new Date(msg.timestamp).toLocaleString() + '] ' + getParticipantName(msg.sender) + ': ' + payload.text;
    } else if (msg.type === ParticipantMessage.TYPE) {
        var recipientOptions = '';
        for (var key in payload.user) {
          recipientOptions+='<option value="'+key+'">'+payload.user[key]+'</option>';
        }
        if (payload.action === ParticipantMessage.ACTION_JOIN){
            document.getElementById('recipient').innerHTML = recipientOptions;
            chatform.chat.value += '\n['+ new Date(msg.timestamp).toLocaleString() + '] ' + getParticipantName(msg.sender) + ' has joined the chat';
        } else if (payload.action === ParticipantMessage.ACTION_LEAVE){
            chatform.chat.value += '\n['+ new Date(msg.timestamp).toLocaleString() + '] ' + getParticipantName(msg.sender) + ' has left the chat';
            document.getElementById('recipient').innerHTML = recipientOptions;
        }
    }
    chatform.chat.scrollTop = chatform.chat.scrollHeight;
}

function getParticipantName(participantId){
    var recipient = document.getElementById('recipient');
    for (var i = 0; i < recipient.length; i++){
        var option = recipient.options[i];
        if (option.value === participantId){
            return option.text;
        }
    }
}

document.getElementById('logout-form').onsubmit = function(event) {
    var payload = new ParticipantMessage(ParticipantMessage.ACTION_LEAVE, {username:username});
    var msg = new Message(ParticipantMessage.TYPE, Date.now(), username, JSON.stringify(payload));
    clientWebSocket.send(JSON.stringify(msg));
    clientWebSocket.close();
    location.reload();
}
window.onbeforeunload = function(e) { leave(); };

document.getElementById('chat-form').onsubmit = function(event) {
    var chatform = document.getElementById('chat-form');
    var payload = new ChatMessage(chatform.recipient.value, chatform.msg.value);
    var msg = new Message(ChatMessage.TYPE, Date.now(), username, JSON.stringify(payload));
    clientWebSocket.send(JSON.stringify(msg));
    chatform.msg.value = '';
    chatform.msg.focus();
    event.preventDefault();
    return false;
}