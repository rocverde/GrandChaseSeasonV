package client;

import java.net.Socket;

import server.SocketRecvHandler;

public class Client {
	public final static int LOGIN_CLIENT = 1;
	public final static int GAME_CLIENT = 2;
	public int client_type = 0;
	
	public boolean isClosed = false;
	public Socket s = null;
	public SocketRecvHandler sh = null;
	
	public long heartbit = 0;

	public byte[] packetPrefix = new byte[2];
	public byte[] packetKey = new byte[8];
	public byte[] packetHmac = new byte[8];
	public int packetNum = 1;
	public boolean isFirstPacket = true;
	
	public void close() {}
}
