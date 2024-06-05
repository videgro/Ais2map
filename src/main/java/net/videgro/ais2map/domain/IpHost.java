package net.videgro.ais2map.domain;

import java.net.InetAddress;

public class IpHost {
	final InetAddress ipAddress;
	final int port;
	
	public IpHost(InetAddress ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "AisRepeater [ipAddress=" + ipAddress + ", port=" + port + "]";
	}
}
