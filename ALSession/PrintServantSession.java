package ALSession;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by 0x18 on 12/10/2016.
 */

public class PrintServantSession extends UnicastRemoteObject implements PrintServiceSession{
    private int jobNumber = 0;
    private LinkedList<JobSession> LocalQueue = new LinkedList();
    private boolean serverStart = false;
    private HashMap Config = new HashMap();
    //key with TGS service
    private String BTGSkey = "ry932fh9319fifj0";

    public PrintServantSession() throws RemoteException{
        super();
        //create remote objects here for printers
    }

    @Override
    public String startSession(byte[] time, String ticket) throws RemoteException {

        //decrypt ticket:
        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        String UserKey = ticketSub[1];

        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        String timeS = "";
        if (time != null) {
            for (byte a : time) {
                int i = a;
                timeS = timeS + ":" + Integer.toString(i);
            }
            timeS = timeS.substring(1);
        }
        else
            return "From server: time synchronization issue!";
        timeS = decryptfunction(timeS, UserKey);


        String st = Integer.toString(cal.get(Calendar.YEAR)) + Integer.toString(cal.get(Calendar.MONTH))+ Integer.toString(cal.get(Calendar.DATE))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+Integer.toString(cal.get(Calendar.MINUTE));
        if (!st.equals(timeS)){
            return "From server: time synchronization issue!";
        }

        String add = st.charAt(st.length()-1)+"";
        st = st.substring(0,st.length()-2);
        st = st + (Integer.parseInt(add)+1);

        byte[] encr = encryptfucntion(st, UserKey);
        st = "";
        for (byte a : encr) {
            int i = a;
            st = st + ":" + Integer.toString(i);
        }
        st = st.substring(1);
        //return {| t+1 |}K(client, service)
        return st;
    }

    @Override
    public String print(String filename, String printer, String ticket) throws RemoteException{
        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(serverStart) {
            LocalQueue.add(new JobSession(filename, jobNumber));
            jobNumber++;
            //send the request to the printer
            System.out.println("Printing: " + filename);
            return "From Server: " + "Added file " + filename + " to queue of " + printer + " in position " + LocalQueue.size();
        }
        else
            return "From Server: Error, server not started";
    }

    @Override
    public String queue(String ticket) {
        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";

        if(serverStart) {
            String queueStr = "From Server:\n";
            for (JobSession a: LocalQueue) {
                queueStr += a.toString();
            }
            return queueStr;
        }
        else
            return "From Server: Error, server not started";
    }

    @Override
    public String topQueue(int job, String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(serverStart) {
            int index = LocalQueue.indexOf(new JobSession("", job));
            if (index != -1) {
                JobSession temp = LocalQueue.get(index);
                LocalQueue.remove(index);
                LocalQueue.add(0, temp);
                return "From Server: Moved job " + job + " to the top";
            } else
                return "From Server: Job is not in queue";
        }
        else
            return "From Server: Error, server not started";
    }

    @Override
    public String start(String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(serverStart) {
            return "From Server: Error, server already started";
        }
        else {
            serverStart = true;
            return "From Server: Server successfully started";
        }
    }

    @Override
    public String stop(String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(serverStart) {
            serverStart = false;
            return "From Server: Server successfully stopped";
        }
        else {
            return "From Server: Error, server not running";
        }
    }

    @Override
    public String restart(String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        stop(ticket);
        LocalQueue.clear();
        jobNumber = 0;
        start(ticket);
        return "From Server: Server successfully restarted";
    }

    @Override
    public String status(String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(serverStart) {
            return "From Server: Server is online";
        }
        else {
            return "From Server: Server is offline";
        }
    }

    @Override
    public String readConfig(String parameter, String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        if(Config.containsKey(parameter)){
            return "From Server: " + parameter + ": " + Config.get(parameter);
        }
        else
            return "From Server: Error, no such parameter";
    }

    @Override
    public String setConfig(String parameter, String value, String ticket) throws RemoteException {

        String Ticket = decryptfunction(ticket ,BTGSkey);
        String[] ticketSub = Ticket.split(":");
        if (!ticketCheck(ticketSub))
            return "From server: ticket invalid!";


        Config.put(parameter, value);
        return "From Server: set " + parameter + " to " + value;
    }

    private static byte[] String2ByteArray(String st){
        String[] subST = st.split(":");
        byte[] encr = new byte[subST.length];
        for (int i = 0; i<subST.length; i++){
            encr[i] = (byte) Integer.parseInt(subST[i]);
        }
        return encr;
    }

    private String decryptfunction(String st, String key){
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

    private byte[] encryptfucntion(String st, String key){
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

    private boolean ticketCheck(String[] ticketSub){
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        //check if ticket is still valid:
        if(ticketSub.length == 8 //correct format check
                && Integer.parseInt(ticketSub[2]) == cal.get(Calendar.YEAR) && Integer.parseInt(ticketSub[3]) == cal.get(Calendar.MONTH)
                && Integer.parseInt(ticketSub[4]) == cal.get(Calendar.DATE)
                && ((Integer.parseInt(ticketSub[5]) == cal.get(Calendar.HOUR_OF_DAY) && Integer.parseInt(ticketSub[6]) >= cal.get(Calendar.MINUTE))
                || (Integer.parseInt(ticketSub[5]) == cal.get(Calendar.HOUR_OF_DAY)+1 && Integer.parseInt(ticketSub[6]) <= cal.get(Calendar.MINUTE)) )){
            return true;
        }
        else
            return false;
    }

}
