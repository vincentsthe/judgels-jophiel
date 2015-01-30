package org.iatoki.judgels.jophiel;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class JophielUtils {

    private JophielUtils() {
        // prevent instantiation
    }

    public static String hashSHA256(String s) {
        return messageDigest(s, "SHA-256");
    }

    public static String hashMD5(String s) {
        return messageDigest(s, "MD5");
    }

    private static String messageDigest(String s, String algorithm) {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            hash = md.digest(s.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            // No way this thing will ever happen
            e.printStackTrace();
        }
        return new String(Hex.encodeHex(hash));
    }

}
