package server;

import java.util.Calendar;
import util.*;

public class Main {
	public Login server_login = null;
	
	public Main() {
		printmsg( "Grand Chase Season V Emulator" );
		
		printmsg("데이터베이스 연결 테스트...");
		if( Database.test() == true )
			printmsg("    성공");
		else {
			printmsg("    실패", 0);
			QuitProgram();
			return;
		}
		
		if( Ini.getIni("login.enable").equals("1") == true ) {
			server_login = new Login( Integer.parseInt(Ini.getIni("login.port")) );
			server_login.start();
		}
	}
	
	public static void QuitProgram() {
		printmsg("종료");
		System.exit(0);
	}

	public static void main(String[] args) {
		test();
		new Main();
	}
	
	public static void printmsg(String msg) {
		System.out.printf( "[알림 / %tT] %s\n", Calendar.getInstance(), msg );
	}
	
	public static void printmsg(String msg, int level) {
		String l = "알림";
		switch( level ) {
		case 0: l = "오류"; break;
		case 1: l = "경고"; break;
		case 2: l = "알림"; break;
		}
		System.out.printf( "[%s / %tT] %s\n", l, Calendar.getInstance(), msg );
	}
	
	// 테스트용
	public static void test() {

	}
}
