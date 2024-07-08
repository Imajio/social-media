package org.bytebound;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class SHA256Hash {

    public String HashSTR(String s) {
        try {
            return toHexString(getSHA(s));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public String HashINT(int i) {
        try {
            return toHexString(getSHA(String.valueOf(i)));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

}