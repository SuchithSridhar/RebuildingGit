package git;
import java.io.File;
import java.util.*;

/**
 * GitManager
 */
public class GitManager {

    protected static final String COMMITS_FOLDER = ".git/commits/";
    protected static final String BRANCHES_FOLDER = ".git/branches/";
    protected static final String BLOBS_FOLDER = ".git/blobs/";
    protected static final String GIT_FOLDER = ".git/";
    protected static final String STAGE_FOLDER = ".git/staging_area/";
    protected static final String STAGE_FILE = ".git/STAGE";
    protected static final String HEAD_FILE = ".git/HEAD";

    public FileManager fm;

    public GitManager() {
        fm = new FileManager();
    }

    public void init() {
        File baseFolder = new File(GIT_FOLDER);
        File brf = new File(BRANCHES_FOLDER);
        File blf = new File(BLOBS_FOLDER);
        File stf = new File(STAGE_FOLDER);
        File cmf = new File(COMMITS_FOLDER);

        if (baseFolder.exists()) {
            // TODO: Manage exception
            System.out.println("A Git version-control system already exists in the current directory.");
            return;
        }

        // Creating directories requires

        baseFolder.mkdir();
        brf.mkdir();
        blf.mkdir();
        stf.mkdir();
        cmf.mkdir();

        Commit initialCommit = new Commit("initial commit", new SuperDate(1l));
        String sha1Commit = GitUtils.sha1(initialCommit);

        Head HEAD = new Head(sha1Commit);
        Branch master = new Branch(sha1Commit);
        Stage stage = new Stage();

        HEAD.currentBranch = "master";
        initialCommit.branch = HEAD.currentBranch;
        initialCommit.depth = 0;

        fm.writeFile(COMMITS_FOLDER + sha1Commit, initialCommit);
        fm.writeFile(BRANCHES_FOLDER + "master", master);
        fm.writeFile(HEAD_FILE, HEAD);
        fm.writeFile(STAGE_FILE, stage);

    }

    public static List<String> getAllCommits() {
        List<String> list = new ArrayList<String>();
        for (File f : (new File(COMMITS_FOLDER)).listFiles()) {
            list.add(f.getName());
        }
        return list;
    }

    // Internal use
    public void add(String filename, String blobFile) {
        Stage stage = (Stage) fm.readFile(STAGE_FILE);

        if (stage.filenames.contains(filename)) {
            int index = stage.filenames.indexOf(filename);
            stage.blobs.remove(index);
            stage.blobs.add(index, blobFile);

        } else {
            stage.filenames.add(filename);
            stage.blobs.add(blobFile);
        }

        fm.writeFile(STAGE_FILE, stage);
    }

    public void add(String filename) {
        Stage stage = (Stage) fm.readFile(STAGE_FILE);

        // if previously staged to be removed
        if (stage.removed.contains(filename)) {
            int index = stage.removed.indexOf(filename);
            Blob blob = (Blob) fm.readFile(BLOBS_FOLDER + stage.removedBlobs.get(index));
            fm.writeTextFile(filename, blob.data);

            stage.removed.remove(index);
            stage.removedBlobs.remove(index);
            fm.writeFile(STAGE_FILE, stage);
            return;
        }

        if (!(new File(filename)).exists()) {
            System.out.println("File does not exist.");
            return;
        }

        Blob blobFile = new Blob(filename);
        String sha1 = GitUtils.sha1(blobFile);

        // Check if in last commit or in stage
        if (checkInLastCommit(sha1)) {

            // check if staged previously
            if (stage.filenames.contains(filename)) {
                int index = stage.filenames.indexOf(filename);
                stage.filenames.remove(index);
                stage.blobs.remove(index);
            }
            // if not skip
            return;
        }

        // if just in stage and not in commit
        if (checkInStage(stage, sha1)) {
            return;
        }

        // Check if only the contents have changed but file previously
        // staged.
        if (stage.filenames.contains(filename)) {
            int index = stage.filenames.indexOf(filename);
            stage.blobs.remove(index);
            stage.blobs.add(index, sha1);
        } else {
            stage.filenames.add(filename);
            stage.blobs.add(sha1);
        }

        fm.writeFile(STAGE_FILE, stage);
        fm.writeFile(STAGE_FOLDER + sha1, blobFile);

    }

