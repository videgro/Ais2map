function pad(n, width, z) {
	z = z || '0';
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

function timestamp2prettyPrint(timestamp) {
	var a = new Date(timestamp);
	var months = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug','Sep', 'Oct', 'Nov', 'Dec' ];
	var year = a.getFullYear();
	var month = months[a.getMonth()];
	var date = a.getDate();
	var hour = pad(a.getHours(), 2);
	var min = pad(a.getMinutes(), 2);
	var sec = pad(a.getSeconds(), 2);
	var time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec;
	return time;
}

function addShip(ship) {
	// console.log(ship);

	ship.lastUpdated=new Date().getTime(); // Add extra field
	ships[mmsiKey(ship)]=ship;
	
	// Create new marker
	var lonLat = new OpenLayers.LonLat(ship.lon, ship.lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());

	var popupText = "<table>";
	popupText += "<tr><td><h3>Name:</h3></td><td><h3>" + ship.name+ "</h3></td></tr>";
	popupText += "<tr><td>MMSI:</td><td><a href='http://www.marinetraffic.com/en/ais/details/ships/mmsi:"+ship.mmsi+"' target='_blank'>" + ship.mmsi + "</a></td></tr>";
	popupText += "<tr><td>Callsign:</td><td>" + ship.callsign + "</td></tr>";
	popupText += "<tr><td>Ship type:</td><td>" + ship.shipType + "</td></tr>";
	popupText += "<tr><td>Destination:</td><td>" + ship.dest + "</td></tr>";
	popupText += "<tr><td>Nav. status:</td><td>" + ship.navStatus+ "</td></tr>";
	popupText += "<tr><td>Speed:</td><td>" + ship.sog + "</td></tr>";
	popupText += "<tr><td>Heading:</td><td>" + ship.heading + "</td></tr>";
	popupText += "<tr><td>Course:</td><td>" + (ship.cog / 10).toFixed(1)+ "</td></tr>";
	popupText += "<tr><td><h3>Position</h3></td><td/></tr>";
	popupText += "<tr><td> - Latitude:</td><td>" + ship.lat + "</td></tr>";
	popupText += "<tr><td> - Longitude:</td><td>" + ship.lon + "</td></tr>";
	popupText += "<tr><td><h3>Dimensions</h3></td><td/></td></tr>";
	popupText += "<tr><td> - Bow:</td><td>" + ship.dimBow + "</td></tr>";
	popupText += "<tr><td> - Port:</td><td>" + ship.dimPort + "</td></tr>";
	popupText += "<tr><td> - Starboard:</td><td>" + ship.dimStarboard+ "</td></tr>";
	popupText += "<tr><td> - Stern:</td><td>" + ship.dimStern + "</td></tr>";
	popupText += "<tr><td>Time:</td><td>"+ timestamp2prettyPrint(ship.timestamp) + " ("	+ (new Date().getTime() - ship.timestamp) + ")</td></tr>";
	popupText += "</table>";

	// "rot":
	// specialManIndicator
	// subMessage
	// "draught":0,

	var angle = Math.round(ship.cog / 10);
	var origin = new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat);
	var name=(ship.name=="")?ship.mmsi:ship.name;

	// Default: 55 x 5 m
	var width=   ((ship.dimBow!="" && ship.dimStern!="")?parseInt(ship.dimBow)+parseInt(ship.dimStern):55)/10*shipScaleFactor; // = real length
	var height=  ((ship.dimStarboard!="" && ship.dimPort!="")?parseInt(ship.dimStarboard)+parseInt(ship.dimPort):5)*shipScaleFactor; // = real width

     /*
     Strings copied from: dk.dma.ais.message.ShipTypeCargo
     */
	var shipIcon=DEFAULT_SHIP_ICON;
	switch (ship.shipType){
	    case "CARGO":
    	case "TANKER":
            shipIcon="container-ship-top.png";
        break;
	    case "PILOT":
	    case "SAR":
	    case "PORT_TENDER":
	    case "LAW_ENFORCEMENT":
	    case "TOWING":
        case "TOWING_LONG_WIDE":
        case "TUG":
	        shipIcon="basic_red.png";
	    break;
	    case "MILITARY":
	        shipIcon="basic_green.png";
        break;
	    case "SAILING":
            shipIcon="sailing-yacht_1.png";
        break;
        case "PLEASURE":
            shipIcon="yacht_4.png";
        break;
	    case "PASSENGER":
	        shipIcon="passenger.png";
	    break;
	    case "FISHING":
	        shipIcon="basic_blue.png";
	    break;
	    case "UNKNOWN":
	        shipIcon="basic_yellow.png";
	    break;
	    case "WIG":
	    case "ANTI_POLLUTION":
	    case "MEDICAL":
	    case "DREDGING":
	    case "DIVING":
	    case "HSC":
	    case "SHIPS_ACCORDING_TO_RR":
	    case "UNDEFINED":
	    default:
	        shipIcon=DEFAULT_SHIP_ICON;
	    break;
	}

	var shipFeature1 = new OpenLayers.Feature.Vector(
			// +90 degrees because icon is pointing to the left instead of top
			origin, {
				angle : angle + 90,
				opacity : 100,
				name : name,
				width: width,
				height: height,
				fontColor: 0,
				shipIcon: shipIcon,
				message : popupText
	})

	var pts = new Array(origin, new OpenLayers.Geometry.Point(lonLat.lon,lonLat.lat + ((1 + ship.sog) * speedfactor)));

	var shipFeature2 = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(pts), {name : ""	});
	// Rotation angle in degrees (measured counterclockwise from the positive x-axis)
	shipFeature2.geometry.rotate(angle * -1, origin);

	var shipFeatures = [ shipFeature2, shipFeature1 ];
	shipVectors.addFeatures(shipFeatures);

	// We know this ship already. Put marker at new position and draw trace
	var previousMarkers = markers[mmsiKey(ship)];
	if (previousMarkers != null) {

		// Remove previous marker
		shipVectors.removeFeatures(previousMarkers);

		// Create trace line from previous to new position
		var points = new Array(previousMarkers[0].geometry.getCentroid(),new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat));

		var line = new OpenLayers.Geometry.LineString(points);

		var strokeColor = pad(ship.mmsi, 6);
		var strokeColor = strokeColor.substring(strokeColor.length - 7,	strokeColor.length - 1);
		// console.log("strokeColor: "+strokeColor);

		var lineFeature = new OpenLayers.Feature.Vector(line, null,{
			strokeColor : '#' + strokeColor,
			strokeOpacity : 0.5,
			strokeWidth : 2
		});
		
		lineLayer.addFeatures([ lineFeature ]);
		traces[new Date().getTime()] = lineFeature;
	}

	if (ship.audioAvailable	&& (typeof shipsNamePlayed[mmsiKey(ship)] === 'undefined')) {
		// console.log("Play sound");

		var gongListener = function(event) {
			audioName.src = "audio/" + mmsiKey(ship) + ".wav";
			//console.log("Play: " + audioName.src);
			audioName.play();
			document.querySelector("#audioGong").removeEventListener("ended",gongListener);
		}

		document.querySelector("#audioGong").addEventListener("ended",gongListener);
		audioGong.play();
		shipsNamePlayed[mmsiKey(ship)] = true;
	}

	// Replace/add new marker to map of markers
	markers[mmsiKey(ship)] = shipFeatures;

	if (!init) {
		map.setCenter(lonLat, zoom);
		map.zoomToExtent(shipVectors.getDataExtent());
		init = true;
	}
}

