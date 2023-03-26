package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Brent Friedman
 */
public class Main {

    /** Contains the CWD. */
    public static final String CWD = System.getProperty("user.dir");


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            validateNumArgs(args.length, 1, 1);
            doesGitletExistInit();
            Commands.init();
            break;
        case "add":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.add(args[1]);
            break;
        case "commit":
            doesGitletExist();
            if (args.length == 1 || args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            validateNumArgs(args.length, 2, 2);
            Commands.commit(args[1], null);
            break;
        case "log":
            doesGitletExist();
            validateNumArgs(args.length, 1, 1);
            Commands.log();
            break;
        case "checkout":
            doesGitletExist();
            validateNumArgs(args.length, 2, 4);
            runCheckout(args);
            break;
        case "rm":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.rm(args[1]);
            break;
        case "global-log":
            doesGitletExist();
            validateNumArgs(args.length, 1, 1);
            Commands.globalLog();
            break;
        case "find":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.find(args[1]);
            break;
        default:
            secondHalfSwitch(args);
        }

    }

    public static void secondHalfSwitch(String[] args) throws IOException {
        switch (args[0]) {
        case "branch":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.branch(args[1]);
            break;
        case "rm-branch":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.rmBranch(args[1]);
            break;
        case "status":
            doesGitletExist();
            validateNumArgs(args.length, 1, 1);
            Commands.status();
            break;
        case "reset":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.reset(args[1]);
            break;
        case "merge":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.merge(args[1]);
            break;
        case "add-remote":
            doesGitletExist();
            validateNumArgs(args.length, 3, 3);
            Commands.addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            doesGitletExist();
            validateNumArgs(args.length, 2, 2);
            Commands.rmRemote(args[1]);
            break;
        case "push":
            doesGitletExist();
            validateNumArgs(args.length, 3, 3);
            Commands.push(args[1], args[2]);
            break;
        case "fetch":
            doesGitletExist();
            validateNumArgs(args.length, 3, 3);
            Commands.fetch(args[1], args[2]);
            break;
        case "pull":
            doesGitletExist();
            validateNumArgs(args.length, 3, 3);
            Commands.pull(args[1], args[2]);
            break;

        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }

    }


    public static void doesGitletExistInit() {
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            System.out.println(
                    "A Gitlet version-control system"
                            + " already exists in the current directory.");
            System.exit(0);
        }
    }
    public static void validateNumArgs(
            int numArgs, int minExpected, int maxExpected) {
        if (!(numArgs >= minExpected) | !(numArgs <= maxExpected)) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void doesGitletExist() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void runCheckout(String[] args) throws IOException {
        if (args.length == 3 && args[1].equals("--")) {
            Commands.checkout1(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            Commands.checkout2(args[1], args[3]);
        } else if (args.length == 2) {
            Commands.checkout3(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

}
