package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Branch implements Serializable {

/** This is the Sha-1 ID of the commit this branch
 * points to of the form [Sha-1]. */
    private String commitPointer;
    /** The NAME of this branch. */
    private String name;
    /** The file this branch is stored in.*/
    private File branchFile;

    /** Creates a new branch object with
     *  name N and commitpointer COMMITID. */
    public Branch(String n, String commitID) throws IOException {
        name = n;
        commitPointer = commitID;
        branchFile = new File(".gitlet/branches/" + name);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            branchFile.createNewFile();
            Utils.writeObject(branchFile, this);
        }
    }


    /** Sets the commit pointer of this branch to COMMITID. */
    public void setCommitPointer(String commitID) {
        commitPointer = commitID;
        Utils.writeObject(branchFile, this);
    }
    public void setCommitPointerWithoutWrite(String commitID) {
        commitPointer = commitID;
    }


    /** Returns the Sha-1 ID of the current commit as [Sha-1]. */
    public String getCommitPointer() {
        return commitPointer;
    }
}
