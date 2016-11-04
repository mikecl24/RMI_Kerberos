package ALSession;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by 0x18 on 02/11/2016.
 */
public class AuthenticationServantSession extends UnicastRemoteObject implements AuthenticationServiceSession{

    //simulated database
    private HashMap database = new HashMap();
    private String ASTGSkey = "fI2JODj5eoiwAFsa";
    private HashMap key2encr = new HashMap();
    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public AuthenticationServantSession() throws RemoteException {
        super();

        //manually populate database
        byte[] salt = getNextSalt();
        database.put("User1", new TupleSession(hash("Password1".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User2", new TupleSession(hash("Password2".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User3", new TupleSession(hash("Password3".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User4", new TupleSession(hash("Password4".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User5", new TupleSession(hash("Password5".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User6", new TupleSession(hash("Password6".toCharArray(), salt), salt));
    }

    @Override
    public String authenticate(String user, String password, int year, int month, int day, int hour, int minute) throws RemoteException {
        byte[] toReturn;
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String newKey = "";
        //check clock synchronization
        if (year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DATE) && hour == cal.get(Calendar.HOUR_OF_DAY) && minute == cal.get(Calendar.MINUTE)) {
            if(database.get(user) != null && isExpectedPassword(password.toCharArray(), ((TupleSession)database.get(user)).salt, ((TupleSession)database.get(user)).hashpw)) {
                SecureRandom random = new SecureRandom();
                newKey = new BigInteger(130, random).toString(32);
                String ASTGSstring = user + ":" + newKey + ":" + cal.get(Calendar.YEAR) + ":" + cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.DATE) + ":" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
                try {
                    Key aesKey = new SecretKeySpec(ASTGSkey.getBytes(), "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    // encrypt the text
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    byte[] encrypted = cipher.doFinal(ASTGSstring.getBytes());
                    toReturn = encrypted.clone();

                }catch (Exception e){
                    e.printStackTrace();
                    toReturn = null;
                }
            }
            else {
                toReturn = null;
            }
        }
        else {
            toReturn = null;
        }

        String st = newKey;
        if (toReturn != null) {
            for (byte a : toReturn) {
                int i = a;
                st = st + ":" + Integer.toString(i);
            }
            return st;
        }
        return null;
    }



    private static boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] pwdHash = hash(password, salt);
        Arrays.fill(password, Character.MIN_VALUE);
        if (pwdHash.length != expectedHash.length) return false;
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

    private static byte[] getNextSalt() {
        byte[] salt = new byte[12];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }
}
