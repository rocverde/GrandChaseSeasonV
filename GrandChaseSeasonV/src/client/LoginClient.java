package client;

import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;

import login.AllFunction;
import packet.Crypto;
import packet.OpcodeLogin;
import packet.Packet;
import server.Main;
import server.SocketRecvHandler;
import util.Convert;

public class LoginClient extends Client {
	public LoginClient(Socket s)  {
		isClosed = false;
		this.s = s;
		client_type = Client.LOGIN_CLIENT;
		
		sh = new SocketRecvHandler(this);
		sh.start();
		
		Main.printmsg("새로운 클라이언트 접속 (" + s.getInetAddress().getHostAddress() + ":" + s.getPort() + ")");
		
		// 패킷 암호화 키 초기화
		SecureRandom sr = new SecureRandom();
		sr.nextBytes(packetPrefix);
		sr.nextBytes(packetKey);
		sr.nextBytes(packetHmac);
		
		// 처음 보내는 패킷
		isFirstPacket = true;
		Packet p = new Packet(OpcodeLogin.SET_SECURITY_KEY_NOT);
		p.write(packetPrefix);
		p.writeInt(8);
		p.write(packetHmac);
		p.writeInt(8);
		p.write(packetKey);
		p.writeInt(1);
		p.skip(8);
		sendPacket(p, false);
		p = null;
		isFirstPacket = false;
		
		Main.printmsg("    DES KEY: " + Convert.byteArrayToHexString(packetKey));
		
		// 두 번째 패킷
		p = new Packet(OpcodeLogin.ENU_WAIT_TIME_NOT);
		p.writeInt(10000);
		sendPacket(p, false);
		p = null;
	}
	
	// 패킷 보내기
	public void sendPacket(Packet p, boolean compress) {
		p.applyWrite();
		
		byte[] sendbuffer = null;
		if( isFirstPacket )
			sendbuffer = Crypto.AssemblePacket(p, Crypto.GC_DES_KEY, Crypto.GC_HMAC_KEY, new byte[] {0, 0}, packetNum++, compress);
		else
			sendbuffer = Crypto.AssemblePacket(p, packetKey, packetHmac, packetPrefix, packetNum++, compress);
		
		//System.out.println( Convert.byteArrayToHexString(sendbuffer) );
		
		try {
			OutputStream os = s.getOutputStream();
			os.write(sendbuffer);
			os.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// 패킷 수신
	public void onPacket(Packet p) {
		Crypto.DecryptPacket(p, packetKey);
		
		int Opcode = p.readShort();
		int dataSize = p.readInt();
		int isCompressed = p.readByte();
		if( isCompressed == 1 ) p.readInt();
		
		switch( Opcode ) {
		case OpcodeLogin.HEART_BIT_NOT: // ��Ʈ��Ʈ
			heartbit = System.currentTimeMillis();
			break;
		case OpcodeLogin.ENU_CLIENT_CONTENTS_FIRST_INIT_INFO_REQ:
			AllFunction.sendClientContentsFirstInitInfo(this);
			break;
		case OpcodeLogin.ENU_SHAFILENAME_LIST_REQ:
			AllFunction.sendShaFileList(this);
			break;
		case OpcodeLogin.ENU_VERIFY_ACCOUNT_REQ:
			AllFunction.onLogin(this, p);
			break;
		case OpcodeLogin.ENU_GUIDE_BOOK_LIST_REQ:
			AllFunction.sendGuideBookList(this);
			break;
		case OpcodeLogin.ENU_CLIENT_PING_CONFIG_REQ:
			AllFunction.sendClientPingConfig(this);
			break;
		default:
			Main.printmsg("정의되지 않은 패킷 수신 (" + dataSize + "바이트)\n" + Convert.byteArrayToHexString(p.buffer));
			break;
		}
	}
	
	public  void close() {
		try {
			Main.printmsg("클라이언트 접속 해제 (" + s.getInetAddress().getHostAddress() + ":" + s.getPort() + ")");
		
			sh.interrupt();
			s.close();
		
			isClosed = true; // 끊어졌다고 표시한다. PingPong이 객체 삭제해야함.
			s = null;
			sh = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
