package net.videgro.ais2map;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.videgro.ais2map.domain.IpHost;

public class AisRepeater {
	private static final Logger LOGGER = LogManager.getRootLogger();
	
	private final List<IpHost> hosts;
	
	public AisRepeater(final List<IpHost> hosts){
		this.hosts=hosts;		
	}
	
	/**
	 * Sends Ais message via UDP
	 * @param message The AIS message
	 */
	public void repeat(final String message) {
		//LOGGER.trace(message);
		hosts.forEach(h -> {
			byte[] sendData;
			DatagramPacket sendPacket;
			try (final DatagramSocket clientSocket = new DatagramSocket()) {
				sendData = message.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, h.getIpAddress(),h.getPort());
				clientSocket.send(sendPacket);
				clientSocket.close();
			} catch (SocketException e) {
				LOGGER.error(e);
			} catch (IOException e) {
				LOGGER.error(e);
			}
			sendData=null;
			sendPacket=null;
		});
	}
}
