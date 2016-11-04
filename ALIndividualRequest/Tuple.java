package ALIndividualRequest;

/**
 * Created by acl on 10/30/16.
 */
public class Tuple {
    public byte[] hashpw;
    public byte[] salt;

    public Tuple(byte[] string1, byte[] string2){
        hashpw = string1;
        salt = string2;
    }
}
