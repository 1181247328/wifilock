package com.deelock.wifilock.utils;


/**
 * Created by forgive for on 2018\6\22 0022.
 */
public class XxUtil {

    /**
     * Encrypt data with key.
     *
     * @param v
     * @param k
     * @return
     */
    public static int[] encrypt(int[] v, int[] k) {
        int n = v.length;

        int y;
        int p;
        int rounds = 6 + 52/n;
        int sum = 0;
        int z = v[n-1];
        int delta = 0x9E3779B9;
        do {
            sum += delta;
            int e = (sum >>> 2) & 3;
            for (p=0; p<n-1; p++) {
                y = v[p+1];
                v[p] += (z>>3^y<<2) + (y>>1^z<<4) ^ (sum^y) + (k[(p&3)^e] ^ z);
                v[p] &= 0xFF;
                z = v[p];
            }
            y = v[0];
            v[n-1] += (z>>3^y<<2) + (y>>1^z<<4) ^ (sum^y) + (k[(p&3)^e] ^ z);
            v[n-1] &= 0xFF;
            z = v[n-1];
        } while (--rounds > 0);

        return v;
    }

    /**
     * Decrypt data with key.
     *
     * @param v
     * @param k
     * @return
     */
    public static int[] decrypt(int[] v, int[] k) {
        int n = v.length;
        int z = v[n - 1], y = v[0], delta = 0x9E3779B9, sum, e;
        int p;
        int rounds = 6 + 52/n;
        sum = rounds*delta;
        y = v[0];
        do {
            e = (sum >>> 2) & 3;
            for (p=n-1; p>0; p--) {
                z = v[p-1];
                v[p] -= (z>>3^y<<2) + (y>>1^z<<4) ^ (sum^y) + (k[(p&3)^e] ^ z);
                v[p] &= 0xFF;
                y = v[p];
            }
            z = v[n-1];
            v[0] -= (z>>3^y<<2) + (y>>1^z<<4) ^ (sum^y) + (k[(p&3)^e] ^ z);
            v[0] &= 0xFF;
            y = v[0];
        } while ((sum -= delta) != 0);
        return v;
    }

//    public static void main(String args[]){
//        int[] data = {0xA4,  0xD0, 0x48};
//        int[] key = {0x12, 0x5A, 0x06, 0x15};
//        int[] result = encrypt(data, key);  //5DDE99    93 222 153
//    }
}