function cleanup() {
	// Remove after 20 minutes:
	// - Old markers (ship+speed)
	// - Old traces

	var now = new Date().getTime();

	// Remove ships
	for (keyMmsi in ships) {
	    if (ships.hasOwnProperty(keyMmsi)) {	
	    		var ship=ships[keyMmsi];
	    		var timestamp=ship.lastUpdated;
	    		
	    		var age=(now-timestamp);
	    		
	    		if (age>(maxAge/2)){
	    			// Indicating ship will be removed in the (near) future
	    			// Change label color on second feature (index 1)
		    		markers[keyMmsi][1].attributes.fontColor='#FF0000';	   
		    		shipVectors.redraw();
		    		
		    		if (age>maxAge){
		    			//console.log("Removing ship: "+ship.mmsi+" ("+ship.name+"), Timestamp: "+timestamp+" (Age: "+age+")");
		    			
		    			// Remove ship markers
		    			shipVectors.removeFeatures(markers[keyMmsi]);
		    			
		    			// Remove ship from administration	    			
		    			delete markers[keyMmsi];
		    			delete shipsNamePlayed[keyMmsi];
		    			delete ships[keyMmsi];
		    			
		    			printStatistics();
		    		}
	    		}
	    }
	}	
	  
    // ^0$|^[1-9]\d*$/.test(keyTimestamp) &&    
    // keyTimestamp <= 4294967294          
	
	// Remove traces
	for (keyTimestamp in traces) {
	    if (traces.hasOwnProperty(keyTimestamp)){	
	    		if ((now-keyTimestamp)>maxAge){
	    			//console.log("Removing trace - Timestamp: "+keyTimestamp+" (Age: "+(now-keyTimestamp)+")");
	    			
	    			// Remove trace
	    			lineLayer.removeFeatures(traces[keyTimestamp]);
	    			
	    			// Remove trace from administration
	    			delete traces[keyTimestamp];	    			
	    		}
	    	}
	}
}

