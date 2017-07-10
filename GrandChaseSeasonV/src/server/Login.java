package server;

import java.net.*;
import java.util.*;

import client.*;

public class Login extends Thread {
	public int serverPort = 0;
	ServerSocket ss = null;
	
	public Vector<LoginClient> clients = null;
	
	public LoginPingPong pingpong = null;
	
	public Login(int port) {
		Main.printmsg("로그인 서버(" + port + ") 생성");
		
		serverPort = port;		
		clients = new Vector<LoginClient>();
		
		pingpong = new LoginPingPong(this);
		pingpong.start();
	}
	
	public void run() {
		Main.printmsg("로그인 서버(" + serverPort + ") 스레드 시작 됨");
		
		try {
			ss = new ServerSocket(serverPort);
			
			while( true ) {
				LoginClient c = new LoginClient(ss.accept());
				clients.add(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
