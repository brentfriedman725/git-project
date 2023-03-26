package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.TreeMap;

public class Commit implements Serializable {

    /** Contains the message of this commit. */
    private String message;
    /** Contains the first parent of this commit. */
    private String parent1;
    /** Contains the second parent of this commit. */
    private String parent2;
    /** Contains the date of this commit.*/
    private String date;


    /** TreeMap containing file names as keys and Sha-1's as [Sha1] as values.*/
    private TreeMap<String, String> blobs;
    public Commit(String m, String p1,
                  String p2, TreeMap<String, String> toAdd) {
        message = m;
        parent1 = p1;
        parent2 = p2;
        if (p1 == null && p2 == null) {
            date = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            Date now = new Date();
            Formatter format = new Formatter();
            format.format("%ta %tb %te %tH:%tM:%tS %tY %tz",
                    now, now, now, now, now, now, now, now);
            date = format.toString();
        }
        blobs = toAdd;




    }


    public TreeMap getBlobs() {
        return blobs;
    }

    public String getParent1() {
        return parent1;
    }

    public java.lang.String getParent2() {
        return parent2;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }


}
