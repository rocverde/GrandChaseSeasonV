package packet;

import java.security.SecureRandom;

import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.*;
import javax.crypto.spec.*;

import server.Main;
import util.Convert;

public class Crypto {
	public final static byte[] GC_DES_KEY = { (byte) 0xC7, (byte) 0xD8, (byte) 0xC4, (byte) 0xBF, (byte) 0xB5, (byte) 0xE9, (byte) 0xC0, (byte) 0xFD };
    public final static byte[] GC_HMAC_KEY = { (byte) 0xC0, (byte) 0xD3, (byte) 0xBD, (byte) 0xC3, (byte) 0xB7, (byte) 0xCE, (byte) 0xB8, (byte) 0xB8 };
    public final static byte GC_HMAC_SIZE = 10;
    
    public static byte[] AssemblePacket(Packet p, byte[] key, byte[] hmac, byte[] prefix, int packetno, boolean compress) {
		try {
			DESKeySpec keySpec = new DESKeySpec(key);
			SecretKeyFactory sKeyFac = SecretKeyFactory.getInstance("DES");
			SecretKey sKey = sKeyFac.generateSecret(keySpec);
			
			// IV 생성
			SecureRandom sr = new SecureRandom();
			byte[] iv = new byte[8];
			sr.nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, sKey, ivSpec);
			
			// 데이터
			byte[] packetdata = null;
			int compressedDataLength = -1;
			
			// 압축?
			if( compress == true ) {			
				byte[] temp = new byte[ (int)(p.buffer.length * 1.5) ];
				Deflater compresser = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
				compresser.setInput(p.buffer);
			    compresser.finish();
			    compressedDataLength = compresser.deflate(temp);
			    compresser.end();

			    packetdata = new byte[4 + compressedDataLength];
			    System.arraycopy(Convert.IntToByteArrayLittle(p.buffer.length), 0, 
			    		         packetdata, 0, 4);
			    System.arraycopy(temp, 0, packetdata, 4, compressedDataLength);
			    
			    //System.out.println(Convert.byteArrayToHexString(packetdata));

			    temp = null;
			    compresser = null;
			    
			} else {
				packetdata = new byte[p.buffer.length];
				System.arraycopy(p.buffer, 0, packetdata, 0, p.buffer.length);
			}
			
			// 데이터 조합 (Op, Size, CompFlag, Data)
			int datasize = 2 + 4 + 1 + packetdata.length + 8;
			datasize = datasize - (datasize % 8);
				
			byte[] realdata = new byte[datasize];
			System.arraycopy(Convert.ShortToByteArrayBig((short)p.OpCode), 0,
			         realdata, 0, 2);
			System.arraycopy(Convert.IntToByteArrayBig(packetdata.length), 0,
			         realdata, 2, 4);
			if( compress == true ) {
				realdata[6] = 1;
			} else {
				realdata[6] = 0;
			}
			System.arraycopy(packetdata, 0, realdata, 7, packetdata.length);
			realdata = cipher.doFinal(realdata); // ��ȣȭ
			packetdata = null;
	
			// Prefix + Count + IV + Data
			byte[] readydata = new byte[2 + 4 + 8 + realdata.length];
			System.arraycopy(prefix, 0, readydata, 0, 2);
			System.arraycopy(Convert.IntToByteArrayLittle(packetno), 0, readydata, 2, 4);
			System.arraycopy(iv, 0, readydata, 6, 8);
			System.arraycopy(realdata, 0, readydata, 14, realdata.length);
			
			// hmac 생성
			byte[] completedata = new byte[2 + readydata.length + 10];
			System.arraycopy(Convert.ShortToByteArrayLittle((short)completedata.length), 0, 
					         completedata, 0, 2);
			System.arraycopy(readydata, 0, completedata, 2, readydata.length);
			System.arraycopy(getHmacMD5Data(hmac, readydata), 0, completedata, 2 + readydata.length, 10);
			
			keySpec = null;
			iv = null;
			packetdata = null;
			readydata = null;
			
			return completedata;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
    
    public static void DecryptPacket(Packet p, byte[] key) {
		try {
			DESKeySpec keySpec = new DESKeySpec(key);
			SecretKeyFactory sKeyFac = SecretKeyFactory.getInstance("DES");
			SecretKey sKey = sKeyFac.generateSecret(keySpec);
			
			// iv 파싱
			byte[] iv = new byte[8];
			System.arraycopy(p.buffer, 8, iv, 0, 8);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, sKey, ivSpec);
			
			// 데이터 파싱
			byte[] packetdata = new byte[p.buffer.length - 16 - GC_HMAC_SIZE];
			System.arraycopy(p.buffer, 16, packetdata, 0, packetdata.length);
			
			//System.out.println( Convert.byteArrayToHexString(packetdata) );
			
			p.buffer = cipher.doFinal(packetdata);
			
			byte[] op = new byte[2];
			System.arraycopy(p.buffer, 0, op, 0, 2);
			p.OpCode = Convert.byteArrayToShortBig(op);
			
			// 압축?
			if( p.buffer[6] == 1 ) {			     
			     byte[] compressedData = new byte[p.buffer.length - 11];
			     System.arraycopy(p.buffer, 11, compressedData, 0, p.buffer.length - 11);
			     
			     int originalSize = Convert.byteArrayToIntLittle(p.buffer, 7);
			     
			     Inflater decompresser = new Inflater();
			     decompresser.setInput(compressedData, 0, compressedData.length);
			     
			     // 압축 해제
			     packetdata = new byte[originalSize];
			     decompresser.inflate(packetdata);
			     decompresser.end();
			     
			     // 복사
			     byte[] newpacketdata = new byte[11 + originalSize];
			     System.arraycopy(p.buffer, 0, newpacketdata, 0, 11);
			     System.arraycopy(packetdata, 0, newpacketdata, 11, packetdata.length);
			     
			     p.buffer = newpacketdata;
			}
			
			keySpec = null;
			iv = null;
			packetdata = null;
			op = null;
		}catch(Exception e) {
			e.printStackTrace();
			Main.printmsg("패킷 압축 해제중 오류 발생\n" + Convert.byteArrayToHexString(p.buffer), 0);
		}
	}
    
    public static byte[] getHmacMD5Data(byte[] key, byte[] data) {
        byte[] result = null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacMD5");
            
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(keySpec);
            mac.update(data);
            result = mac.doFinal();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    
}