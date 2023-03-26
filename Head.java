package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Head implements Serializable {
    /** This is the name of the branch file
     *  name of the form [branchName].txt. */
    private String branchPointer;
    /** Contains the file that the head is stored in. */
    private File headFile;

    public Head(String branchFileName) throws IOException {
        branchPointer = branchFileName;
        headFile = new File(".gitlet/head.txt");
        if (headFile.exists()) {
            throw new GitletException("Head already exists!");
        } else {
            headFile.createNewFile();
            Utils.writeObject(headFile, this);
        }
    }

    /**Sets the head to be on BRANCH. BRANCH must be of form [branchName].txt.*/
    public void setHead(String branch) {
        branchPointer = branch;
        Utils.writeObject(headFile, this);
    }


    /** Returns the name of the branch
     *  the head points to of form [branchName].txt. */
    public String getHead() {
        return branchPointer;
    }
}
