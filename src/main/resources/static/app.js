var seed = null;
var token = null;
var xmlhttp = new XMLHttpRequest();
xmlhttp.open("GET", "build", false);
xmlhttp.send();
if (xmlhttp.status == 200) {
	seed = xmlhttp.responseText;
}
console.log(seed);

const delay = ms => {
	return new Promise((resolve, reject) => setTimeout(resolve, ms))
};

class Decoder {
	constructor(buffer) {
		this.buffer = buffer;
		this.at = 0;
	}

	private_decrypt() {
		this.buffer = private_decrypt_packet(this.buffer);
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
		this.buffer = [];
		this.position = 0;
	}

	private_encrypt() {
		this.buffer = private_encrypt_packet(this.buffer);
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
			this.state += ((c * i) % this.c);
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
const global_encryptor = range;
const global_decryptor = new Array(256);
for (let i = 0; i < 256; i++) {
	let num = range[i];
	global_decryptor[num] = i;
}

var private_encryptor;
var private_decryptor;


function global_encrypt_packet(packet) {
	const new_ = [];
    for (let i = 1; i < packet.length; i++) {
        new_[i] = global_encryptor[packet[i]];
    }
	new_[0] = packet[0];
    return new_;
}

function global_decrypt_packet(packet) {
	const new_ = [];
    for (let i = 1; i < packet.length; i++) {
        new_[i] = global_decryptor[packet[i]];
    }
	new_[0] = packet[0];
    return new_;
}

function private_encrypt_packet(packet) {
	const new_ = [];
	new_[0] = packet[0];
    for (let i = 1; i < packet.length; i++) {
        new_[i] = private_encryptor[packet[i]];
    }
    return new_;
}

function private_decrypt_packet(packet) {
	const new_ = [];
	new_[0] = packet[0];
    for (let i = 1; i < packet.length; i++) {
        new_[i] = private_decryptor[packet[i]];
    }
    return new_;
}



class User {
	id;
	name;
}

class Group {
	id; 
	name;
	members = {};
}

class WSHandler {
	constructor() {
		this.users = {};
		this.groups = {};
		this.stompClient = new StompJs.Client({
			brokerURL: 'ws://localhost:8080/websocket'
		});
		
		this.stompClient.onConnect = (frame) => {
			console.log('Connected!!!');
			this.stompClient.subscribe('/user/queue/listen', (packet) => {
				packet = Array.from(packet._binaryBody);
				this.parse_packet(global_decrypt_packet(packet));
			});
		};
		
		this.stompClient.onWebSocketError = (error) => {
			console.error('Error with websocket', error);
		};
		
		this.stompClient.onStompError = (frame) => {
			console.error('Broker reported error: ' + frame.headers['message']);
			console.error('Additional details: ' + frame.body);
		};
		console.log("connect attempt...");
		this.stompClient.activate();
	}


	/*
	packet structure
	(int 0 or 1) global or private decrypt | serverbound (int): 
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


    (int 0 or 1) global or private decrypt | clientbound (int):
        1: recieve user token
            (string) user token 

        2: recieve alert
            (string) alert message
        
        3: recieve message
			(int) private or group message:
				1:
					(string) sender ID | (string) sender name | (string) message
				2: 
            		(string) group ID | (string) sender ID | (string) message
            
		4: group updates
				(string) group ID | (string)  group name | (int) # of updates | repeat 
					(int) add or remove 
						0: add	
							(string) user ID | (string) user name
						1: remove 
							(string) user ID


    */
	parse_packet(packet) {
		const decoder = new Decoder(packet);
		const private_ = decoder.getInt();
		if (private_) {
			decoder.private_decrypt();
		}
		const header = decoder.getInt();
		var type;
		console.log("recieved: ", packet);
		console.log(private_, header);
		switch (header) {
			case 1: // get token
				token = decoder.getString();
				console.log(token);

				let p_generator = new RandomNumberGenerator(token);
				let p_range = new Array(256);
				for (let i = 0; i < 256; i++) {
					p_range[i] = i;
				}
				for (let i = 0; i < 1000; i++) {
					let pos_1 = p_generator.nextRange(1, 256);
					let pos_2 = p_generator.nextRange(1, 256);
					let int_1 = p_range[pos_1];
					let int_2 = p_range[pos_2];

					p_range[pos_2] = int_1;
					p_range[pos_1] = int_2;
				}
				private_encryptor = p_range;
				private_decryptor = new Array(256);
				for (let i = 0; i < 256; i++) {
					let num = p_range[i];
					private_decryptor[num] = i;
				}

				// get group ids and names
				// call ui
				break;

			case 2: // alert
				var alert = decoder.getString();
				console.log("alert: ", alert);
				// alert window with message
				// call ui
				break;

			case 3:
				type = decoder.getInt();
				switch (type) {
					case 1: // private message
						var sender_id = decoder.getString();
						var sender_name = decoder.getString();
						var message = decoder.getString();
						console.log("private message: ", sender_id, sender_name, message);
						// call ui
						break;
					case 2: // group message
						var group_id = decoder.getString();
						var sender_id = decoder.getString();
						var message = decoder.getString();
						console.log("group message: ", group_id, sender_id, message);
						// call ui
						break;
				}
				
				break;

			case 4:
				var group_id = decoder.getString();
				var group_name = decoder.getString();
				var updates = decoder.getInt();
				var user_id;
				var user_name;
				for (let i = 0; i < updates; i++) {
					var action = decoder.getInt();
					sw: switch (action) {
						case 0: // add user to group
							user_id = decoder.getString();
							user_name = decoder.getString();
							console.log("add user: " + group_id, group_name, updates, user_id, user_name);
							break sw;
							// call ui
						case 1: // delete user from group
							user_id = decoder.getString();
							console.log("remove user: " + group_id, group_name, updates, user_id);
							break sw;
							// call ui
					}
					
				}
				
				break;

			
		}
	}

	
	disconnect() {
		this.stompClient.deactivate();
		console.log("Disconnected");
	}

	send(packet) {
		console.log("Sent: ", packet);
		const encrypted = global_encrypt_packet(packet);
		this.stompClient.publish({
			destination: "/app/endpoint",
			binaryBody: encrypted
		});
	}
}

class UIHandler {
	constructor() {
		this.ws = new WSHandler();

	}

	register(username) {
		const encoder = new Encoder();
		encoder.addInt(0);
		encoder.addInt(1);
		encoder.addString(username);
		this.ws.send(encoder.finish());
	}

	send_private_message(recipient_id, message) {
		const encoder = new Encoder();
		encoder.addInt(1);
		encoder.addInt(3);
		encoder.addString(token);
		encoder.addInt(1);
		encoder.addString(recipient_id);
		encoder.addString(message);
		
		this.ws.send(private_encrypt_packet(encoder.finish()));
	}

	send_group_message(recipient_id, message) {
		const encoder = new Encoder();
		encoder.addInt(1);
		encoder.addInt(3);
		encoder.addString(token);
		encoder.addInt(2);
		encoder.addString(recipient_id);
		encoder.addString(message);
		this.ws.send(private_encrypt_packet(encoder.finish()));
	}

	create_group(name, password) {
		const encoder = new Encoder();
		encoder.addInt(1);
		encoder.addInt(4);
		encoder.addString(token);
		encoder.addString(name);
		encoder.addString(password);
		this.ws.send(private_encrypt_packet(encoder.finish()));
	}

	/*5: join/leave group
			(string) user token | (string) group ID | (int) join/leave
			1: join
				 (string) group password
			2: leave*/
	join_group(group_id, password) {
		const encoder = new Encoder();
		encoder.addInt(1);
		encoder.addInt(5);
		encoder.addString(token);
		encoder.addString(group_id);
		encoder.addInt(1);
		encoder.addString(password);
		this.ws.send(private_encrypt_packet(encoder.finish()));
	}

	leave_group(group_id) {
		const encoder = new Encoder();
		encoder.addInt(1);
		encoder.addInt(5);
		encoder.addString(token);
		encoder.addString(group_id);
		encoder.addInt(2);
		this.ws.send(private_encrypt_packet(encoder.finish()));
	}

}

const ui = new UIHandler();









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