package ALSession;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by 0x18 on 02/11/2016.
 */
public interface AuthenticationServiceSession extends Remote {
    public String authenticate(String user, String password, int year, int month, int day, int hour, int minute) throws RemoteException;

}
