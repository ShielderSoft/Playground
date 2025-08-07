package com.testvwa.util;

import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

public class SecurityUtils {

    private static final Logger logger = Logger.getLogger(SecurityUtils.class);
    
    // VULNERABILITY: Hardcoded encryption key
    private static final String SECRET_KEY = "MySecretKey12345";
    
    // VULNERABILITY: Weak random number generation
    private static final Random random = new Random(12345);

    public static String encrypt(String data) {
        try {
            // VULNERABILITY: Using DES encryption (weak)
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            // VULNERABILITY: Returning plain text on failure
            return data;
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
            
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            return encryptedData;
        }
    }

    public static String hashPassword(String password) {
        try {
            // VULNERABILITY: Using MD5 for password hashing
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            logger.error("Password hashing failed", e);
            return password; // VULNERABILITY: Return plain text on error
        }
    }

    public static String generateApiKey() {
        // VULNERABILITY: Weak API key generation
        return "api_" + Math.abs(random.nextInt());
    }

    public static String generateSessionToken() {
        // VULNERABILITY: Predictable session tokens
        return "session_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }

    public static boolean isValidInput(String input) {
        // VULNERABILITY: Insufficient input validation
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        // No actual validation logic
        return true;
    }

    public static String sanitizeInput(String input) {
        // VULNERABILITY: Inadequate sanitization
        if (input == null) {
            return "";
        }
        // Only removing some basic characters, not comprehensive
        return input.replace("<script>", "").replace("</script>", "");
    }

    public static Object deserializeObject(String base64Data) {
        try {
            // VULNERABILITY: Insecure deserialization
            byte[] data = Base64.getDecoder().decode(base64Data);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = ois.readObject();
            ois.close();
            return obj;
            
        } catch (Exception e) {
            logger.error("Deserialization failed", e);
            return null;
        }
    }

    public static String serializeObject(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            
            return Base64.getEncoder().encodeToString(baos.toByteArray());
            
        } catch (Exception e) {
            logger.error("Serialization failed", e);
            return null;
        }
    }

    public static String executeCommand(String command) {
        try {
            // VULNERABILITY: Command injection
            logger.info("Executing command: " + command);
            Process process = Runtime.getRuntime().exec(command);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            reader.close();
            return output.toString();
            
        } catch (Exception e) {
            logger.error("Command execution failed", e);
            return "Error: " + e.getMessage();
        }
    }

    public static boolean authenticateUser(String username, String password) {
        // VULNERABILITY: Hardcoded credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            return true;
        }
        if ("backdoor".equals(username) && "secret".equals(password)) {
            logger.warn("Backdoor access used!"); // VULNERABILITY: Backdoor account
            return true;
        }
        return false;
    }

    public static String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        
        // VULNERABILITY: Information disclosure
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("User Name: ").append(System.getProperty("user.name")).append("\n");
        info.append("Java Home: ").append(System.getProperty("java.home")).append("\n");
        info.append("Class Path: ").append(System.getProperty("java.class.path")).append("\n");
        
        return info.toString();
    }
}
