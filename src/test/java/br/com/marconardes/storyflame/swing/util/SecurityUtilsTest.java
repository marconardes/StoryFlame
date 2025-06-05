package br.com.marconardes.storyflame.swing.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// No need to import NoSuchAlgorithmException for tests if SecurityUtils handles it internally
// and tests assert behavior based on that handling (e.g. returning null or throwing runtime).
// However, if a test *itself* needs to call a method that declares it, it would be needed.
// SecurityUtils.hashPassword and verifyPassword now internally catch NoSuchAlgorithmException
// and print to stderr / return null/false. So tests don't need to declare it.

class SecurityUtilsTest {

    @Test
    void testHashPassword_NonNullOutput() {
        String hash = SecurityUtils.hashPassword("password123");
        assertNotNull(hash, "Hash should not be null for valid input.");
        assertFalse(hash.isEmpty(), "Hash should not be empty for valid input.");
    }

    @Test
    void testHashPassword_ConsistentOutput() {
        String pass = "testPassword";
        String hash1 = SecurityUtils.hashPassword(pass);
        String hash2 = SecurityUtils.hashPassword(pass);
        assertEquals(hash1, hash2, "Hashing the same password twice should produce the same hash.");
    }

    @Test
    void testHashPassword_DifferentForDifferentPasswords() {
        String pass1 = "password123";
        String pass2 = "password456";
        String hash1 = SecurityUtils.hashPassword(pass1);
        String hash2 = SecurityUtils.hashPassword(pass2);
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes.");
    }

    @Test
    void testVerifyPassword_CorrectPassword() {
        String plainPassword = "securePass";
        String hashedPassword = SecurityUtils.hashPassword(plainPassword);
        assertTrue(SecurityUtils.verifyPassword(plainPassword, hashedPassword),
                "Verification should succeed for the correct password.");
    }

    @Test
    void testVerifyPassword_IncorrectPassword() {
        String correctPassword = "securePass";
        String incorrectPasswordAttempt = "wrongPass";
        String hashedPassword = SecurityUtils.hashPassword(correctPassword);
        assertFalse(SecurityUtils.verifyPassword(incorrectPasswordAttempt, hashedPassword),
                "Verification should fail for an incorrect password.");
    }

    @Test
    void testHashAndVerify_EmptyPassword() {
        String emptyPassword = "";
        String hashedPassword = SecurityUtils.hashPassword(emptyPassword);
        assertNotNull(hashedPassword, "Hashing an empty string should produce a non-null hash.");
        assertFalse(hashedPassword.isEmpty(), "Hash of an empty string should not be empty.");
        assertTrue(SecurityUtils.verifyPassword(emptyPassword, hashedPassword),
                "Verification should succeed for an empty password and its hash.");
    }

    @Test
    void testHashPassword_NullInput_ShouldThrowNullPointerException() {
        // SecurityUtils.hashPassword calls password.getBytes(), which will throw NPE if password is null.
        assertThrows(NullPointerException.class, () -> {
            SecurityUtils.hashPassword(null);
        }, "Hashing a null password should throw NullPointerException.");
    }

    @Test
    void testVerifyPassword_NullPlainPassword_ShouldResultInFalseOrException() {
        // SecurityUtils.verifyPassword calls hashPassword(plainPassword).
        // If plainPassword is null, the defensive check `if (plainPassword == null || hashedPassword == null)`
        // in verifyPassword should catch this and return false.
        String hashedPassword = SecurityUtils.hashPassword("somePassword");
        assertFalse(SecurityUtils.verifyPassword(null, hashedPassword),
                "Verification should fail if plain password is null (due to internal check).");
    }

    @Test
    void testVerifyPassword_NullHashedPassword_ShouldReturnFalse() {
        // The defensive check `if (plainPassword == null || hashedPassword == null)`
        // in verifyPassword should catch this and return false.
        String plainPasswordToHash = "anyPassword";
        assertFalse(SecurityUtils.verifyPassword(plainPasswordToHash, null),
                "Verification should fail if hashed password is null (due to internal check).");
    }

    @Test
    void testVerifyPassword_BothNull_ShouldReturnFalse() {
        // The defensive check `if (plainPassword == null || hashedPassword == null)`
        // in verifyPassword should catch this and return false.
        assertFalse(SecurityUtils.verifyPassword(null, null),
                "Verification should fail if both passwords are null (due to internal check).");
    }

}