    public void commit(String message) {
        // TODO: Remap the tip of the branch
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        Head HEAD = (Head) fm.readFile(HEAD_FILE);
        Branch branch = (Branch) fm.readFile(BRANCHES_FOLDER + HEAD.currentBranch);
        Commit currentCommit = (Commit) fm.readFile(COMMITS_FOLDER + HEAD.filename);

        if (stage.filenames.isEmpty() && stage.removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message);
        newCommit.filenames = currentCommit.filenames;
        newCommit.blobfilenames = currentCommit.blobfilenames;
        newCommit.parent = HEAD.filename;
        newCommit.branch = HEAD.currentBranch;
        newCommit.depth = currentCommit.depth + 1;

        int index = 0;
        for (String file : stage.filenames) {

            String corBlob = stage.blobs.get(index);

            if (newCommit.filenames.contains(file)) {

                int i = newCommit.filenames.indexOf(file);
                newCommit.blobfilenames.remove(i);
                newCommit.blobfilenames.add(i, corBlob);

            } else {
                newCommit.filenames.add(file);
                newCommit.blobfilenames.add(corBlob);
            }

            // Move from staging area to blobs folder
            File f = new File(STAGE_FOLDER + corBlob);
            f.renameTo(new File(BLOBS_FOLDER + corBlob));

            index++;
        }

        // TODO: To make more effecient, add check to previous for
        // loop to check if file in removed and remove file.
        for (String file : stage.removed) {
            index = newCommit.filenames.indexOf(file);
            newCommit.filenames.remove(index);
            newCommit.blobfilenames.remove(index);
        }

        String sha1 = GitUtils.sha1(newCommit);
        fm.writeFile(COMMITS_FOLDER + sha1, newCommit);

        HEAD.filename = sha1;
        branch.filename = sha1;
        fm.writeFile(HEAD_FILE, HEAD);
        fm.writeFile(BRANCHES_FOLDER + HEAD.currentBranch, branch);

        GitUtils.purgeDirectory(new File(STAGE_FOLDER));
        Stage newStage = new Stage();
        fm.writeFile(STAGE_FILE, newStage);

    }

    private boolean checkInLastCommit(String sha1) {
        Head HEAD = (Head) fm.readFile(HEAD_FILE);
        Commit currentCommit = (Commit) fm.readFile(COMMITS_FOLDER + HEAD.filename);

        if (currentCommit.blobfilenames.contains(sha1))
            return true;
        else
            return false;
    }

    private boolean checkInStage(Stage stage, String sha1) {
        if (stage.blobs.contains(sha1))
            return true;
        else
            return false;
    }

    public void status() {
        Head HEAD = (Head) fm.readFile(HEAD_FILE);
        File branchesFolder = new File(BRANCHES_FOLDER);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);

        List<File> branches = Arrays.asList(branchesFolder.listFiles());
        ArrayList<String> stagedFlies = stage.filenames;
        ArrayList<String> removedFiles = stage.removed;

        String str;
        Collections.sort(branches);

        System.out.println("=== Branches ===");

        for (File f : branches) {
            str = f.getName();
            if (str.equals(HEAD.currentBranch)) {
                str = "*" + str;

            }
            System.out.println(str);
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        for (String s : stagedFlies) {
            System.out.println(s);
        }

        System.out.println("");

        System.out.println("=== Removed Files ===");
        for (String s : removedFiles) {
            System.out.println(s);
        }

        System.out.println("");


        String suffix = "";
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String file : getModifiedFiles()) {
            if (!(GitUtils.plainFilenamesIn(".")).contains(file))
                suffix = " (deleted)";
            else
                suffix = " (modified)";

            System.out.println(file + suffix);
        }

        System.out.println("");

