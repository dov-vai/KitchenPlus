package com.kitchenplus.kitchenplus.utils;

public class GeneratorUtils {
    /**
     * Generates a random string of the specified length.
     *
     * @param length the length of the random string
     * @return a random string of the specified length
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
