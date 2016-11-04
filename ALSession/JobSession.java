package ALSession;

/**
 * Created by 0x18 on 12/10/2016.
 */
public class JobSession {
    private String filename;
    private int jobNum;

    public JobSession(String filename, int jobNum){
        this.filename = filename;
        this.jobNum = jobNum;
    }

    public String getFilename(){
        return filename;
    }

    public int getJobNum(){
        return jobNum;
    }

    @Override
    public String toString() {
        return "<" + jobNum + "> <" + getFilename() + ">\n";
    }


    @Override
    public boolean equals(Object v){
        boolean retVal = false;

        if (v instanceof JobSession){
            JobSession ptr = (JobSession) v;
            retVal = ptr.getJobNum() == this.jobNum;
        }
        return retVal;

    }

    @Override
    public int hashCode() {
        return this.getJobNum();
    }
}
