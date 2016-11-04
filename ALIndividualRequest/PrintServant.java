package ALIndividualRequest;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


/**
 * Created by 0x18 on 12/10/2016.
 */

public class PrintServant extends UnicastRemoteObject implements PrintService{
    private int jobNumber = 0;
    private LinkedList<Job> LocalQueue = new LinkedList();
    private boolean serverStart = false;
    private HashMap Config = new HashMap();

    //simulated database
    private HashMap database = new HashMap();

    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    public PrintServant() throws RemoteException{
        super();

        //authentication data structures populating

        byte[] salt = getNextSalt();
        database.put("User1", new Tuple(hash("Password1".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User2", new Tuple(hash("Password2".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User3", new Tuple(hash("Password3".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User4", new Tuple(hash("Password4".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User5", new Tuple(hash("Password5".toCharArray(), salt), salt));
        salt = getNextSalt();
        database.put("User6", new Tuple(hash("Password6".toCharArray(), salt), salt));

        //create remote objects here for printers
    }

    public static boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] pwdHash = hash(password, salt);
        Arrays.fill(password, Character.MIN_VALUE);
        if (pwdHash.length != expectedHash.length) return false;
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

    public static byte[] getNextSalt() {
        byte[] salt = new byte[12];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hash(char[] password, byte[] salt) {
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


    @Override
    public String print(String filename, String printer, String user, String password) throws RemoteException{
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)){
            if(serverStart) {
                LocalQueue.add(new Job(filename, jobNumber));
                jobNumber++;
                //send the request to the printer
                System.out.println("Printing: " + filename);
                return "From Server: " + "Added file " + filename + " to queue of " + printer + " in position " + LocalQueue.size();
            }
            else
                return "From Server: Error, server not started";

        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String queue(String user, String password) {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                String queueStr = "From Server:\n";
                for (Job a : LocalQueue) {
                    queueStr += a.toString();
                }
                return queueStr;
            } else
                return "From Server: Error, server not started";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String topQueue(int job, String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                int index = LocalQueue.indexOf(new Job("", job));
                if (index != -1) {
                    Job temp = LocalQueue.get(index);
                    LocalQueue.remove(index);
                    LocalQueue.add(0, temp);
                    return "From Server: Moved job " + job + " to the top";
                } else
                    return "From Server: Job is not in queue";
            } else
                return "From Server: Error, server not started";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String start(String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                return "From Server: Error, server already started";
            }
            else {
                serverStart = true;
                return "From Server: Server successfully started";
            }
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String stop(String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if(serverStart) {
                serverStart = false;
                return "From Server: Server successfully stopped";
            }
            else
                return "From Server: Error, server not running";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String restart(String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            stop(user, password);
            LocalQueue.clear();
            start(user, password);
            return "From Server: Server successfully restarted";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String status(String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                return "From Server: Server is online";
            } else
                return "From Server: Server is offline";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String readConfig(String parameter, String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                if (Config.containsKey(parameter)) {
                    return "From Server: " + parameter + ": " + Config.get(parameter);
                } else
                    return "From Server: Error, no such parameter";
            }
            else
                return "From Server: Error, server not running";
        }
        return "Form Server: Error Authenticating";
    }

    @Override
    public String setConfig(String parameter, String value, String user, String password) throws RemoteException {
        if(isExpectedPassword(password.toCharArray(), ((Tuple)database.get(user)).salt, ((Tuple)database.get(user)).hashpw)) {
            if (serverStart) {
                Config.put(parameter, value);
                return "From Server: set " + parameter + " to " + value;
            }
            else
                return "From Server: Error, server not running";
        }
        return "Form Server: Error Authenticating";
    }

}
