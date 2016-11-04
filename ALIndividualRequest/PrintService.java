package ALIndividualRequest;

/**
 * Created by 0x18 on 12/10/2016.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintService extends Remote{

    // prints file filename on the specified printer
    public String print(String filename, String printer, String user, String password) throws RemoteException;

    // lists the print queue on the user's display in lines of the form <job number>   <file name>
    public String queue(String user, String password) throws RemoteException;

    // moves job to the top of the queue
    public String topQueue(int job, String user, String password) throws RemoteException;

    // starts the print server
    public String start(String user, String password) throws RemoteException;

    // stops the print server
    public String stop(String user, String password) throws RemoteException;

    // stops the print server, clears the print queue and starts the print server again
    public String restart(String user, String password) throws RemoteException;

    // prints status of printer on the user's display
    public String status(String user, String password) throws RemoteException;

    // prints the value of the parameter on the user's display
    public String readConfig(String parameter, String user, String password) throws RemoteException;

    // sets the parameter to value
    public String setConfig(String parameter, String value, String user, String password) throws RemoteException;
}
