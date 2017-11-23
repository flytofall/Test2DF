package com.xprinter.test2df;

import java.io.UnsupportedEncodingException;

/**
 * Created by kylin on 2017/4/6.
 */

public class StringUtils {
    /**
     * 字符串转byte数组
     * */
    public static byte[] strTobytes(String str){
        byte[] b=null,data=null;
        try {
            b = str.getBytes("utf-8");
            data=new String(b,"utf-8").getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
    /**
     * byte数组拼接
     * */
    public static   byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
    public static byte[] strTobytes(String str ,String charset){
        byte[] b=null,data=null;
        try {
            b = str.getBytes("utf-8");
            data=new String(b,"utf-8").getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
    /**
     * 字符串转换成十六进制字符串
     * @param  str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }
    /**
     * 打印十六进制字符指令
     * */
    public static byte[] sendHex(String text){
        int l=text.length()/2;
        int a=0;
        byte[] data=new byte[l];
        String[] temps=new String[l];
        for (int i = 0; i < l; i++) {
            String s=text.substring(2*i, 2*i+2);
            temps[i]="0x"+s;
            try {
                a=Integer.decode(temps[i]);
                data[i]=(byte) a;
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                data=null;
            }
        }
        return data;
    }
}
