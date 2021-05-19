package git;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;

/**
 * Represents a git commit object. TODO: It's a good idea to give a
 * description here of what else this Class does at a high level.
 *
 * @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful comment
     * above them describing what that variable represents and how that variable is
     * used. We've provided one example for `message`.
     */

    public String parent;
    public String branch;
    public int depth;
    public ArrayList<String> filenames;
    public ArrayList<String> blobfilenames;
    public boolean isMerged = false;
    /** The message of this Commit. */
    public String message;
    public SuperDate timestamp;

    public Commit(String message) {
        this(message, new SuperDate());
    }

    public Commit(String message, SuperDate timestamp) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = null;
        this.filenames = new ArrayList<String>();
        this.blobfilenames = new ArrayList<String>();
    }

    @Override
    public String toString() {
        String str=" === Commit Object\n";
        str += "parent = " + parent + "\n";
        str += "branch = " + branch + "\n" ;
        str += "depth = " + depth + "\n" ;
        str += "filenames = " + filenames + "\n" ;
        str += "blobfilenames = " + blobfilenames + "\n" ;
        str += "======";
        return str;
    }
}
