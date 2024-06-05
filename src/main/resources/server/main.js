// HTTP_PORT=8282 UDP_PORT=12345 UDP_ADDRESS=127.0.0.1 DEBUG=1 node main.js

var express = require('express')();
var http = require('http').Server(express);
var io = require('socket.io')(http);
var dgram = require('dgram');
var morgan  = require('morgan');
var fs = require('fs');

// Parsing environment variables
var httpPort=process.env.HTTP_PORT || 8181;
var udpPort=process.env.UDP_PORT || 10110;
var udpAddress=process.env.UDP_ADDRESS || "127.0.0.1";
var debug=process.env.DEBUG || false;

console.log("DEBUG is: "+ (debug ? "on" : "off"));

// Setup UDP
var udp = dgram.createSocket("udp4");

udp.on("error", function (err) {
  console.error("UDP server error:\n" + err.stack);
  udp.close();
});

udp.on("message", function (msg, rinfo) {
  if (debug){
	console.log("UDP ["+rinfo.address + ":" + rinfo.port+"]: " + msg);
  }
  io.emit('update-msg', { data: ""+msg});
});

udp.on("listening", function () {
  var address = udp.address();
  console.log("UDP server listening on " + address.address + ":" + address.port);
});

udp.bind(udpPort,udpAddress);

// Setup Express 

// - Setup the HTTP logger
var accessLogStream = fs.createWriteStream(__dirname + '/logs/access.log', {flags: 'a'});
express.use(morgan('combined', {stream: accessLogStream}));

express.get('/', function(req, res){
  res.sendFile(__dirname + '/static/index.html');
});

express.get('/ships.js', function(req, res){
	  res.sendFile(__dirname + '/static/ships.js');
});

express.get('/style.css', function(req, res){
	  res.sendFile(__dirname + '/static/style.css');
});

express.get(/^\/openlayers\/(.*)$/, function(req, res, next){
  var filePath=__dirname + '/static/openlayers/OpenLayers-2.13.1/'+req.params[0];
  
  fs.stat(filePath, function(err, stats){
	  if (err==null && stats.isFile()){
		  res.sendFile(filePath);
	  } else {
		  console.log("Requested non existing file: "+filePath);
		  res.status(404).send('Not found');
	  }
  });  
});

express.get(/^\/images\/(.*)$/, function(req, res){
	  var filePath=__dirname + '/static/images/'+req.params[0];
	  
	  fs.stat(filePath, function(err, stats){
		  if (err==null && stats.isFile()){
			  res.sendFile(filePath);
		  } else {
			  console.log("Requested non existing file: "+filePath);
			  res.status(404).send('Not found');
		  }
	  });  
});

express.get(/^\/audio\/(.*)$/, function(req, res, next){
	  var filePath=__dirname + '/cache/audio/'+req.params[0];
	  
	  fs.stat(filePath, function(err, stats){
		  if (err==null && stats.isFile()){
			  res.sendFile(filePath);
		  } else {
			  console.log("Requested non existing file: "+filePath);
			  res.status(404).send('Not found');
		  }
	  });  
});

udp.on("error", function (err) {
  console.error("UDP server error:\n" + err.stack);
  udp.close();
});

// Setup HTTP server
http.on("error", function (err) {
  console.error("HTTP server error:\n" + err.stack);
  http.close();
});

http.listen(httpPort, function(){
	if (debug){
	  var address = http.address();
	  console.log("HTTP server listening on "+ address.address + ":" + address.port);
	}
});


