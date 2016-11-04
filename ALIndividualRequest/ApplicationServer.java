package ALIndividualRequest;

import ALIndividualRequest.PrintServant;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by 0x18 on 12/10/2016.
 */
public class ApplicationServer {
    public static void main(String[] args) throws RemoteException {

        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("print", new PrintServant());
    }
}
