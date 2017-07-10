package server;

import client.LoginClient;

public class LoginPingPong extends Thread {
	public Login server = null;
	
	public LoginPingPong(Login server){
		this.server = server;
	}
	
	// 연결 상태 체크
	public boolean checkConnect(LoginClient c) {
		try {
			if( c.s.isConnected() == false || c.s.isClosed() == true || c.s.isBound() == false  ){
				return false;
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	// 아직 미구현
	public boolean checkTimeout(LoginClient c) {
		return false;
	}
	
	public void remove(LoginClient c) {
		Main.printmsg("클라이언트 객체 삭제");
		
		server.clients.remove(c);
		c = null;
	}
	
	public void run() {
		while(true) {
			try {
				for(LoginClient c : server.clients) {
					// 닫혔다고 표시?
					if( c.isClosed == true ) {
						remove(c);
						break;
					}
					
					// 소켓이 없나?
					if( c.s == null ) {
						remove(c);
						break;
					}
					
					// 연결 되어있나?
					if( checkConnect(c) == false ) {
						c.close();
						remove(c);
						break;
					}
					
					// 타임아웃됐나?
					if( checkTimeout(c) == true ) {
						c.close();
						remove(c);
						break;
					}
				}
				
				Thread.sleep(5000);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