function printStatistics(){
	console.log("# Ships: "+Object.keys(ships).length);
	console.log("# Markers: "+Object.keys(markers).length);
	console.log("# Ships (name said): "+Object.keys(shipsNamePlayed).length);	
	console.log("# Traces: "+Object.keys(traces).length);
}

function getSeaMapTileURL(bounds) {
	var res = this.map.getResolution();
	var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
	var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
	var z = this.map.getZoom();
	var limit = Math.pow(2, z);
	if (y < 0 || y >= limit) {
		return null;
	} else {
		x = ((x % limit) + limit) % limit;
		url = this.url;
		path = z + "/" + x + "/" + y + "." + this.type;
		if (url instanceof Array) {
			url = this.selectUrl(path, url);
		}
		console.log(url + path);
		return url + path;
	}
}

var mmsiKey = function(obj) {
	// some unique object-dependent key
	return obj.mmsi;
};

function createStyleMapShipSymbol(){
  	styleMapShipSymbol = new OpenLayers.StyleMap({
  		"default" : new OpenLayers.Style({
  			externalGraphic : "images/${shipIcon}",
  			graphicWidth : "${width}",
  			graphicHeight: "${height}",
  			// graphicXOffset: -40,
  			// graphicYOffset: -40,
  			rotation : "${angle}",
  			fillOpacity : "${opacity}",
  			label : "${name}",
  			fontColor : "${fontColor}",
  			fontSize : "12px",
  			fontFamily : "Courier New, monospace",
  			fontWeight : "bold",
  			labelAlign : "left",
  			labelXOffset : "0",
  			labelYOffset : "-50",
  			labelOutlineColor : "white",
  			labelOutlineWidth : 3,
  			strokeColor : "#00FF00",
  			strokeOpacity : 1,
  			strokeWidth : 3,
  			fillColor : "#FF5500"
  		}),
  		
  		"select" : new OpenLayers.Style({
  			cursor : "crosshair",
  		})
  	});
}


/** ************************************************************************************************************************* */

const DEFAULT_SHIP_SCALE_FACTOR=8;
const DEFAULT_SHIP_ICON="basic_turquoise.png";
const DEFAULT_MAX_AGE=(1000*60*20); // 20 minutes

var ships = {};
var shipsNamePlayed = {};
var markers = {};
var traces = {};

var shipScaleFactor=DEFAULT_SHIP_SCALE_FACTOR;
var styleMapShipSymbol;

var maxAge = DEFAULT_MAX_AGE;


createStyleMapShipSymbol();

