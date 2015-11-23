//------------------------------------------------------------------------------
package com.robertniu.nfcarse;


//[------------------------------ MAIN CLASS ----------------------------------]
//--------------------------------- REVISIONS ----------------------------------
//Date       Name                 Tracking #         Description
//--------   -------------------  -------------      --------------------------
//13SEP2011  James Shen                 	         Initial Creation
////////////////////////////////////////////////////////////////////////////////
/**
 * Convert help class.
 * <hr>
 * <b>? Copyright 2011 Guidebee, Inc. All Rights Reserved.</b>
 * 
 * @version 1.00, 13/09/11
 * @author Guidebee Pty Ltd.
 */
public class Converter {

	// Hex help
	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
			(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

	// //////////////////////////////////////////////////////////////////////////
	// --------------------------------- REVISIONS
	// ------------------------------
	// Date Name Tracking # Description
	// --------- ------------------- ------------- ----------------------
	// 13SEP2011 James Shen Initial Creation
	// //////////////////////////////////////////////////////////////////////////
	/**
	 * convert a byte arrary to hex string
	 * 
	 * @param raw
	 *            byte arrary
	 * @param len
	 *            lenght of the arrary.
	 * @return hex string.
	 */
	public static String getHexString(byte[] raw, int len) {
		byte[] hex = new byte[2 * len];
		int index = 0;
		int pos = 0;

		for (byte b : raw) {
			if (pos >= len)
				break;

			pos++;
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		return new String(hex);
	}

	private static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 | _b1);
		return ret;
	}

	public static byte[] HexString2Bytes(String src) {
		int length = src.length()/2;
		byte[] ret = new byte[length];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < length; ++i) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);			
		}
		return ret;
	}
	
	// String C1940190 -> String 11000001100101000000000110010000
	public static String HexString2BinaryString(String src) {
		int length = src.length();
        String ret=new String();
		for(int i=0;i<length;i++){
			String retBinaryString=Integer.toBinaryString(Integer.parseInt(String.valueOf(src.charAt(i)), 16));
			for(int j=0;j<(4-retBinaryString.length());j++)
			{ret += "0";}
			ret += retBinaryString;
		}
	
		return ret;
	}

}
