package com.thriftyApp;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted, key-stretched password hashing for credentials stored in the local
 * database. Passwords are never persisted in clear text: only a per-record
 * random salt and a PBKDF2 derivation are stored, in the form
 * {@code base64(salt):base64(hash)}.
 *
 * <p>This is the remediation for the {@code java/android/cleartext-storage-database}
 * findings — the credential value reaching the SQLite sink is the irreversible
 * output of {@link #hashPassword(String)} rather than the raw password.</p>
 */
final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String SEPARATOR = ":";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Utility class – not instantiable.
    }

    /**
     * Derives a salted PBKDF2 hash for {@code plainPassword}. The returned value
     * is safe to persist and cannot be reversed to recover the password.
     */
    static String hashPassword(String plainPassword) {
        String safePassword = plainPassword == null ? "" : plainPassword;
        try {
            byte[] salt = new byte[SALT_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(salt);
            byte[] derived = derive(safePassword.toCharArray(), salt);
            return encode(salt) + SEPARATOR + encode(derived);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    /**
     * Verifies {@code plainPassword} against a value previously produced by
     * {@link #hashPassword(String)}. Falls back to a constant-time comparison
     * against legacy clear-text records (created before hashing was introduced)
     * so existing accounts keep working.
     */
    static boolean verifyPassword(String plainPassword, String storedValue) {
        if (storedValue == null) {
            return false;
        }
        String safePassword = plainPassword == null ? "" : plainPassword;
        int separatorIndex = storedValue.indexOf(SEPARATOR);
        if (separatorIndex <= 0) {
            // Legacy clear-text record: compare directly (constant time).
            return constantTimeEquals(
                    safePassword.getBytes(), storedValue.getBytes());
        }
        try {
            byte[] salt = decode(storedValue.substring(0, separatorIndex));
            byte[] expected = decode(storedValue.substring(separatorIndex + 1));
            byte[] actual = derive(safePassword.toCharArray(), salt);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    private static String encode(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private static byte[] decode(String data) {
        return Base64.decode(data, Base64.NO_WRAP);
    }
}