var map = new OpenLayers.Map("mapdiv", {
	projection : new OpenLayers.Projection("EPSG:900913"),
	displayProjection : new OpenLayers.Projection("EPSG:4326"),
	controls : [ new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.ScaleLine({
				topOutUnits : "nmi",
				bottomOutUnits : "km",
				topInUnits : 'nmi',
				bottomInUnits : 'km',
				maxWidth : '40'
			}), new OpenLayers.Control.LayerSwitcher(),
			new OpenLayers.Control.MousePosition(),
			new OpenLayers.Control.PanZoomBar(),
			new OpenLayers.Control.TouchNavigation()],
	numZoomLevels : 18,
	maxResolution : 156543,
	units : 'meters'
});

var layerOsm = new OpenLayers.Layer.OSM("OpenStreetMap",
// Official OSM tileset as protocol-independent URLs
[ '//a.tile.openstreetmap.org/${z}/${x}/${y}.png',
		'//b.tile.openstreetmap.org/${z}/${x}/${y}.png',
		'//c.tile.openstreetmap.org/${z}/${x}/${y}.png' ], null);

var layerSeamark = new OpenLayers.Layer.TMS("OpenSeaMap",
		"http://tiles.openseamap.org/seamark/", {
			numZoomLevels : 18,
			type : 'png',
			getURL : getSeaMapTileURL,
			isBaseLayer : false,
			displayOutsideMaxExtent : true
		});
map.addLayers([ layerOsm, layerSeamark ]);

var zoom = 18;
var init = false;
var speedfactor = 25;

// Ships
var shipVectors = new OpenLayers.Layer.Vector("Ships", {
	eventListeners : {
		'featureselected' : function(evt) {
			var feature = evt.feature;

			if (typeof feature.attributes.message !== 'undefined' && feature.attributes.message != "") {
				// Must create a popup on ship symbol
				var popup = new OpenLayers.Popup.FramedCloud("popup",OpenLayers.LonLat.fromString(feature.geometry.toShortString()), null,feature.attributes.message, null, true, null);
				popup.autoSize = true;
				popup.maxSize = new OpenLayers.Size(400, 800);
				popup.fixedRelativePosition = true;
				feature.popup = popup;
				map.addPopup(popup);
			}
		},
		'featureunselected' : function(evt) {
			var feature = evt.feature;
			if (feature.popup != null) {
				map.removePopup(feature.popup);
				feature.popup.destroy();
				feature.popup = null;
			}
		}
	},
	styleMap : styleMapShipSymbol
});

map.addLayers([shipVectors]);

var selectControl = new OpenLayers.Control.SelectFeature(shipVectors, {hover : true});
map.addControl(selectControl);
selectControl.activate();

// Traces
var lineLayer = new OpenLayers.Layer.Vector("Ship traces");
map.addLayer(lineLayer);
map.addControl(new OpenLayers.Control.DrawFeature(lineLayer,OpenLayers.Handler.Path));

// Zoom (in/out) ships
var zoomSquared=zoom*zoom;
map.events.register("zoomend", map, function() {
        // http://gis.stackexchange.com/questions/31943/how-to-resize-a-point-on-zoom-out
        //var new_style = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
        var stle=styleMapShipSymbol.styles['default'].defaultStyle;
        stle.pointRadius=((45/zoomSquared)*map.getZoom()*map.getZoom());
        //stle.graphicWidth=((75/zoomSquared)*map.getZoom()*map.getZoom());
        //stle.fontSize=((12/zoomSquared)*map.getZoom()*map.getZoom());
        shipVectors.redraw();
});

// Data stream
var socket = io('//'+window.location.host, {path: window.location.pathname+'socket.io'});
socket.on('update-msg', function(msg) {
	var data = msg.data;
	// $('#messages').append(data).append("<br />");
	console.log(data);
	
    var ship=JSON.parse(data);

    var now = new Date().getTime();
    var age=(now-ship.timestamp);

    if (age<maxAge){
        // Only add ship when it is not too old
	    addShip(ship);
	}
	
	cleanup();
});
