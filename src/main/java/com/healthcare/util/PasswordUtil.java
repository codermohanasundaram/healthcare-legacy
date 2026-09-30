package com.healthcare.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing. Introduced in v1.4 - before that passwords were stored
 * as plain text (some accounts were never migrated, see UserDAO).
 */
public class PasswordUtil {

    public static String md5(String input) {
        if (input == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
