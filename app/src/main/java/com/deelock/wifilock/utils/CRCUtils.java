/**
 *
 */
package com.deelock.wifilock.utils;

import java.util.zip.CRC32;

/**
 * @Description: TODO
 * @author: melody.xu
 * @date: Jan 12, 2018 4:36:31 PM
 */
public class CRCUtils {
    private static final String TAG = CRCUtils.class.getName();

    /**
     * @param data
     * @return
     */
    public static String loadCRC32(byte[] data) {
        CRC32 crc32 = new CRC32();
        String crcStr = null;
        try {
            crc32.update(data);
            crcStr = Long.toHexString(crc32.getValue()).toUpperCase();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return crcStr;
    }

    /**
     * @param data
     * @return
     */
    public static String loadCRC16(byte[] data) {
        int high;
        int flag;
        // 16位寄存器，所有数位均为1
        int wcrc = 0x0000ffff;
        for (int i = 0; i < data.length; i++) {
            // 16 位寄存器的高位字节
            high = (wcrc >>> 8) & 0x000000ff;
            // 取被校验串的一个字节与 16 位寄存器的高位字节进行“异或”运算
            wcrc = ((high ^ data[i])) & 0x000000ff;
            for (int j = 0; j < 8; j++) {
                flag = wcrc & 0x00000001;
                // 把这个 16 寄存器向右移一位
                wcrc = (wcrc >>> 1) & 0x00007fff;
                // 若向右(标记位)移出的数位是 1,则生成多项式 1010 0000 0000 0001 和这个寄存器进行“异或”运算
                if (flag == 1)
                    wcrc ^= 0x0000a001;
            }
        }
        String crc = Integer.toHexString(wcrc);
        if (crc.length() != 4) {
            crc = "0" + crc;
        }
        return crc;
    }
}
