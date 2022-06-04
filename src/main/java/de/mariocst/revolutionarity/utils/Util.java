package de.mariocst.revolutionarity.utils;

import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

public class Util {
    static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static SecureRandom rnd = new SecureRandom();

    public static byte[] skinArray = null;

    // Sets up the invisible skin
    public static void setupSkinStream() {
        if (skinArray != null) return;
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        for (int i = 0; i < 16384; i++)
            str.write(0);
        skinArray = str.toByteArray();
    }

    // Returns a random string with specified length
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }

    // Returns the distance between two coordinates
    public static float distance(float x, float y, float z, float x2, float y2, float z2) {
        float dX = x - x2;
        float dY = y - y2;
        float dZ = z - z2;
        return (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    // Checks if a value is similar to another
    public static boolean isRoughlyEqual(double x, double y, double leniency) {
        return Math.abs(x - y) < leniency;
    }

    public static boolean isRoughlyEqual(float x, float y, float leniency) {
        return Math.abs(x - y) < leniency;
    }

}
