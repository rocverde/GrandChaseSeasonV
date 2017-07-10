package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

public class Ini {
	public static Properties p = null;
	public static String ini_file = "GrandChaseSeasonV.ini";
	
	public static void init() {
		if( p == null ) {
			try {
				p = new Properties();
				p.load(new FileInputStream(ini_file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getIni(String key) {
		init();
		return new String(p.getProperty(key).getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"));
	}
	
	public static void setIni(String key, String value) {
		init();
		p.setProperty(key, value);
		
		try {
			p.store(new FileOutputStream(ini_file), "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
