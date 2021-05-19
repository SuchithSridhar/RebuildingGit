package git;
/**
 * Driver class for the program
 * @author Suchith Sridhar Khajjayam
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (!args[0].equals("init")) {
            if (!(new java.io.File(GitManager.GIT_FOLDER)).exists()) {
                System.out.println("Not in an initialized git directory.");
                return;
            }
        }

        GitManager gm = new GitManager();

        String firstArg = args[0];
        switch (firstArg) {
        case "init":
            gm.init();
            break;
        case "add":
            gm.add(args[1]);
            break;
        case "commit":
            if (args.length < 2) {
                System.out.println("Please enter a commit message.");
                return;
            }
            if (args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                return;
            }
            gm.commit(args[1]);
            break;
        case "status":
            gm.status();
            break;
        case "find":
            String out = gm.find(args[1]);
            if (out.equals("")) {
                System.out.println("Found no commit with that message.");
            } else {
                System.out.println(out);
            }
            break;
        case "rm":
            gm.rm(args[1]);
            break;
        case "log":
            gm.log();
            break;
        case "global-log":
            gm.global_log();
            break;
        case "merge":
            gm.merge(args[1]);
            break;
        case "reset":
            gm.reset(args[1]);
            break;
        case "rm-branch":
            gm.rm_branch(args[1]);
            break;
        case "branch":
            gm.branch(args[1]);
            break;
        case "checkout":
            if (args.length == 2)
                gm.checkoutBranch(args[1]);
            else if (args[1].equals("--"))
                gm.checkoutFileFromHead(args[2]);
            else if (args.length == 3 && !args[1].equals("--"))
                System.out.println("Incorrect operands.");
            else if (args.length == 4 && !args[2].equals("--"))
                System.out.println("Incorrect operands.");
            else if (args.length == 4 && args[2].equals("--"))
                gm.checkoutFileFromCommitID(args[3], args[1]);
            else
                System.out.println("Incorrect operands.");
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }

    }
}
