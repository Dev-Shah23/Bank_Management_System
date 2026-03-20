import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES.java
 * Handles AES-128 encryption and decryption for sensitive data
 * such as passwords and account details.
 * Concept: Data Integrity / Security
 */
public class AES {

    private static final String ALGORITHM = "AES";

    // Fixed secret key (16 bytes = 128-bit AES)
    // In a real system, store this securely (e.g., environment variable)
    private static final String SECRET_KEY = "BankSecureKey123";

    /**
     * Encrypts a plain text string using AES.
     * @param plainText the text to encrypt
     * @return Base64-encoded encrypted string
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.out.println("[AES] Encryption failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypts an AES-encrypted Base64 string.
     * @param encryptedText the encrypted Base64 string
     * @return original plain text
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.out.println("[AES] Decryption failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies if a plain text matches an encrypted value.
     * Used during login to verify passwords.
     * @param plainText the raw input (e.g., password typed by user)
     * @param encryptedText the stored encrypted password
     * @return true if they match
     */
    public static boolean verify(String plainText, String encryptedText) {
        String encryptedInput = encrypt(plainText);
        return encryptedInput != null && encryptedInput.equals(encryptedText);
    }
}
