package packet;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import util.Convert;

public class Packet {
	public byte[] buffer = null;
	private int pos = 0;
	
	public int OpCode = -1;
	
	private ByteArrayOutputStream baos = null;
	
	public Packet(byte[] packet) {
		buffer = new byte[ packet.length ];
		System.arraycopy(packet, 0, buffer, 0, packet.length);
	}
	
	public Packet(int op) {
		OpCode = op;
		baos = new ByteArrayOutputStream();
	}
	
	// ----- WRITE -----
	public void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            baos.write(b[x]);
        }
    }
	
	public void write(int b) {
        baos.write((byte) b);
    }
	
	public void skip(int b) {
		write(new byte[b]);
    }
	
	public void writeShort(short i) {
		baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) (i & 0xFF));
    }
	
	public void writeInt(int i) {
		baos.write((byte) ((i >>> 24) & 0xFF));
		baos.write((byte) ((i >>> 16) & 0xFF));
		baos.write((byte) ((i >>> 8) & 0xFF));
		baos.write((byte) (i & 0xFF));
    }
	
	public void writeString(String s) {
        write(s.getBytes(Charset.forName("ASCII")));
    }
	
	public void writeUnicodeString(String s) {
        write(s.getBytes(Charset.forName("UTF-16LE")));
    }
	
	public void writeHexString(String s) {
        write(Convert.hexToByteArray(s));
    }
	
	public void applyWrite() {
		if( baos.size() < 10 ) {
			write(new byte[] {0,0,0,0,0,0,0,0,0,0});
		}
		
		buffer = baos.toByteArray();
	}
	
	// ----- READ -----
	public void seek(long offset) {
        pos = (int) offset;
    }
	
	public int read() {
        return ((int) buffer[pos++]) & 0xFF;
    }
	
	public byte readByte() {
        return (byte)read();
    }
	
	public short readShort() {
        return (short) ((read() << 8) + read());
    }
	
	public int readInt() {
        return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }
	
	public String readString(int n) {
		byte ret[] = new byte[n];
		for (int x = 0; x < n; x++) {
		    ret[x] = readByte();
		}
		return new String(ret, Charset.forName("ASCII"));
	}
	
	public String readUnicodeString(int n) {
		byte ret[] = new byte[n];
		for (int x = 0; x < n; x++) {
		    ret[x] = readByte();
		}
		return new String(ret, Charset.forName("UTF-16"));
	}
}
