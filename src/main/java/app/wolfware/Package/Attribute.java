package app.wolfware.Package;

import java.text.DecimalFormat;

public class Attribute {

	private byte[] ID;
	private byte[] DATA;
	
	
	public Attribute(int ID) {
		this.ID = intToByteArray2Bytes(ID);
	}
	
	public Attribute(int ID, byte[] DATA) {
		this.ID = intToByteArray2Bytes(ID);
		this.DATA = DATA;
	}
	
	public Attribute(int ID, int DATA) {
		this.ID = intToByteArray2Bytes(ID);
		this.DATA = intToByteArray2Bytes(DATA);
	}
	
	public Attribute(int ID, String DATA) {
		this.ID = intToByteArray2Bytes(ID);
		this.DATA = DATA.getBytes();
	}
	
	public Attribute(byte[] ID, byte[] DATA) {
		this.ID = ID;
		this.DATA = DATA;
		
	}
	
	@Deprecated
	public void setData(byte[] DATA) {
		this.DATA = DATA;
	}
	
	public byte[] get() {
		byte[] PACKET_LENGTH = intToByteArray(ID.length + DATA.length);
		return attachByteArrays(attachByteArrays(PACKET_LENGTH, ID), DATA);
	}
	
	public String toString() {
		String output = "ID: ";
		for (byte b : ID) {
			output += "0x" + String.format("%02x", b) + ",";
		}
		output = output.substring(0, output.length() - 1);
		output += "\nDATA: ";
		for (byte b : DATA) {
			output += "0x" + String.format("%02x", b) + ",";
		}
		output = output.substring(0, output.length() - 1);
		return output;
	}
	
	public byte[] getID() {
		return ID;
	}
	
	public int getIDAsInt() {
		return ((ID[0] & 0xFF) | (ID[1] & 0xFF) << 8);
	}
	
	public byte[] getDATA() {
		return DATA;
	}
	
	public String getDATAAsString() {
		return new String(DATA);
	}
	
	public int getDATAAsInt() {
		if (DATA.length == 1) {
			return (DATA[0] & 0xFF);
		} else if (DATA.length == 2) {
			return ((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8);
		} else if (DATA.length == 3) {
			return ((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8 | (DATA[2] & 0xFF) << 16);
		} else if (DATA.length == 4) {
			return ((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8 | (DATA[2] & 0xFF) << 16 | (DATA[3] & 0xFF) << 24);
		}
		return -1;
	}
	
	public float getDATAAsFloat() {
		if (DATA.length == 1) {
			return Float.intBitsToFloat((DATA[0] & 0xFF));
		} else if (DATA.length == 2) {
			return Float.intBitsToFloat(((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8));
		} else if (DATA.length == 3) {
			return Float.intBitsToFloat(((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8 | (DATA[2] & 0xFF) << 16));
		} else if (DATA.length == 4) {
			return Float.intBitsToFloat(((DATA[0] & 0xFF) | (DATA[1] & 0xFF) << 8 | (DATA[2] & 0xFF) << 16 | (DATA[3] & 0xFF) << 24));
		}
		return -1.0F;
	}
	
	public String getDATAAsFloat(String format) {
		return new DecimalFormat(format).format(getDATAAsFloat());
	}
	
	
	public boolean getDATAAsBoolean() {
		if (getDATAAsInt() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	/*public int getDATAAsInt() {
		return (DATA[0] & 0xFF);
	}*/
	
	private static int byteArrayToInt(byte[] b) {
	    int value = 0;
	    for (int i = 0; i < b.length; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	private final static byte[] intToByteArray2Bytes(int value) {
		return new byte[] {
				(byte)value,
				(byte)(value >>> 8)};
	}
	
	private final static byte[] intToByteArray(int value) {
	    byte[] ret = new byte[4];
	    ret[0] = (byte) (value & 0xFF);   
	    ret[1] = (byte) ((value >> 8) & 0xFF);   
	    ret[2] = (byte) ((value >> 16) & 0xFF);   
	    ret[3] = (byte) ((value >> 24) & 0xFF);
	    return ret;
	}
	
	private final static byte[] attachByteArrays(byte[] target, byte[] source) {
	    byte[] result = new byte[target.length + source.length]; 
	    System.arraycopy(target, 0, result, 0, target.length); 
	    System.arraycopy(source, 0, result, target.length, source.length); 
	    return result;
	}
}
