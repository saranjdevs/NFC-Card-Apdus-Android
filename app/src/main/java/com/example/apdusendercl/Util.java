

package com.example.apdusendercl;

public class Util
{

    public static String szByteHex2String(byte datain)
    {
        String[] CHARS0F = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        int idata = datain & 0xFF;
        int nibble_1 = (idata >> 0x04) & 0x0F;
        int nibble_2 = idata & 0x0F;

        return CHARS0F[nibble_1] + CHARS0F[nibble_2];
    }

    public static String getHexString(byte[] data) throws Exception {
        String szDataStr = "";
        for (int ii = 0; ii < data.length; ii++) {
            szDataStr += String.format("%02X ", data[ii] & 0xFF);
        }
        return szDataStr;
    }

    public static String getATRLeString(byte[] data) throws Exception {
        return String.format("%02X ", data.length | 0x80);
    }

    public static String getATRXorString(byte[] b) throws Exception {
        int Lrc = 0x00;
        Lrc = b.length | 0x80;
        Lrc = Lrc ^ 0x81;
        for (int i = 0; i < b.length; i++) {
            Lrc = Lrc ^ (b[i] & 0xFF);
        }
        return String.format("%02X ", Lrc);
    }

}
