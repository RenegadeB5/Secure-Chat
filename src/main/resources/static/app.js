var seed = null;
var token = null;
var xmlhttp = new XMLHttpRequest();
xmlhttp.open("GET", "build", false);
xmlhttp.send();
if (xmlhttp.status == 200) {
	seed = xmlhttp.responseText;
}
console.log(seed);

class Decoder {
	constructor(buffer) {
		this.buffer = new Uint8Array(buffer);
		this.at = 0;
	}

	getInt() {
		return this.buffer[this.at++];
	}

	getString() {
		const length = this.buffer[this.at++];
		let s = '';
		for (let i = 0; i < length; i++) {
			s += String.fromCharCode(this.buffer[this.at++]);
		}
		return s;
	}
}

class Encoder {
	constructor() {
		this.buffer = new Uint8Array();
		this.position = 0;
	}

	getPosition() {
		return this.position;
	}

	addInt(i) {
		this.buffer[this.position++] = i;
	}

	addString(s) {
		this.buffer[this.position++] = s.length;
		for (let i = 0; i < s.length; i++) {
			this.buffer[this.position++] = s.charCodeAt(i);
		}
	}

	finish() {
		return this.buffer;
	}
}

class RandomNumberGenerator {
	constructor(seed) {
		this.m = 0x80000000;
		this.a = 1103515245;
		this.c = 12345;
		this.state = 0;

		for (let i = 0; i < seed.length; i++) {
			const c = seed.charCodeAt(i);
			this.state += c * i;
		}
	}

	nextInt() {
		this.state = (this.a * this.state + this.c) % this.m;
		return Math.floor(this.state);
	}

	nextRange(start, end) {
		const range = end - start;
		return start + Math.floor(this.nextFloat() * range);
	}

	nextFloat() {
		return this.nextInt() / (this.m - 1);
	}
}


let generator = new RandomNumberGenerator(seed);
let range = new Array(256);
for (let i = 0; i < 256; i++) {
	range[i] = i;
}
for (let i = 0; i < 1000; i++) {
	let pos_1 = generator.nextRange(1, 256);
	let pos_2 = generator.nextRange(1, 256);
	let int_1 = range[pos_1];
	let int_2 = range[pos_2];

	range[pos_2] = int_1;
	range[pos_1] = int_2;
}
const encryptor = range;
const decryptor = new Array(256);
for (let i = 0; i < 256; i++) {
	let num = range[i];
	decryptor[num] = i;
}


function encrypt_packet(packet) {
    for (let i = 0; i < packet.length; i++) {
        packet[i] = encryptor[packet[i]];
    }
    return packet;
}

function decrypt_packet(packet) {
    for (let i = 0; i < packet.length; i++) {
        packet[i] = decryptor[packet[i]];
    }
    return packet;
}

class UIHandler {
	constructor() {

	}

	
}

class WSHandler {
	constructor() {
		this.stompClient = new StompJs.Client({
			brokerURL: 'ws://localhost:8080/websocket'
		});
		
		this.stompClient.onConnect = (frame) => {
			setConnected(true);
			console.log('Connected: ' + frame);
			stompClient.subscribe('/api/listen', (packet) => {
				console.log(JSON.parse(packet.body));
				this.parse_packet(decrypt_message(JSON.parse(packet.body)));
			});
		};
		
		this.stompClient.onWebSocketError = (error) => {
			console.error('Error with websocket', error);
		};
		
		this.stompClient.onStompError = (frame) => {
			console.error('Broker reported error: ' + frame.headers['message']);
			console.error('Additional details: ' + frame.body);
		};
	}

	parse_packet(packet) {
		const decoder = new Decoder(packet);
		const header = decoder.getInt();
		switch (header) {

		}
	}

	connect() {
		this.stompClient.activate();
	}
	
	disconnect() {
		this.stompClient.deactivate();
		console.log("Disconnected");
	}

	send(packet) {
		this.stompClient.publish({
			destination: "/api/endpoint",
			binaryBody: new Uint8Array(encrypt_packet(packet))
		});
	}
}


/*
	packet structure
	serverbound (int): 
		1: register 
			string username
		2: authenticate

		3: send message 
			(string) user token | (int) dm(1) or gm(2)  | (string) recipient ID | (string) message

		4: create group
            (string) user token | (string) name | (string) group password
		
		5: join/leave group
			(string) user token | (string) group ID | (int) join/leave
			1: join
				 (string) group password
			2: leave


    clientbound (int):
        1: recieve user info
            (string) user token |
            (int) # of groups (for each) | (string group ID) (String group name) |
            (int) # of dms (for each) | (string user ID) (String user name) |
            * maybe send messages for each *

        2: recieve alert
            (string) alert message
        
        3: recieve message
            (string) sender ID | (string) group ID | (string) message
            

    */




/*const stompClient = new StompJs.Client({
	brokerURL: 'ws://localhost:8080/websocket'
});

stompClient.onConnect = (frame) => {
	setConnected(true);
	console.log('Connected: ' + frame);
	stompClient.subscribe('/api/listen', (message) => {
		console.log(JSON.parse(message.body));
		showMessage(JSON.parse(message.body).content.content);
	});
};

stompClient.onWebSocketError = (error) => {
	console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
	console.error('Broker reported error: ' + frame.headers['message']);
	console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
	if (connected) {
		$("#conversation").show();
	} else {
		$("#conversation").hide();
	}
	$("#messages").html("");
}

function connect() {
	stompClient.activate();
}

function disconnect() {
	stompClient.deactivate();
	setConnected(false);
	console.log("Disconnected");
}

function sendName() {
	stompClient.publish({
		destination: "/api/endpoint",
		binaryBody: new Uint8Array([1])
	});
}

function showMessage(message) {
	$("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function() {
	$("form").on('submit', (e) => e.preventDefault());
	$("#connect").click(() => connect());
	$("#disconnect").click(() => disconnect());
	$("#send").click(() => sendName());
});*/