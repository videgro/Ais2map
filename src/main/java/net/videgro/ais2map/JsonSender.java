package net.videgro.ais2map;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonSender {
	private static final Logger LOGGER = LogManager.getRootLogger();
	
	private final Settings settings;
	
	public JsonSender(final Settings settings){
		this.settings=settings;		
	}
	
	/**
	 * Sends JSON message via UDP
	 * @param message The JSON message
	 */
	public void send(final String message){			
		try {
			DatagramSocket clientSocket = new DatagramSocket();		
			byte[] sendData = new byte[1024];
			sendData = message.getBytes();
		    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, settings.getIpAddressJson(),settings.getPortJson());
		    clientSocket.send(sendPacket);
		    clientSocket.close(); 
		} catch (SocketException e) {
			LOGGER.error(e);			
		} catch (IOException e){
			LOGGER.error(e);
		}
	}
}
