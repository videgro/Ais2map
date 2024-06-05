package net.videgro.ais2map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketParser;
import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;
import dk.dma.ais.sentence.SentenceException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import net.videgro.ais2map.domain.NmeaTO;

/*
 *  Run at development PC (no logging, other speech synthesizer)
 *  --ais-address 192.168.178.20 --logging-dir "" --speech-synthesizer-bin /usr/bin/pico2wave --speech-synthesizer-args "-w <OUTFILE> '<TEXT>'"
 *  
 *  
 *  Build with JAVA 8
 */
public class Main {
	private static final Logger LOGGER = LogManager.getRootLogger();

	private Settings settings = new Settings();
	private AisRepeater aisRepeater;
	private AisParser aisParser;
	private JsonSender jsonSender;

	public Main(final String[] args) throws IOException {
		LOGGER.trace("Main");

		if (settings.parseCommandLine(args)) {
			jsonSender = new JsonSender(settings);
			if (settings.isDemoMode()) {
				demoMode();
			} else {
				aisRepeater = new AisRepeater(settings.getAisRepeaters());
				aisParser = new AisParser(settings);
				final AisReader reader = AisReaders.createUdpReader(settings.getIpAddressAisStr(),settings.getPortAis());
				reader.registerHandler(new Consumer<AisMessage>() {
					public void accept(AisMessage aisMessage) {
						aisRepeater.repeat(aisMessage.getVdm().getRawSentencesJoined());
						final String json = aisParser.parse(aisMessage);
						jsonSender.send(json);
					}
				});
				reader.start();
				//listenSocketIoNmea2sendSocketIoShip();
				try {
					reader.join();
				} catch (InterruptedException e) {
					LOGGER.error(e);
				}
			}
		} else {
			LOGGER.error("Not starting: invalid arguments.");
		}
	}

	/**
	 * 1) Listen at SocketIO server A for NMEA messages encapsulated in NmeaTO
	 * 2) Parse them to Ship-object and send them to SocketIO server B.
	 * 
	 * Payload on both servers is encapsulated into JSON-objects
	 */
	private void listenSocketIoNmea2sendSocketIoShip() {
		final String tag="listenSocketIoNmea2sendSocketIoShip - ";
		Socket socketIo = null;

		final Gson gson = new Gson();

		final AisPacketParser aisPacketParser = new AisPacketParser();

		try {
			socketIo = IO.socket(NmeaTO.SERVER);
		} catch (URISyntaxException e) {
			LOGGER.error(tag+"init",e);
		}
		
		if (socketIo != null) {
			socketIo.on(NmeaTO.TOPIC_NMEA, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
	
					NmeaTO nmeaTo=null;
					try {
						nmeaTo = gson.fromJson((String) args[0], NmeaTO.class);
					} catch (JsonSyntaxException e){
						LOGGER.error(tag+"fromJson",e);
					}
					
					if (nmeaTo!=null){
						AisPacket aisPacket = null;
						try {
							aisPacket = aisPacketParser.readLine(nmeaTo.getData());
						} catch (SentenceException e) {
							LOGGER.error(tag+"readline",e);
						}
						
						try {
							if (aisPacket != null) {
								final String json = aisParser.parse(aisPacket.getAisMessage());
								LOGGER.trace("Received JSON from SocketIO server ("+NmeaTO.SERVER+"): "+json);
								jsonSender.send(json);
							}
						} catch (AisMessageException | SixbitException e) {
							LOGGER.error(tag+"parse/send JSON",e);
						}
					}
				}					
			});
			socketIo.connect();

			// Ask cached messages from server
			socketIo.emit(NmeaTO.TOPIC_NMEA_RETRANSMIT, "JAVA CLIENT");
		}
	}

	private void demoMode() throws IOException {
		final int delay = 1000;
		final String file = settings.getDirResources() + "logs/ais-log-demo.json";
		LOGGER.warn("DEMO MODE - Will playback AIS log: " + file);
		BufferedReader br = new BufferedReader(new FileReader(file));
		int lineNumber = 0;
		String line;
		while ((line = br.readLine()) != null) {
			LOGGER.debug("Send line: " + (++lineNumber));
			jsonSender.send(line);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
		br.close();
	}

	public static void main(String[] args) {
		try {
			new Main(args);
			System.out.println("Running, press CTRL-C to stop..");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
