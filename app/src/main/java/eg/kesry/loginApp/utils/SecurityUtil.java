package eg.kesry.loginApp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {

    public static String md5HexString(String source) {
        // 获取md5HexString

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(source.getBytes());

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
