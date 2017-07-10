package util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

public class Convert {
	public static int byteArrayToIntBig(byte[] bytes) {
		return ((((int)bytes[0] & 0xff) << 24) |
				(((int)bytes[1] & 0xff) << 16) |
				(((int)bytes[2] & 0xff) << 8) |
				(((int)bytes[3] & 0xff)));
	}

	public static int byteArrayToIntBig(byte[] bytes, int offset) {
		return ((((int)bytes[offset+0] & 0xff) << 24) |
				(((int)bytes[offset+1] & 0xff) << 16) |
				(((int)bytes[offset+2] & 0xff) << 8) |
				(((int)bytes[offset+3] & 0xff)));
	}
	
	public static int byteArrayToShortBig(byte[] bytes) {
		return ((((int)bytes[0] & 0xff) << 8) |
				((int)bytes[1] & 0xff));
	}
	
	public static int byteArrayToShortBig(byte[] bytes, int offset) {
		return ((((int)bytes[offset+0] & 0xff) << 8) |
				((int)bytes[offset+1] & 0xff));
	}
	
	public static int byteArrayToIntLittle(byte[] bytes) {
		return ((((int)bytes[0] & 0xff)) |
				(((int)bytes[1] & 0xff) << 8) |
				(((int)bytes[2] & 0xff) << 16) |
				(((int)bytes[3] & 0xff) << 24));
	}
	
	public static int byteArrayToIntLittle(byte[] bytes, int offset) {
		return ((((int)bytes[offset+0] & 0xff)) |
				(((int)bytes[offset+1] & 0xff) << 8) |
				(((int)bytes[offset+2] & 0xff) << 16) |
				(((int)bytes[offset+3] & 0xff) << 24));
	}
	
	public static int byteArrayToShortLittle(byte[] bytes) {
		return ((((int)bytes[0] & 0xff)) |
				((int)bytes[1] & 0xff) << 8);
	}
	
	public static int byteArrayToShortLittle(byte[] bytes, int offset) {
		return ((((int)bytes[offset+1] & 0xff)) |
				((int)bytes[offset+0] & 0xff) << 8);
	}
	
	public static byte[] IntToByteArrayLittle(int n) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(n);
		return bb.array();
	}
	
	public static byte[] IntToByteArrayBig(int n) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(n);
		return bb.array();
	}
	
	public static byte[] ShortToByteArrayLittle(short n) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort(n);
		return bb.array();
	}
	
	public static byte[] ShortToByteArrayBig(short n) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(n);
		return bb.array();
	}
	
	public static byte[] hexToByteArray(String hex) { 
		if (hex == null || hex.length() == 0) { 
			return null; 
		} 
		
		hex = hex.replace(" ", "");
		hex = hex.trim();
		
		byte[] ba = new byte[hex.length() / 2]; 
		
		for (int i = 0; i < ba.length; i++) { 
			ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		} 
		
		return ba; 
	}

	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder("");
		
		for(final byte b : bytes)
	        sb.append(String.format("%02x ", b&0xff));
		
		return sb.toString();
	}
	
	public static String MD5(String str){
		String MD5 = ""; 
		try{
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			md.update(str.getBytes()); 
			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			MD5 = sb.toString();
		}catch(Exception e){
			e.printStackTrace(); 
			MD5 = null; 
		}
		return MD5;
	}
}
