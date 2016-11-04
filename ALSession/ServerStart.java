package ALSession;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by 0x18 on 04/11/2016.
 */
public class ServerStart {
    public static void main(String[] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(5100);
        registry.rebind("auth", new AuthenticationServantSession());

        registry = LocateRegistry.createRegistry(5101);
        registry.rebind("tgs", new TGSServantSession());

        registry = LocateRegistry.createRegistry(5099);
        registry.rebind("print", new PrintServantSession());
    }
}