        System.out.println("=== Untracked Files ===");
        for (String file : getUntractedFiles()) {
            System.out.println(file);
        }
    }

    private List<String> getUntractedFiles() {
        Head head = (Head) fm.readFile(HEAD_FILE);
        Commit commit = (Commit) fm.readFile(COMMITS_FOLDER + head.filename);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        List<String> filesInCurDir = GitUtils.plainFilenamesIn(".");
        List<String> untracted = new ArrayList<String>();
        for (String f : filesInCurDir) {
            if (!commit.filenames.contains(f) && !stage.filenames.contains(f))
                untracted.add(f);
        }
        return untracted;
    }

    private List<String> getModifiedFiles() {
        Head head = (Head) fm.readFile(HEAD_FILE);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        Commit commit = (Commit) fm.readFile(COMMITS_FOLDER + head.filename);
        List<String> filesInCurDir = GitUtils.plainFilenamesIn(".");
        List<String> modified = new ArrayList<String>();
        for (String f : filesInCurDir) {
            if (commit.filenames.contains(f) || stage.filenames.contains(f)) {
                Blob blob;
                if (stage.filenames.contains(f)) {
                    int index = stage.filenames.indexOf(f);
                    blob = (Blob) fm.readFile(STAGE_FOLDER + stage.blobs.get(index));
                } else {
                    int index = commit.filenames.indexOf(f);
                    blob = (Blob) fm.readFile(BLOBS_FOLDER + commit.blobfilenames.get(index));
                }
                if (!blob.data.equals(GitUtils.readContentsAsString(new File(f)))) {
                    modified.add(f);
                }
            }

        }

        for (String f : commit.filenames) {
            if ( !stage.removed.contains(f) && !filesInCurDir.contains(f)) {
                modified.add(f);
            }
        }
        return modified;
    }

    public String find(String commitMessage) {

        File commitsDir = new File(COMMITS_FOLDER);
        File[] commits = commitsDir.listFiles();

        String str;
        String out = "";
        Commit current;
        for (File file : commits) {
            str = file.getName();
            current = (Commit) fm.readFile(COMMITS_FOLDER + str);
            if (current.message.equals(commitMessage)) {
                out += str + "\n";
            }

        }
        return out;

    }

    public void rm(String filename) {
        Head head = (Head) fm.readFile(HEAD_FILE);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        Commit curCommit = (Commit) fm.readFile(COMMITS_FOLDER + head.filename);
        boolean flag = false;

        if (!(GitUtils.plainFilenamesIn(".")).contains(filename)) {
            stage.removed.add(filename);
            flag = true;
        } else {
            if (stage.filenames.contains(filename)) {
                stage.filenames.remove(filename);
                flag = true;
            }

            if (curCommit.filenames.contains(filename)) {
                stage.removed.add(filename);
                Blob blob = new Blob(filename);
                String sha1 = GitUtils.sha1(blob);
                stage.removedBlobs.add(sha1);
                fm.writeFile(STAGE_FOLDER + sha1, blob);
                GitUtils.restrictedDelete(filename);
                flag = true;
            }
        }


        if (flag)
            fm.writeFile(STAGE_FILE, stage);
        else
            System.out.println("No reason to remove the file.");
    }

    public void global_log() {
        File commitsDir = new File(COMMITS_FOLDER);
        File[] commits = commitsDir.listFiles();

        String str;
        Commit current;
        for (File file : commits) {
            str = file.getName();
            current = (Commit) fm.readFile(COMMITS_FOLDER + str);
            System.out.println("===\ncommit " + str + "\nDate: " + current.timestamp + "\n" + current.message + "\n");
        }

    }

    public void log() {
        Head head = (Head) fm.readFile(HEAD_FILE);

        String str;
        Commit current = (Commit) fm.readFile(COMMITS_FOLDER + head.filename);
        System.out.println("===");
        System.out.println("commit " + head.filename);
        if (current.isMerged)
            System.out.println("Merge: " + ((MergedCommit) current).parent.substring(0, 6) + " " + ((MergedCommit) current).mergedParent.substring(0, 6));
        System.out.println("Date: " + current.timestamp);
        System.out.println(current.message);
        System.out.println();
        while (current.parent != null) {
            str = current.parent;
            current = (Commit) fm.readFile(COMMITS_FOLDER + current.parent);
            System.out.println("===");
            System.out.println("commit " + str);
            if (current.isMerged)
                System.out.println("Merge: " + ((MergedCommit) current).parent.substring(0, 6) + " " + ((MergedCommit) current).mergedParent.substring(0, 6));
            System.out.println("Date: " + current.timestamp);
            System.out.println(current.message);
            System.out.println();
        }


    }

    public void branch(String branchName) {
        File[] branches = (new File(BRANCHES_FOLDER)).listFiles();
        for (File f : branches) {
            if (f.getName().equals(branchName)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }

        Head head = (Head) fm.readFile(HEAD_FILE);
        Branch branch = new Branch();
        branch.filename = head.filename;

        fm.writeFile(BRANCHES_FOLDER + branchName, branch);
    }

    public String convertCommitId(String commitID) {
        String file = commitID;
        if (commitID.length() < 12) {
            for (String f : getAllCommits()) {
                if (f.substring(0, commitID.length()).equals(commitID)) {
                    file = f;
                    break;
                }
            }
        }
        return file;
    }

    public void checkoutFileFromCommitID(String filename, String commitIDin) {

        String commitID = convertCommitId(commitIDin);

        if (!(new File(COMMITS_FOLDER + commitID)).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit cmt;
        cmt = (Commit) fm.readFile(COMMITS_FOLDER + commitID);
        Head head = (Head) fm.readFile(HEAD_FILE);
        int index = cmt.filenames.indexOf(filename);
        if (index == -1) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob = (Blob)fm.readFile(BLOBS_FOLDER + cmt.blobfilenames.get(index));
        fm.writeTextFile(filename, blob.data);

        for (String file : ((Commit)fm.readFile(COMMITS_FOLDER + head.filename)).filenames) {
            if (!cmt.filenames.contains(file)) {
                GitUtils.restrictedDelete(file);
            }
        }
    }

    public void checkoutFileFromHead(String filename) {
        this.checkoutFileFromCommitID(filename, ((Head) fm.readFile(HEAD_FILE)).filename);
    }

    // Checkout to a different branch
    public void checkoutBranch(String branchName) {
        Head head = (Head) fm.readFile(HEAD_FILE);
        String origCommit = head.filename;
        File[] branches = (new File(BRANCHES_FOLDER)).listFiles();
        boolean flag = false;
        String filename;

        if (head.currentBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        for (File file : branches) {
            filename = file.getName();
            if (filename.equals(branchName)) {
                head.currentBranch = branchName; // set branch
                head.filename = ((Branch) fm.readFile(BRANCHES_FOLDER + branchName)).filename; // set commit
                flag = true;
                break;
            }

        }

        if (!flag) {
            System.out.println("No such branch exists.");
            return;
        }

        fm.writeFile(HEAD_FILE, head);
        boolean check = this.updateCWDWithCommit(head.filename, origCommit);
        if (!check) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
    }

    private boolean updateCWDWithCommit(String commitId, String origCommit) {
        Commit orig = (Commit) fm.readFile(COMMITS_FOLDER + origCommit);
        Commit commit = (Commit) fm.readFile(COMMITS_FOLDER + commitId);

        for (String f : GitUtils.plainFilenamesIn(".")) {
            if (!orig.filenames.contains(f) && commit.filenames.contains(f)) {
                return false;
            }
        }

        for (String file : commit.filenames) {
            int index = commit.filenames.indexOf(file);
            String data = ((Blob) fm.readFile(BLOBS_FOLDER + commit.blobfilenames.get(index))).data;

            fm.writeTextFile(file, data);
        }

        for (String file : orig.filenames) {
            if (!commit.filenames.contains(file)) {
                GitUtils.restrictedDelete(file);
            }
        }
        return true;
    }

    public void reset(String commitIdin) {
        String commitId = convertCommitId(commitIdin);
        if (!(new File(COMMITS_FOLDER + commitId)).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        System.out.println(getUntractedFiles());
        if (!getUntractedFiles().isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        Head head = (Head) fm.readFile(HEAD_FILE);
        Commit resetPoint = (Commit) fm.readFile(COMMITS_FOLDER + commitId);
        Commit headPoint = (Commit) fm.readFile(COMMITS_FOLDER + head.filename);
        Branch branch = (Branch) fm.readFile(BRANCHES_FOLDER + headPoint.branch);

        for (String file : headPoint.filenames) {
            if (!resetPoint.filenames.contains(file)) {
                boolean check = GitUtils.restrictedDelete(file);
                //if (!check)
                //   System.out.println("Unable to delete file in reset command");
            }
        }

        int index = 0;
        Blob blob;
        for (String file : resetPoint.filenames) {
            blob = (Blob) fm.readFile(BLOBS_FOLDER + resetPoint.blobfilenames.get(index));
            fm.writeTextFile(file, blob.data);
            index++;
        }

        branch.filename = commitId;
        head.filename = commitId;
        fm.writeFile(BRANCHES_FOLDER + headPoint.branch, branch);
        fm.writeFile(HEAD_FILE, head);
        fm.writeFile(STAGE_FILE, new Stage());

    }

    private String findSplit(String aFile, Commit a, String bFile, Commit b) {

        while (a.depth != b.depth) {
            if (a.depth > b.depth) {
                aFile = a.parent;
                a = (Commit) fm.readFile(COMMITS_FOLDER + aFile);
            } else {
                bFile = b.parent;
                b = (Commit) fm.readFile(COMMITS_FOLDER + bFile);
            }
        }

        while (!aFile.equals(bFile)) {
            aFile = a.parent;
            bFile = b.parent;
            a = (Commit)fm.readFile(COMMITS_FOLDER + aFile);
            b = (Commit)fm.readFile(COMMITS_FOLDER + bFile);
        }

        return aFile;

    }

    private boolean branchExists(String branch) {
        for (File f : (new File(BRANCHES_FOLDER)).listFiles()) {
            if (f.getName().equals(branch))
                return true;
        }
        return false;
    }

    public void merge(String other) {
        Head head = (Head) fm.readFile(HEAD_FILE);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        if (head.currentBranch.equals(other)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!branchExists(other)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!stage.filenames.isEmpty()) {
            System.out.println("You have uncommitted changes. ");
            return;
        }
        /* Suchith
        if (!getUntractedFiles().isEmpty()){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            return;
        }
        */


        Branch branchHead = (Branch) fm.readFile(BRANCHES_FOLDER + head.currentBranch);
        Branch branchOther = (Branch) fm.readFile(BRANCHES_FOLDER + other);
        Commit commitHead = (Commit) fm.readFile(COMMITS_FOLDER + branchHead.filename);
        Commit commitOther = (Commit) fm.readFile(COMMITS_FOLDER + branchOther.filename);
        Commit split = ((Commit) fm.readFile(COMMITS_FOLDER + findSplit(branchHead.filename, commitHead, branchOther.filename, commitOther)));

        if (split.equals(commitOther)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        Set<String> set = new HashSet<String>();
        set.addAll(commitHead.filenames);
        set.addAll(commitOther.filenames);
        // Anything that's not there in the other or head
        // shouldn't be there in the final commit reguarless
        // of existance in the split.

        int indexHead = -1;
        int indexOther = -1;
        int indexSplit = -1;

        String blobOtherFile = null;
        String blobHeadFile = null;
        String blobSplitFile = null;

        Blob blobOther = null;
        Blob blobHead = null;
        Blob blobSplit = null;

        boolean inOther;
        boolean inSplit;
        boolean inHead;

        boolean modifiedInHead;
        boolean modifiedInOther;
        boolean sameModification;

        boolean[] conditions = new boolean[8];

        for (String file : set) {
            indexOther = commitOther.filenames.indexOf(file);
            indexHead = commitHead.filenames.indexOf(file);
            indexSplit = split.filenames.indexOf(file);

            inOther = commitOther.filenames.contains(file);
            inHead = commitHead.filenames.contains(file);
            inSplit = split.filenames.contains(file);

            if (inOther) {
                blobOtherFile = commitOther.blobfilenames.get(indexOther);
                blobOther = (Blob) fm.readFile(BLOBS_FOLDER + blobOtherFile);
            }
            if (inHead) {
                blobHeadFile = commitHead.blobfilenames.get(indexHead);
                blobHead = (Blob) fm.readFile(BLOBS_FOLDER + blobHeadFile);
            }
            if (inSplit) {
                blobSplitFile = split.blobfilenames.get(indexSplit);
                blobSplit = (Blob) fm.readFile(BLOBS_FOLDER + blobSplitFile);
            }

            // File in split and unmodified in head but modified in other

            System.out.println("file: " + file); //remove

            try {
                modifiedInHead = !blobSplitFile.equals(blobHeadFile);
            } catch (NullPointerException e) {
                modifiedInHead = false;
            }
            try {
                modifiedInOther = !blobSplitFile.equals(blobOtherFile);
            } catch (NullPointerException e) {
                modifiedInOther = false;
            }
            try {
                sameModification = blobHeadFile.equals(blobSplitFile);
            } catch (NullPointerException e) {
                sameModification = false;
            }

            // In split and not modified in Head and modified in other - contents from other
            conditions[0] = ( inSplit && inOther && inHead && !modifiedInHead && modifiedInOther);

            // In split and unmodified in head and deleted from other - delete file
            conditions[1] = ( inSplit && inHead && !inOther && !modifiedInHead );

            // Not in split or head but in other - contents from other
            conditions[2] = ( !inSplit && !inHead && inOther );

            // Either
            // a. In split, modified in other and head in different ways
            // b. Not in split, other and head have different content
            // - Diff them
            conditions[3] = ( inSplit && inOther && inHead && modifiedInHead && modifiedInOther && !sameModification ) || ( !inSplit && inOther && inHead && !sameModification );

            // Conflict -> in split not in head and modified in other
            // or vise vera
            conditions[4] = ( inSplit && !inHead && inOther && modifiedInOther ) || (  inSplit && inHead && !inOther && modifiedInHead  );

            if (conditions[0]) {
                fm.writeTextFile(file, blobOther.data);
                this.add(file);
            } else if (conditions[1]) {
                this.rm(file);
            } else if (conditions[2]) {
                fm.writeTextFile(file, blobOther.data);
                this.add(file);
            } else if (conditions[3]) {
                System.out.println("Encountered a merge conflict.");
                fm.writeTextFile(file, "<<<<<<< HEAD\n" + blobHead.data + "=======\n" + blobOther.data + ">>>>>>>\n");
                this.add(file);
            } else if (conditions[4]) {
                System.out.println("Encountered a merge conflict.");
                String data = "";
                if (!inOther)
                    data = "<<<<<<< HEAD\n" + blobHead.data + "=======\n" + ">>>>>>>\n";
                if (!inHead)
                    data = "<<<<<<< HEAD\n" + "=======\n" + blobOther.data + ">>>>>>>\n";
                fm.writeTextFile(file, data);
                this.add(file);
            }

        }
        this.mergeCommit(branchHead.filename, branchOther.filename, commitHead, commitOther);
        if (split.equals(commitHead)) {
            System.out.println("Current branch fast-forwarded");
        }


    }

    public void mergeCommit(String A, String B, Commit ac, Commit bc) {

        Head head = (Head) fm.readFile(HEAD_FILE);
        Stage stage = (Stage) fm.readFile(STAGE_FILE);
        Branch branch = (Branch) fm.readFile(BRANCHES_FOLDER + head.currentBranch);

        MergedCommit newCommit = new MergedCommit(A, B, ac, bc);
        newCommit.filenames = ac.filenames;
        newCommit.blobfilenames = ac.blobfilenames;
        newCommit.parent = A;
        newCommit.mergedParent = B;
        newCommit.branch = ac.branch;
        newCommit.depth = ac.depth + 1;

        int index = 0;
        for (String file : stage.filenames) {
            String corBlob = stage.blobs.get(index);
            if (newCommit.filenames.contains(file)) {

                int i = newCommit.filenames.indexOf(file);
                newCommit.blobfilenames.remove(i);
                newCommit.blobfilenames.add(i, corBlob);

            } else {
                newCommit.filenames.add(file);
                newCommit.blobfilenames.add(corBlob);
            }

            // Move from staging area to blobs folder
            File f = new File(STAGE_FOLDER + corBlob);
            f.renameTo(new File(BLOBS_FOLDER + corBlob));

            index++;
        }

        // TODO: To make more effecient, add check to previous for
        // loop to check if file in removed and remove file.
        for (String file : stage.removed) {
            index = newCommit.filenames.indexOf(file);
            newCommit.filenames.remove(index);
            newCommit.blobfilenames.remove(index);
        }

        String sha1 = GitUtils.sha1(newCommit);
        fm.writeFile(COMMITS_FOLDER + sha1, newCommit);

        head.filename = sha1;
        branch.filename = sha1;
        fm.writeFile(HEAD_FILE, head);
        fm.writeFile(BRANCHES_FOLDER + head.currentBranch, branch);

        GitUtils.purgeDirectory(new File(STAGE_FOLDER));
        Stage newStage = new Stage();
        fm.writeFile(STAGE_FILE, newStage);

    }

    public void rm_branch(String brName) {
        Head head = (Head) fm.readFile(HEAD_FILE);

        if (brName.equals(head.currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;

        }

        File f = new File(BRANCHES_FOLDER + brName);

        boolean check = f.delete();
        if (!check) {
            System.out.println("A branch with that name does not exist.");
        }
    }

}
