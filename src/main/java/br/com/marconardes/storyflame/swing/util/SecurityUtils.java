package br.com.marconardes.storyflame.swing.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) { // Ensure 32 characters for SHA-256
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // In a real application, you might want to log this error or throw a custom exception
            System.err.println("Error hashing password: " + e.getMessage());
            return null; // Or throw an exception
        }
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashedPassword.equals(hashOfPlainPassword);
    }
}
