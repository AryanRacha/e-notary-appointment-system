package com.enotary.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Cryptographic utility for salted SHA-256 password hashing and constant-time verification.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = HexFormat.of().formatHex(saltBytes);
        String hash = computeHash(salt, rawPassword);
        return salt + "$" + hash;
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || !storedHash.contains("$")) {
            return false;
        }
        String[] parts = storedHash.split("\\$", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        String actualHash = computeHash(salt, rawPassword);
        return MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                actualHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String computeHash(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
