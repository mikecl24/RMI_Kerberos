package ALIndividualRequest;

/**
 * Created by 0x18 on 12/10/2016.
 */

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {

        PrintService service = (PrintService) Naming.lookup("rmi://localhost:5099/print");

        System.out.println("---- " + service.start("User1", "Password1"));
        System.out.println("---- " + service.setConfig("Page", "A3", "User1", "Password1"));
        System.out.println("---- " + service.readConfig("Page", "User1", "Password1"));
        System.out.println("---- " + service.status("User1", "Password1"));
        System.out.println("---- " + service.start("User1", "Password1"));
        System.out.println("---- " + service.status("User1", "Password1"));
        System.out.println("---- " + service.print("file2.txt", "printer1", "User1", "Password1"));
        System.out.println("---- " + service.queue("User2", "Password2"));
        System.out.println("---- " + service.topQueue(3, "User1", "Password1"));
        System.out.println("---- " + service.restart("User1", "Password1"));
        System.out.println("---- " + service.queue("User1", "Password1"));
    }
}
