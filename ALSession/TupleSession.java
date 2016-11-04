package ALSession;


/**
 * Created by acl on 10/30/16.
 */
public class TupleSession {
    public byte[] hashpw;
    public byte[] salt;

    public TupleSession(byte[] string1, byte[] string2){
        hashpw = string1;
        salt = string2;
    }
}
