package ALSession;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TGSServiceSession extends Remote{

    public String getTicket(byte[] encryptedClient, byte[] encryptedAS) throws RemoteException;

}
