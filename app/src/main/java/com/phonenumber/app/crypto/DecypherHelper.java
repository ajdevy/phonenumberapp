//
// Source code recreated from decypherString .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.phonenumber.app.crypto;

import android.annotation.SuppressLint;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DecypherHelper {
    static final byte[] a;
    private static byte[] b;
    private static byte[] c;

    private static final byte[] decypherKeys = new byte[]{(byte) 22, (byte) 0, (byte) 5, (byte) 102, (byte) 116, (byte) 102, (byte) 107, (byte) 113, (byte) 0, (byte) 101, (byte) 4, (byte) 21, (byte) 17, (byte) 7, (byte) 115, (byte) 99};

    static byte[] decypherType(byte[] var0, byte[] var1) {
        byte[] var5 = new byte[var0.length];
        int var3 = 0;

        for (int var2 = 0; var3 < var0.length; ++var3) {
            var5[var3] = (byte) (var0[var3] ^ var1[var2]);
            int var4 = var2 + 1;
            var2 = var4;
            if (var4 >= var1.length) {
                var2 = 0;
            }
        }

        return var5;
    }

    @SuppressLint({"TrulyRandom"})
    public static String decypherString(String var0) {
        Object var1 = null;

        byte[] var5;
        try {
            SecretKeySpec var2 = new SecretKeySpec(DecypherHelper.decypherType(decypherKeys, "SECRET".getBytes()), "AES");
            Cipher var3 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            var3.init(2, var2);
            var5 = var3.doFinal(DecypherHelper.a(var0.getBytes()));
        } catch (Exception var4) {
            var5 = (byte[]) var1;
        }

        return new String(var5);
    }

    static {
        byte var2 = 0;
        a = "\r\n".getBytes();
        b = new byte[255];
        c = new byte[64];

        int var0;
        for (var0 = 0; var0 < 255; ++var0) {
            b[var0] = -1;
        }

        for (var0 = 90; var0 >= 65; --var0) {
            b[var0] = (byte) (var0 - 65);
        }

        for (var0 = 122; var0 >= 97; --var0) {
            b[var0] = (byte) (var0 - 97 + 26);
        }

        for (var0 = 57; var0 >= 48; --var0) {
            b[var0] = (byte) (var0 - 48 + 52);
        }

        b[43] = 62;
        b[47] = 63;

        for (var0 = 0; var0 <= 25; ++var0) {
            c[var0] = (byte) (var0 + 65);
        }

        int var1 = 26;

        for (var0 = 0; var1 <= 51; ++var0) {
            c[var1] = (byte) (var0 + 97);
            ++var1;
        }

        var1 = 52;

        for (var0 = var2; var1 <= 61; ++var0) {
            c[var1] = (byte) (var0 + 48);
            ++var1;
        }

        c[62] = 43;
        c[63] = 47;
    }

    private static boolean a(byte var0) {
        return var0 == 61 || b[var0] != -1;
    }

    public static byte[] a(byte[] var0) {
        byte var3 = 0;
        byte[] var9 = b(var0);
        if (var9.length == 0) {
            var0 = new byte[0];
        } else {
            int var4 = var9.length / 4;
            int var1 = var9.length;

            int var2;
            while (var9[var1 - 1] == 61) {
                var2 = var1 - 1;
                var1 = var2;
                if (var2 == 0) {
                    return new byte[0];
                }
            }

            byte[] var8 = new byte[var1 - var4];
            var2 = 0;
            var1 = var3;

            while (true) {
                var0 = var8;
                if (var1 >= var4) {
                    break;
                }

                int var5 = var1 * 4;
                byte var7 = var9[var5 + 2];
                byte var6 = var9[var5 + 3];
                byte var10 = b[var9[var5]];
                byte var11 = b[var9[var5 + 1]];
                if (var7 != 61 && var6 != 61) {
                    var7 = b[var7];
                    var6 = b[var6];
                    var8[var2] = (byte) (var10 << 2 | var11 >> 4);
                    var8[var2 + 1] = (byte) ((var11 & 15) << 4 | var7 >> 2 & 15);
                    var8[var2 + 2] = (byte) (var7 << 6 | var6);
                } else if (var7 == 61) {
                    var8[var2] = (byte) (var11 >> 4 | var10 << 2);
                } else if (var6 == 61) {
                    var6 = b[var7];
                    var8[var2] = (byte) (var10 << 2 | var11 >> 4);
                    var8[var2 + 1] = (byte) ((var11 & 15) << 4 | var6 >> 2 & 15);
                }

                var2 += 3;
                ++var1;
            }
        }

        return var0;
    }

    static byte[] b(byte[] var0) {
        byte[] var4 = new byte[var0.length];
        int var1 = 0;

        int var2;
        int var3;
        for (var2 = 0; var1 < var0.length; var2 = var3) {
            var3 = var2;
            if (a(var0[var1])) {
                var4[var2] = var0[var1];
                var3 = var2 + 1;
            }

            ++var1;
        }

        var0 = new byte[var2];
        System.arraycopy(var4, 0, var0, 0, var2);
        return var0;
    }
}