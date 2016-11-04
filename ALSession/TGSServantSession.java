package ALSession;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 0x18 on 02/11/2016.
 */
public class TGSServantSession extends UnicastRemoteObject implements TGSServiceSession{

    private String ASTGSkey = "fI2JODj5eoiwAFsa";
    private String BTGSkey = "ry932fh9319fifj0";

    public TGSServantSession() throws RemoteException{
        super();
    }

    @Override
    public String getTicket(byte[] encryptedClient, byte[] encryptedAS) throws RemoteException {

        String toReturn = "";
        String decrypted = "";
        try{
            Key aesKey = new SecretKeySpec(ASTGSkey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(encryptedAS));
            //System.err.println(decrypted);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String[] MessageAS = decrypted.split(":");
        //check clock synchronization and formatting
        if (MessageAS.length == 7
                &&Integer.parseInt(MessageAS[2]) == cal.get(Calendar.YEAR)
                && Integer.parseInt(MessageAS[3]) == cal.get(Calendar.MONTH)
                && Integer.parseInt(MessageAS[4]) == cal.get(Calendar.DATE)
                && Integer.parseInt(MessageAS[5]) == cal.get(Calendar.HOUR_OF_DAY)
                && Integer.parseInt(MessageAS[6]) == cal.get(Calendar.MINUTE)) {
            String ClientKey = MessageAS[1];
            String ClientUsername = MessageAS[0];

            decrypted = "";
            try{
                Key aesKey = new SecretKeySpec(ClientKey.substring(0,16).getBytes(), "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                decrypted = new String(cipher.doFinal(encryptedClient));
                //System.err.println(decrypted);
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
            cal.setTime(date);
            String timestamp = Integer.toString(cal.get(Calendar.YEAR)) + Integer.toString(cal.get(Calendar.MONTH))+ Integer.toString(cal.get(Calendar.DATE))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+Integer.toString(cal.get(Calendar.MINUTE));

            //verify time synch with client
            if (timestamp.equals(decrypted)){
                //return {|serviceName, Key(client, service)|}K(client,TGS), {|username, Key(client, service), timestamp, lifespan|}K(TGS,service)
                byte[] encrypted4C;
                SecureRandom random = new SecureRandom();
                String newKey = new BigInteger(130, random).toString(32);
                String st = "PrinterService:"+newKey;
                try {
                    Key aesKey = new SecretKeySpec(ClientKey.substring(0,16).getBytes(), "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    // encrypt the text
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    encrypted4C = cipher.doFinal(st.getBytes());
                }catch (Exception e){
                    e.printStackTrace();
                    encrypted4C = null;
                    return null;
                }

                for(byte a : encrypted4C){
                    int i = a;
                    toReturn = toReturn+":"+ Integer.toString(i);
                }
                toReturn = toReturn.substring(1)+"W";

                st = ClientUsername + ":" + newKey +":"
                        + cal.get(Calendar.YEAR) + ":" + cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.DATE) + ":" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
                        + ":1";
                byte[] encrypted4B;
                try {
                    Key aesKey = new SecretKeySpec(BTGSkey.getBytes(), "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    // encrypt the text
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    encrypted4B = cipher.doFinal(st.getBytes());
                }catch (Exception e){
                    e.printStackTrace();
                    encrypted4B = null;
                }
                String temp = "";
                for(byte a : encrypted4B){
                    int i = a;
                    temp = temp+":"+ Integer.toString(i);
                }
                toReturn = toReturn + temp.substring(1);
            }
        }
        return toReturn;
    }
}
