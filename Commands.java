package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class Commands {

    /** Represents the maximum length of an
     * abbreviated commit ID. */
    public static final int MAX_COMMIT_LENGTH = 40;

    public static void init() throws IOException {
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File branches = new File(".gitlet/branches");
        branches.mkdir();
        File commits = new File(".gitlet/commits");
        commits.mkdir();
        Commit initial = new Commit(
                "initial commit", null, null, new TreeMap<>());
        String initialCommit = Utils.sha1(Utils.serialize(initial));
        File initCom = new File(".gitlet/commits/" + initialCommit + ".txt");
        initCom.createNewFile();
        Utils.writeObject(initCom, initial);
        File stagingArea = new File(".gitlet/stagingArea");
        stagingArea.mkdir();
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        stageAdd.mkdir();
        stageRemove.mkdir();
        File blobs = new File(".gitlet/blobs");
        blobs.mkdir();
        new Branch("master.txt", initialCommit);
        new Head("master.txt");
        File remotes = new File(".gitlet/remotes");
        remotes.mkdir();

    }

    public static void add(String fileName) throws IOException {
        File fileInCWD = new File(fileName);
        if (!fileInCWD.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob fileBlob = new Blob(fileInCWD);
        File temp = new File(".gitlet/blobTemp.txt");
        Utils.writeObject(temp, fileBlob);
        String sha1 = Utils.sha1(
                Utils.serialize(Utils.readObject(temp, Blob.class)));
        temp.delete();
        File toAddName = new File(".gitlet/stagingArea/stageAdd/" + fileName);
        File inStageRemove = new File(
                ".gitlet/stagingArea/stageRemove/" + fileName);
        Commit currentCommit = getCurrentCommit();

        if (toAddName.exists()) {
            toAddName.delete();
        }
        if (inStageRemove.exists()) {
            inStageRemove.delete();
        }
        if (currentCommit.getBlobs().containsKey(fileName)
                && currentCommit.getBlobs().get(fileName).equals(sha1)) {
            return;
        } else  {
            toAddName.createNewFile();
            Utils.writeObject(toAddName, fileBlob);
        }


    }

    public static void commit(
            String message, String givenbranch) throws IOException {
        Commit currentCommit = getCurrentCommit();
        String parent1 = Utils.sha1(
                Utils.serialize(currentCommit));
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        File[] stageAddLst = stageAdd.listFiles();
        File[] stageRemoveLst = stageRemove.listFiles();
        Commit newCommit;
        @SuppressWarnings("unchecked") TreeMap<String, String> blobs =
                currentCommit.getBlobs();
        if (stageAddLst.length == 0 && stageRemoveLst.length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        for (File f : stageAddLst) {
            Blob contents = Utils.readObject(f, Blob.class);
            String sha1 = Utils.sha1(Utils.serialize(contents));
            blobs.put(f.getName(), sha1);
            File blobFile = new File(".gitlet/blobs/" + sha1 + ".txt");
            blobFile.createNewFile();
            Utils.writeObject(blobFile, contents);
            f.delete();
        }
        for (File fD: stageRemoveLst) {
            blobs.remove(fD.getName());
            fD.delete();

        }
        if (givenbranch == null) {
            newCommit = new Commit(message, parent1, null, blobs);
        } else {
            File checkedOutBranchFile =
                    new File(".gitlet/branches/" + givenbranch + ".txt");
            Branch checkedOutBranch =
                    Utils.readObject(checkedOutBranchFile, Branch.class);
            newCommit = new Commit(
                    message, parent1,
                    checkedOutBranch.getCommitPointer(), blobs);
        }
        String newCommSha1 = Utils.sha1(Utils.serialize(newCommit));
        File commit = new File(".gitlet/commits/" + newCommSha1 + ".txt");
        commit.createNewFile();
        Utils.writeObject(commit, newCommit);
        Branch currentBranch = getCurrentBranch();
        currentBranch.setCommitPointer(newCommSha1);

    }


    public static void log() {
        Commit c = getCurrentCommit();
        while (true) {
            String date = "Date: " + c.getDate();
            String message = c.getMessage();
            String sha1 = Utils.sha1(Utils.serialize(c));
            System.out.println("===");
            System.out.println("commit " + sha1);
            System.out.println(date);
            System.out.println(message);
            System.out.println();
            if (c.getParent1() == null && c.getParent2() == null) {
                break;
            }
            File commFile =
                    new File(".gitlet/commits/" + c.getParent1() + ".txt");
            c = Utils.readObject(commFile, Commit.class);
        }
    }

    public static void globalLog() {
        File commitsDir = new File(".gitlet/commits");
        List<String> commits = Utils.plainFilenamesIn(commitsDir);
        for (String c : commits) {
            File commitFile = new File(".gitlet/commits/" + c);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            String date = "Date: " + commit.getDate();
            String message = commit.getMessage();
            String sha1 = c.substring(0, c.length() - 4);
            System.out.println("===");
            System.out.println("commit " + sha1);
            System.out.println(date);
            System.out.println(message);
            System.out.println();
        }
    }

    public static void find(String m) {
        Boolean foundACommit = false;
        File commitsDir = new File(".gitlet/commits");
        List<String> commits = Utils.plainFilenamesIn(commitsDir);
        for (String c : commits) {
            File commitFile = new File(".gitlet/commits/" + c);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (commit.getMessage().equals(m)) {
                foundACommit = true;
                System.out.println(Utils.sha1(Utils.serialize(commit)));
            }
        }

        if (!foundACommit) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }

    }

    public static void branch(String branchName) throws IOException {
        File branchExists = new File(".gitlet/branches" + branchName + ".txt");
        if (branchExists.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit currentCommit = getCurrentCommit();
        new Branch(branchName + ".txt",
                Utils.sha1(Utils.serialize(currentCommit)));
    }

    public static void rm(String fileName) throws IOException {
        Commit currComm = getCurrentCommit();
        File fileInCWD = new File("" + fileName);
        File toAddName = new File(".gitlet/stagingArea/stageAdd/" + fileName);
        File toRemoveName =
                new File(".gitlet/stagingArea/stageRemove/" + fileName);
        if (!toAddName.exists() && !currComm.getBlobs().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (toAddName.exists()) {
            toAddName.delete();
        }
        if (currComm.getBlobs().containsKey(fileName)) {
            toRemoveName.createNewFile();
            if (fileInCWD.exists()) {
                fileInCWD.delete();
            }
        }
    }


    public static void checkout1(String fileName) throws IOException {
        Commit currComm = getCurrentCommit();
        generalCheckoutAlg(currComm, fileName);
    }

    public static void checkout2(
            String commID, String fileName) throws IOException {
        File commitFile = new File(".gitlet/commits/" + commID + ".txt");
        if (commID.length() < MAX_COMMIT_LENGTH) {
            File commitSearch = new File(".gitlet/commits");
            List<String> allCommits = Utils.plainFilenamesIn(commitSearch);
            for (String s : allCommits) {
                if (s.startsWith(commID)) {
                    commitFile = new File(".gitlet/commits/" + s);
                    break;
                }
            }
        }

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        generalCheckoutAlg(commit, fileName);

    }

    public static void checkout3(String branchName) throws IOException {
        branchName = stupidCheckoutHelper(branchName);
        File checkedOutBranchFile =
                new File(".gitlet/branches/" + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        checkout3Errors(branchName);
        Branch checkedOutBranch =
                Utils.readObject(checkedOutBranchFile, Branch.class);
        File checkedOutCommFile =
                new File(".gitlet/commits/"
                        + checkedOutBranch.getCommitPointer() + ".txt");
        Commit checkedOutComm =
                Utils.readObject(checkedOutCommFile, Commit.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> checkedOutTracked
                = checkedOutComm.getBlobs();
        @SuppressWarnings("unchecked") TreeMap<String, String> currTracked
                = getCurrentCommit().getBlobs();
        Set<String> checkedOutkeys =
                checkedOutTracked.keySet();
        for (String key : checkedOutkeys) {
            File fileToOverWrite = new File("" + key);
            if (!currTracked.containsKey(key) && fileToOverWrite.exists()) {
                System.out.println(
                        "There is an untracked file in the way;"
                                + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String key : checkedOutkeys) {
            Blob blob = Utils.readObject(
                    new File(
                            ".gitlet/blobs/"
                                    + checkedOutTracked.get(key)
                                    + ".txt"), Blob.class);
            File fileToOverWrite = new File("" + key);
            if (fileToOverWrite.exists()) {
                fileToOverWrite.delete();
            }
            fileToOverWrite.createNewFile();
            Utils.writeContents(fileToOverWrite, blob.getContents());
        }
        Set<String> currentKeys = currTracked.keySet();
        for (String key : currentKeys) {
            File fileToDelete = new File(key);
            if (!checkedOutTracked.containsKey(key)) {
                fileToDelete.delete();
            }
        }
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        File[] stageAddLst = stageAdd.listFiles();
        File[] stageRemoveLst = stageRemove.listFiles();
        for (File f : stageAddLst) {
            f.delete();
        }
        for (File f : stageRemoveLst) {
            f.delete();
        }
        head.setHead(branchName + ".txt");
    }

    private static String stupidCheckoutHelper(String branchName) {
        if (branchName.contains("/")) {
            branchName = branchName.replace("/", "");
        }
        return branchName;
    }

    private static void checkout3Errors(String branchName) {
        File checkedOutBranchFile =
                new File(".gitlet/branches/" + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();
        if (headBranch.equals(branchName + ".txt")) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!checkedOutBranchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
    }

    public static void rmBranch(String branchName) {
        File branch = new File(".gitlet/branches/" + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (headBranch.equals(branchName + ".txt")) {
            System.out.println("Cannot remove the current branch");
            System.exit(0);
        }
        branch.delete();
    }

    public static void reset(String commitID) throws IOException {
        File commitFile = new File(".gitlet/commits/" + commitID + ".txt");
        Branch currentBranch = getCurrentBranch();
        String fullSha1 = commitID;
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        File[] stageAddLst = stageAdd.listFiles();
        File[] stageRemoveLst = stageRemove.listFiles();
        if (commitID.length() < MAX_COMMIT_LENGTH) {
            fullSha1 = resetFullSha(commitID);
            commitFile = new File(".gitlet/commits/" + fullSha1);
        }
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        @SuppressWarnings("unchecked") TreeMap<String, String> checkedOutTracked
                = commit.getBlobs();
        @SuppressWarnings("unchecked") TreeMap<String, String> currTracked
                = getCurrentCommit().getBlobs();
        Set<String> checkedOutkeys = checkedOutTracked.keySet();
        Set<String> currentKeys = currTracked.keySet();
        for (String key : checkedOutkeys) {
            File fileToOverWrite = new File("" + key);
            if (!currTracked.containsKey(key) && fileToOverWrite.exists()) {
                System.out.println(
                        "There is an untracked file in the way;"
                                + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String key : checkedOutkeys) {
            Blob blob = Utils.readObject(
                    new File(".gitlet/blobs/"
                            + checkedOutTracked.get(key)
                            + ".txt"), Blob.class);
            File fileToOverWrite = new File(key);
            if (fileToOverWrite.exists()) {
                fileToOverWrite.delete();
            }
            fileToOverWrite.createNewFile();
            Utils.writeContents(fileToOverWrite, blob.getContents());
        }
        for (String key : currentKeys) {
            File fileToDelete = new File(key);
            if (!checkedOutTracked.containsKey(key)) {
                fileToDelete.delete();
            }
        }
        currentBranch.setCommitPointer(fullSha1);
        for (File f : stageAddLst) {
            f.delete();
        }
        for (File f : stageRemoveLst) {
            f.delete();
        }

    }

    public static String resetFullSha(String commitID) {
        File commitFile = new File(".gitlet/commits/" + commitID + ".txt");
        Branch currentBranch = getCurrentBranch();
        String fullSha1 = commitID;
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        File[] stageAddLst = stageAdd.listFiles();
        File[] stageRemoveLst = stageRemove.listFiles();
        if (commitID.length() < MAX_COMMIT_LENGTH) {
            File commitSearch = new File(".gitlet/commits");
            List<String> allCommits = Utils.plainFilenamesIn(commitSearch);
            for (String s : allCommits) {
                if (s.startsWith(commitID)) {
                    return s;

                }
            }
        }

        return "";
    }

    public static void status() {
        File branchDir = new File(".gitlet/branches");
        List<String> branches = Utils.plainFilenamesIn(branchDir);
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        List<String> cwdFiles = Utils.plainFilenamesIn(new File(Main.CWD));
        List<String> stageAddLst = Utils.plainFilenamesIn(stageAdd);
        List<String> stageRemoveLst = Utils.plainFilenamesIn(stageRemove);
        Commit currentComm = getCurrentCommit();
        @SuppressWarnings("unchecked") TreeMap<String, String> tracked
                = currentComm.getBlobs();
        Set<String> trackedNames = tracked.keySet();
        List<String> modNotStagedList = new ArrayList<String>();
        List<String> untrackedFiles = new ArrayList<String>();
        for (String key : trackedNames) {
            File inCWD = new File(key);
            File inStageRemove = new File(
                    ".gitlet/stagingArea/stageRemove/" + key);
            if (!inStageRemove.exists() && !inCWD.exists()) {
                modNotStagedList.add(key + " (deleted)");
                continue;
            } else if (!inCWD.exists()) {
                continue;
            }
            Blob inCWDBlob = new Blob(inCWD);
            String sha1CWD = Utils.sha1(Utils.serialize(inCWDBlob));
            if (!tracked.get(key).equals(sha1CWD)
                    && !stageAddLst.contains(key)) {
                modNotStagedList.add(key + " (modified)");
            }

        }
        for (String fileName : stageAddLst) {
            File inCWD = new File(fileName);
            File inAddFIle = new File(
                    ".gitlet/stagingArea/stageAdd/" + fileName);
            if (!inCWD.exists()) {
                modNotStagedList.add(fileName + " (deleted)");
                continue;
            }
            String sha1CWD = Utils.sha1(
                    Utils.serialize(Utils.readContentsAsString(inCWD)));
            String sha1Add = Utils.sha1(Utils.serialize(
                    Utils.readObject(inAddFIle, Blob.class).getContents()));
            if (!sha1Add.equals(sha1CWD)) {
                modNotStagedList.add(fileName + " (modified)");
            }
        }
        for (String fileName : cwdFiles) {
            File inAdd = new File(".gitlet/stagingArea/stageAdd/" + fileName);
            if (!inAdd.exists() && !tracked.containsKey(fileName)) {
                untrackedFiles.add(fileName);
            }

        }
        statusOut(branches, stageAddLst,
                stageRemoveLst, modNotStagedList, untrackedFiles);
    }

    public static void statusOut(List<String> branches,
                                 List<String> stageAddLst,
                                 List<String> stageRemoveLst,
                                 List<String> modNotStagedList,
                                 List<String> untrackedFiles) {

        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();

        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (headBranch.equals(branch)) {
                branch = "*" + branch;
            }
            System.out.println(branch.substring(0, branch.length() - 4));
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String add : stageAddLst) {
            System.out.println(add);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String remove : stageRemoveLst) {
            System.out.println(remove);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String not : modNotStagedList) {
            System.out.println(not);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String un : untrackedFiles) {
            System.out.println(un);
        }
    }


    public static void merge(String branchName) throws IOException {
        String branchNameForMessage = branchName;
        branchName = stupidHelper(branchName);
        File checkedOutBranchFile =
                new File(".gitlet/branches/" + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();
        Branch checkedOutBranch =
                Utils.readObject(checkedOutBranchFile, Branch.class);
        String commitMessage = "Merged " + branchNameForMessage
                + " into " + headBranch.substring(
                        0, headBranch.length() - 4) + ".";
        String stageAdd = ".gitlet/stagingArea/stageAdd/";
        String stageRemove = ".gitlet/stagingArea/stageRemove/";
        Set<String> allFiles = new HashSet<>();
        @SuppressWarnings("unchecked") TreeMap<String, String>
                splitMap = getSplitMap(branchName);
        @SuppressWarnings("unchecked") TreeMap<String, String>
                currMap = getCurrMap();
        @SuppressWarnings("unchecked") TreeMap<String, String>
                givenMap = getGivenMap(branchName);
        allFiles.addAll(splitMap.keySet());
        allFiles.addAll(currMap.keySet());
        allFiles.addAll(givenMap.keySet());
        boolean mergeConflict = false;
        moreMergeErrors(branchName);
        for (String fileName : allFiles) {
            notConflictCases(fileName, splitMap,
                    currMap, givenMap, checkedOutBranch,
                    stageAdd, stageRemove);
            mergeConflict = conflictCases1(fileName, splitMap,
                    currMap, givenMap, stageAdd);
            if (splitMap.containsKey(fileName) && !currMap.containsKey(fileName)
                    && givenMap.containsKey(fileName)
                    && !splitMap.get(fileName).equals(givenMap.get(fileName))) {
                String givenContent = mergeGetBlob(
                        givenMap.get(fileName)).getContents();
                String conflictString = "<<<<<<< HEAD\n"
                        + "=======\n"
                        + givenContent + ">>>>>>>\n";
                makeConflictedFile(fileName, conflictString, stageAdd);
                mergeConflict = true;
            }
            if (splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                    && !givenMap.containsKey(fileName)
                    && !splitMap.get(fileName).equals(currMap.get(fileName))) {
                String currContent = mergeGetBlob(
                        currMap.get(fileName)).getContents();
                String conflictString = "<<<<<<< HEAD\n"
                        + currContent
                        + "=======\n" + ">>>>>>>\n";
                makeConflictedFile(fileName, conflictString, stageAdd);
                mergeConflict = true;
            }
        }
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        commit(commitMessage, branchName);
    }

    private static String stupidHelper(String branchName) throws IOException {
        if (branchName.contains("/")) {
            branchName = branchName.replace("/", "");
        }
        mergeErrors(branchName);
        return branchName;
    }

    private static void makeConflictedFile(String fileName,
                                           String conflictString,
                                           String stageAdd) throws IOException {
        File inCWD = new File(fileName);
        inCWD.delete();
        inCWD.createNewFile();
        Utils.writeContents(inCWD, conflictString);
        Blob conflictedContents = new Blob(inCWD);
        File toAdd = new File(stageAdd + fileName);
        toAdd.createNewFile();
        Utils.writeObject(toAdd, conflictedContents);
    }

    private static boolean conflictCases1(String fileName,
                                          TreeMap<String, String> splitMap,
                                          TreeMap<String, String> currMap,
                                          TreeMap<String, String> givenMap,
                                          String stageAdd) throws IOException {
        boolean mergeConflict = false;
        if (splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && givenMap.containsKey(fileName)
                && !splitMap.get(fileName).equals(givenMap.get(fileName))
                && !splitMap.get(fileName).equals(currMap.get(fileName))
                && !currMap.get(fileName).equals(givenMap.get(fileName))) {
            File inCWD = new File(fileName);
            String currContent = mergeGetBlob(
                    currMap.get(fileName)).getContents();
            String givenContent = mergeGetBlob(
                    givenMap.get(fileName)).getContents();
            String conflictString = "<<<<<<< HEAD\n"
                    + currContent + "=======\n"
                    + givenContent + ">>>>>>>\n";
            inCWD.delete();
            inCWD.createNewFile();
            Utils.writeContents(inCWD, conflictString);
            Blob conflictedContents = new Blob(inCWD);
            File toAdd = new File(stageAdd + fileName);
            toAdd.createNewFile();
            Utils.writeObject(toAdd, conflictedContents);
            mergeConflict = true;

        }

        if (!splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && givenMap.containsKey(fileName)
                && !currMap.get(fileName).equals(givenMap.get(fileName))) {
            File inCWD = new File(fileName);
            String currContent = mergeGetBlob(
                    currMap.get(fileName)).getContents();
            String givenContent = mergeGetBlob(
                    givenMap.get(fileName)).getContents();
            String conflictString = "<<<<<<< HEAD\n"
                    + currContent + "=======\n"
                    + givenContent + ">>>>>>>\n";
            inCWD.delete();
            inCWD.createNewFile();
            Utils.writeContents(inCWD, conflictString);
            Blob conflictedContents = new Blob(inCWD);
            File toAdd = new File(stageAdd + fileName);
            toAdd.createNewFile();
            Utils.writeObject(toAdd, conflictedContents);
            mergeConflict = true;
        }
        return mergeConflict;
    }

    private static void notConflictCases(String fileName,
                                         TreeMap<String, String> splitMap,
                                         TreeMap<String, String> currMap,
                                         TreeMap<String, String> givenMap,
                                         Branch checkedOutBranch,
                                         String stageAdd,
                                         String stageRemove)
            throws IOException {
        Branch currBranch = getCurrentBranch();
        if (splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && givenMap.containsKey(fileName)
                && splitMap.get(fileName).equals(currMap.get(fileName))
                && !splitMap.get(fileName).equals(givenMap.get(fileName))) {
            checkout2(checkedOutBranch.getCommitPointer(), fileName);
            File toAdd = new File(stageAdd + fileName);
            toAdd.createNewFile();
            Utils.writeObject(toAdd, mergeGetBlob(givenMap.get(fileName)));
        }

        if (splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && givenMap.containsKey(fileName)
                && splitMap.get(fileName).equals(givenMap.get(fileName))
                && !splitMap.get(fileName).equals(currMap.get(fileName))) {
            checkout2(currBranch.getCommitPointer(), fileName);
        }

        if (splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && !givenMap.containsKey(fileName)
                && splitMap.get(fileName).equals(currMap.get(fileName))) {
            File inCWD = new File(fileName);
            inCWD.delete();
            File toRemove = new File(stageRemove + fileName);
            toRemove.createNewFile();
            Utils.writeObject(toRemove, mergeGetBlob(currMap.get(fileName)));
        }

        if (!splitMap.containsKey(fileName)
                && !currMap.containsKey(fileName)
                && givenMap.containsKey(fileName)) {
            checkout2(checkedOutBranch.getCommitPointer(), fileName);
            File toAdd = new File(stageAdd + fileName);
            toAdd.createNewFile();
            Utils.writeObject(toAdd, mergeGetBlob(givenMap.get(fileName)));
        }

        if (!splitMap.containsKey(fileName) && currMap.containsKey(fileName)
                && !givenMap.containsKey(fileName)) {
            checkout2(currBranch.getCommitPointer(), fileName);
        }
    }

    private static Blob mergeGetBlob(String sha1) {
        Blob currBlob = Utils.readObject(
                new File(".gitlet/blobs/"
                        + sha1
                        + ".txt"), Blob.class);
        return currBlob;
    }

    private static TreeMap getGivenMap(String branchName) {
        File checkedOutBranchFile =
                new File(".gitlet/branches/" + branchName + ".txt");
        Branch checkedOutBranch =
                Utils.readObject(checkedOutBranchFile, Branch.class);

        Commit givenCommit = Utils.readObject
                (new File(".gitlet/commits/"
                        + checkedOutBranch.getCommitPointer()
                        + ".txt"), Commit.class);


        return givenCommit.getBlobs();
    }

    private static TreeMap getCurrMap() {
        Commit currentCommit = getCurrentCommit();
        return currentCommit.getBlobs();
    }

    private static TreeMap getSplitMap(String branchName) {
        String splitPoint = findSplitPoint(branchName);
        Commit splitPointCommit = Utils.readObject(
                new File(
                        ".gitlet/commits/"
                                + splitPoint + ".txt"), Commit.class);
        return splitPointCommit.getBlobs();

    }


    private static void moreMergeErrors(String branchName) {
        File checkedOutBranchFile =
                new File(".gitlet/branches/" + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        Branch checkedOutBranch =
                Utils.readObject(checkedOutBranchFile, Branch.class);


        String splitPoint = findSplitPoint(branchName);
        Commit splitPointCommit = Utils.readObject(
                new File(
                        ".gitlet/commits/"
                                + splitPoint + ".txt"), Commit.class);
        Commit currentCommit = getCurrentCommit();
        Commit givenCommit = Utils.readObject
                (new File(".gitlet/commits/"
                        + checkedOutBranch.getCommitPointer()
                        + ".txt"), Commit.class);
        Set<String> allFiles = new HashSet<>();
        @SuppressWarnings("unchecked") TreeMap<String, String> splitMap
                = splitPointCommit.getBlobs();
        @SuppressWarnings("unchecked") TreeMap<String, String> currMap
                = currentCommit.getBlobs();
        @SuppressWarnings("unchecked") TreeMap<String, String> givenMap
                = givenCommit.getBlobs();
        allFiles.addAll(splitMap.keySet());
        allFiles.addAll(currMap.keySet());
        allFiles.addAll(givenMap.keySet());
        List<String> filesInCWD = Utils.plainFilenamesIn(Main.CWD);

        for (String fileName : filesInCWD) {
            File fileInCWD = new File(fileName);
            Blob fileBlob = new Blob(fileInCWD);
            File temp = new File(".gitlet/blobTemp.txt");
            Utils.writeObject(temp, fileBlob);
            String sha1 = Utils.sha1(
                    Utils.serialize(Utils.readObject(temp, Blob.class)));
            temp.delete();
            if (!currMap.containsKey(fileName) && givenMap.containsKey(fileName)
                    && !givenMap.get(fileName).equals(sha1)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }

        }
    }


    public static void mergeErrors(String branchName) throws IOException {
        File checkedOutBranchFile =
                new File(".gitlet/branches/"
                        + branchName + ".txt");
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();
        if (headBranch.equals(branchName + ".txt")) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (!checkedOutBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch checkedOutBranch =
                Utils.readObject(checkedOutBranchFile, Branch.class);

        String splitPoint = findSplitPoint(branchName);

        if (checkedOutBranch.getCommitPointer().equals(splitPoint)) {
            System.out.println(
                    "Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (getCurrentBranch().getCommitPointer().equals(splitPoint)) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        File stageAdd = new File(".gitlet/stagingArea/stageAdd");
        File stageRemove = new File(".gitlet/stagingArea/stageRemove");
        List<String> stageAddFiles = Utils.plainFilenamesIn(stageAdd);
        List<String> stageRemoveFiles = Utils.plainFilenamesIn(stageRemove);

        if (!stageAddFiles.isEmpty() || !stageAddFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

    }
    public static String findSplitPoint(String branchName) {
        Set<String> commitsInBranch = findAllCommitsGivenBranch(branchName);
        return findLastCommAncestor(commitsInBranch);
    }

    private static String findLastCommAncestor(Set<String> commitsInBranch) {
        LinkedList<String> queue = new LinkedList<>();
        String splitpoint = null;
        queue.add(getCurrentBranch().getCommitPointer());

        while (queue.size() > 0) {
            String sha1 = queue.poll();
            if (commitsInBranch.contains(sha1)) {
                splitpoint = sha1;
                break;
            }
            Commit commVisiting = Utils.readObject(
                    new File(".gitlet/commits/"
                            + sha1 + ".txt"), Commit.class);
            if (commVisiting.getParent1() != null) {
                queue.add(commVisiting.getParent1());
            }
            if (commVisiting.getParent2() != null) {
                queue.add(commVisiting.getParent2());
            }
        }

        return splitpoint;
    }

    public static Set<String> findAllCommitsGivenBranch(String branchName) {
        File branchFile = new File(".gitlet/branches/" + branchName + ".txt");
        Branch branch = Utils.readObject(branchFile, Branch.class);
        Set<String> commitsInThisBranch = new TreeSet<>();
        LinkedList<String> queue = new LinkedList<>();
        queue.add(branch.getCommitPointer());

        while (queue.size() > 0) {
            String sha1 = queue.poll();
            commitsInThisBranch.add(sha1);
            Commit commVisiting = Utils.readObject(
                    new File(".gitlet/commits/"
                            + sha1 + ".txt"), Commit.class);
            if (commVisiting.getParent1() != null) {
                queue.add(commVisiting.getParent1());
            }
            if (commVisiting.getParent2() != null) {
                queue.add(commVisiting.getParent2());
            }
        }
        return commitsInThisBranch;
    }





    public static void generalCheckoutAlg(
            Commit toGetFrom, String fileName) throws IOException {
        File inCWD = new File(fileName);
        if (!toGetFrom.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String sha1 = (String) toGetFrom.getBlobs().get(fileName);
        File fileToCheckout = new File(".gitlet/blobs/" + sha1 + ".txt");
        Blob contents = Utils.readObject(fileToCheckout, Blob.class);
        inCWD.delete();
        inCWD.createNewFile();
        Utils.writeContents(inCWD, contents.getContents());
    }

    public static Commit getCurrentCommit() {
        Branch currentBranch = getCurrentBranch();
        File currCommFile = new File(
                ".gitlet/commits/"
                        + currentBranch.getCommitPointer() + ".txt");
        return Utils.readObject(currCommFile, Commit.class);
    }

    public static Branch getCurrentBranch() {
        Head head = Utils.readObject(new File(".gitlet/head.txt"), Head.class);
        String headBranch = head.getHead();
        File branchFile = new File(".gitlet/branches/" + headBranch);
        return Utils.readObject(branchFile, Branch.class);
    }


    public static void addRemote(String name, String directory)
            throws IOException {
        File newRemote = new File(".gitlet/remotes/" + name + ".txt");
        if (newRemote.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        newRemote.createNewFile();
        Utils.writeContents(newRemote, directory);

    }

    public static void rmRemote(String name) throws IOException {
        File remote = new File(".gitlet/remotes/" + name + ".txt");
        if (!remote.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        remote.delete();

    }

    public static void push(String name, String branchName) throws IOException {
        File remoteFile = new File(".gitlet/remotes/" + name + ".txt");
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        String directory = Utils.readContentsAsString(remoteFile);
        File remoteGitlet = new File(directory);
        if (!remoteGitlet.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File remoteBranchFile = new
                File(directory + "/branches/" + branchName + ".txt");
        Branch remoteBranch = Utils.readObject(remoteBranchFile, Branch.class);
        String remoteSha = remoteBranch.getCommitPointer();
        Commit commit = getCurrentCommit();
        boolean foundIt = false;

        while (commit.getParent1() != null) {
            String parentSha1 = commit.getParent1();
            if (parentSha1.equals(remoteSha)) {
                foundIt = true;
                break;
            }
            File parent1File = new
                    File(".gitlet/commits/" + commit.getParent1() + ".txt");
            commit = Utils.readObject(parent1File, Commit.class);
        }

        if (!foundIt) {
            System.out.println(
                    "Please pull down remote changes before pushing.");
            System.exit(0);
        }
        String sha1Iterate = Utils.sha1(Utils.serialize(getCurrentCommit()));
        Commit commIterate = getCurrentCommit();

        while (!sha1Iterate.equals(remoteSha)) {
            File remCommit = new File(
                    directory + "/commits/" + sha1Iterate + ".txt");
            remCommit.createNewFile();
            Utils.writeObject(remCommit, commIterate);
            File parent = new File(
                    ".gitlet/commits/" + commIterate.getParent1() + ".txt");
            commIterate = Utils.readObject(parent, Commit.class);
            sha1Iterate = Utils.sha1(Utils.serialize(commIterate));
        }
        remoteBranch.setCommitPointerWithoutWrite(
                Utils.sha1(Utils.serialize(getCurrentCommit())));
        Utils.writeObject(remoteBranchFile, remoteBranch);

    }

    public static void fetch(String name, String branchName)
            throws IOException {
        File remoteFile = new File(".gitlet/remotes/" + name + ".txt");
        if (!remoteFile.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        String directory = Utils.readContentsAsString(remoteFile);
        File remoteDir = new File(directory);
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File branchFile = new File(
                directory + "/branches/" + branchName + ".txt");
        if (!branchFile.exists()) {
            System.out.println("The remote does not have that branch.");
            System.exit(0);
        }
        File remCommitsDir = new File(directory + "/commits");
        List<String> remoteCommits = Utils.plainFilenamesIn(remCommitsDir);
        File localBranchFile = new File(
                ".gitlet/branches/" + name + branchName + ".txt");
        Branch newBranch;
        if (!localBranchFile.exists()) {
            newBranch = new Branch(
                    name + branchName + ".txt",
                    Utils.sha1(Utils.serialize(getCurrentCommit())));
        } else {
            newBranch = Utils.readObject(localBranchFile, Branch.class);
        }
        for (String fileName : remoteCommits) {
            File inRemote = new File(directory + "/commits/" + fileName);
            File inLocalComm = new File(".gitlet/commits/" + fileName);
            Commit inQuestion = Utils.readObject(inRemote, Commit.class);
            @SuppressWarnings("unchecked") TreeMap<String, String> blobs
                    = inQuestion.getBlobs();
            if (!inLocalComm.exists()) {
                inLocalComm.createNewFile();
                Utils.writeObject(inLocalComm, inQuestion);
                for (String blobFile : blobs.keySet()) {
                    String sha1 = blobs.get(blobFile);
                    File contentsInRemote = new
                            File(directory + "/blobs/" + sha1 + ".txt");
                    Blob contents = Utils.readObject(
                            contentsInRemote, Blob.class);
                    File localBlobFile = new File(
                            ".gitlet/blobs/" + sha1 + ".txt");
                    localBlobFile.createNewFile();
                    Utils.writeObject(localBlobFile, contents);
                }
                newBranch.setCommitPointer(
                        fileName.substring(0, fileName.length() - 4));
            }
        }



    }

    public static void pull(String name, String branchName) throws IOException {
        fetch(name, branchName);
        merge(name + "/" + branchName);
    }
}
