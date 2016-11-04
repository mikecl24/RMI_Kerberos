package ALSession;

/**
 * Created by 0x18 on 12/10/2016.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;

public class ClientSession {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {

        PrintServiceSession PrintService = (PrintServiceSession) Naming.lookup("rmi://localhost:5099/print");
        AuthenticationServiceSession AuthService = (AuthenticationServiceSession) Naming.lookup("rmi://localhost:5100/auth");
        TGSServiceSession TGSService = (TGSServiceSession) Naming.lookup("rmi://localhost:5101/tgs");

        //setup time
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        /* REQUEST AUTHENTICATION */
        String auth = AuthService.authenticate("User1", "Password1",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        if (auth.equals("")){
            System.out.println("Authentication Failed");
            System.exit(0);
        }

        //split return (Key(client, tgs), {|username, Key(client, tgs), time|}K(AS,TGS))
        //get key from first element
        String[] val = auth.split(":");
        String TGSkey = val[0];
        //get encrypted bytes from the rest (ASTGS encryption)
        auth = auth.substring(TGSkey.length()+1);
        TGSkey = TGSkey.substring(0,16);
        byte[] encr = String2ByteArray(auth);

        //use gotten key between client and tgs to encrypt time to send TGS
        cal.setTime(date);
        String st = Integer.toString(cal.get(Calendar.YEAR)) + Integer.toString(cal.get(Calendar.MONTH))+ Integer.toString(cal.get(Calendar.DATE))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+Integer.toString(cal.get(Calendar.MINUTE));
        byte[] encrypted = encryptfucntion(st, TGSkey);


        /* REQUEST A TICKET */
        String[] TGS = TGSService.getTicket(encrypted,encr).split("W");

        if (TGS.length != 2){
            System.out.println("Ticket Retreival Failed");
            System.exit(0);
        }
        String[] TGSClientBytes = TGS[0].split(":");
        encr = new byte[TGSClientBytes.length];

        for (int i = 0; i<TGSClientBytes.length; i++){
            encr[i] = (byte) Integer.parseInt(TGSClientBytes[i]);
        }
        //decrypt 1st part of the message
        String decrypted ="";
        decrypted = decryptfunction(TGS[0],TGSkey);
        //remove service, since there is only print service currently
        decrypted = decrypted.split(":")[1];

        //Part 2 still encrypted! TGS[1] K(TGS,Service) It is the ticket
        //encrypt time using new key received
        cal.setTime(date);
        st = Integer.toString(cal.get(Calendar.YEAR)) + Integer.toString(cal.get(Calendar.MONTH))+ Integer.toString(cal.get(Calendar.DATE))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+Integer.toString(cal.get(Calendar.MINUTE));
        encr = encryptfucntion(st, decrypted);
        String ticket = TGS[1];

        /* CONFIRM THE SESSION WITH SERVICE */
        String SessionConfirmation = PrintService.startSession(encr, ticket);
        //verify server answer!
        String add = st.charAt(st.length()-1)+"";
        st = st.substring(0,st.length()-2);
        st = st + (Integer.parseInt(add)+1);
        if (!decryptfunction(SessionConfirmation, decrypted).equals(st)){
            System.out.println("Server verification failed");
            System.exit(0);
        }

        /* DO ANY ACTIONS IN PRINT SERVICE HERE */
        System.out.println("---- " + PrintService.start(ticket));
        System.out.println("---- " + PrintService.print("file1.txt", "printer1", ticket));
        System.out.println("---- " + PrintService.setConfig("Page", "A4", ticket));
        System.out.println("---- " + PrintService.readConfig("Page", ticket));
        System.out.println("---- " + PrintService.status(ticket));
        System.out.println("---- " + PrintService.stop(ticket));
        System.out.println("---- " + PrintService.status(ticket));
        System.out.println("---- " + PrintService.print("file2.txt", "printer1", ticket));
        System.out.println("---- " + PrintService.start(ticket));
        System.out.println("---- " + PrintService.print("file2.txt", "printer1", ticket));
        System.out.println("---- " + PrintService.print("file3.txt", "printer1", ticket));
        System.out.println("---- " + PrintService.print("file4.txt", "printer1", ticket));
        System.out.println("---- " + PrintService.queue(ticket));
        System.out.println("---- " + PrintService.topQueue(10, ticket));
        System.out.println("---- " + PrintService.topQueue(3, ticket));
        System.out.println("---- " + PrintService.queue(ticket));
        System.out.println("---- " + PrintService.restart(ticket));
        System.out.println("---- " + PrintService.queue(ticket));
    }

    private static String decryptfunction(String st, String key){
        //Transform string to byte array
        String[] IndByte = st.split(":");
        byte[] encr = new byte[IndByte.length];

        for (int i = 0; i<IndByte.length; i++){
            encr[i] = (byte) Integer.parseInt(IndByte[i]);
        }
        String decrypted ="";
        try {
            Key aesKey = new SecretKeySpec(key.substring(0,16).getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(encr));
            //System.out.println(decrypted);
        }catch (Exception e){
            e.printStackTrace();
        }
        return decrypted;
    }

    private static byte[] encryptfucntion(String st, String key){
        byte[] encrypted;
        String TGSkey = key.substring(0,16);
        try {
            Key aesKey = new SecretKeySpec(TGSkey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            encrypted = cipher.doFinal(st.getBytes());
        }catch (Exception e){
            e.printStackTrace();
            encrypted = null;
        }
        return encrypted;
    }

    private static byte[] String2ByteArray(String st){
        String[] subST = st.split(":");
        byte[] encr = new byte[subST.length];
        for (int i = 0; i<subST.length; i++){
            encr[i] = (byte) Integer.parseInt(subST[i]);
        }
        return encr;
    }
}
