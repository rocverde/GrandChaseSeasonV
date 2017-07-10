package server;

import java.io.InputStream;

import client.Client;
import client.LoginClient;
import packet.Packet;
import util.Convert;

public class SocketRecvHandler extends Thread {
	public Client parent = null;
	public byte[] buffer = null;
	public int packetlen = -1;
	
	public SocketRecvHandler(Client c) {
		parent = c;
		buffer = new byte[0];
	}
	
	public void end() {
		switch( parent.client_type ) {
		case Client.LOGIN_CLIENT:
			LoginClient lc = (LoginClient)parent;
			lc.close();
			break;
		case Client.GAME_CLIENT:
			break;
		}
	}

	public void run() {
		try {
			InputStream is = parent.s.getInputStream();
		
			while(true){
			
				byte[] data = new byte[1000];
				int readBytes = -1;
				
				// 패킷 계속 수신한다
				while( (readBytes = is.read(data)) != -1 ) {
					appendPacket(data, readBytes);
				}
				
				// 여기로 빠져나왔으면 뭔가 문제가 생긴거
				end();
				return;
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		end();
	}
	
	public void appendPacket(byte[] inputdata, int readBytes) {
		// 패킷 이어 붙이기
		byte[] oldbuffer = buffer;
		buffer = new byte[oldbuffer.length + readBytes];
		
		System.arraycopy(oldbuffer, 0, buffer, 0, oldbuffer.length);
		System.arraycopy(inputdata, 0, buffer, oldbuffer.length, readBytes);
		
		ProcessPacket();
	}
	
	public void ProcessPacket() {
		// 새로운 패킷인가?
		if( buffer.length >= 2 && packetlen == -1 ) {
			byte[] len_temp = new byte[2];
			System.arraycopy(buffer, 0, len_temp, 0, 2);
			packetlen = Convert.byteArrayToShortLittle(len_temp);
			
			len_temp = null;
		}
		
		// 패킷 길이가 아직도 안 정해졌다.
		if( packetlen == -1 )
			return;
		
		// 처리할 패킷이 전부 도착했다
		if( buffer.length >= packetlen ) {
			// 패킷
			byte[] packet_temp = new byte[packetlen];
			System.arraycopy(buffer, 0, packet_temp, 0, packetlen);
			
			// 버퍼에서 빼기
			byte[] oldbuffer = buffer;
			buffer = new byte[oldbuffer.length - packetlen];
			System.arraycopy(oldbuffer, packetlen, buffer, 0, buffer.length);
			
			// 패킷 객체에 담기
			Packet p = new Packet(packet_temp);
			
			switch( parent.client_type ) {
			case Client.LOGIN_CLIENT:
				LoginClient lc = (LoginClient)parent;
				lc.onPacket(p);
				break;
			case Client.GAME_CLIENT:
				break;
			}
			
			packetlen = -1;
			packet_temp = null;
			oldbuffer = null;
			
			// 아직 남은 데이터가 있으므로...
			ProcessPacket();
		}
	}
}
