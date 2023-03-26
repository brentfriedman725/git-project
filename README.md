# Gitlet Design Document
author:

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main
#### Description
* This class will run the main method, which checks the input and performs actions accordingly
  * The actions performed will with either:
    * Throw errors before doing anything else (if repo already exists, no input, unknown command, wrong # of operands, check that this working directory has been initialized)
    * Initialize a new repository corresponding to CURRENT_WORKING_DIRECTORY or if already exists, do nothing
    * main method will then deal with commands by calling a method in the repository associated with CURRENT_WORKING_DIRECTORY that deals with the given args
#### Fields
* static final String[] LIST_OF_COMMANDS: list of all the valid commands for Gitlet
* static ArrayList<Repository> allRepositories: list of all the current repositories with their path as the name

#### Methods
1. validateNumArgs(String command, int numArgs, int numMinExpectedArgs, int numMaxExpectedArgs)
   1. This method makes sure that the number of arguments is correct for the provided command
   

### Commands

#### Description
* This class contains all the methods that correspond to certain commands

####Fields
* public Repository repo

#### Methods
* public Commands(Repository repo): initializes repo
* add(String fileName): Performs the add function, which instantiates a new blob with fileName and contents of the file and adds to stageAdd in repo. If already committed before and not changed, dont do anything
* commit(String message): Instantiates a new Commit object and add it to commitChain.
* rm(String fileName): remove fileName from stageAdd if there; if in the current Commit, remove from current commit and add to stageRemove; also remove from CWD
* log(): print out information about the commit chain in backwards order
* checkout(String commitID = null, String branchName = null, String fileName = null):
  * If given only fileName: access Commit that headBranch points to, get the commitID, go into .gitlet/commitID folder and get the fileName and push it to CWD, overwriting if necessary
  * If given commitID and filename: get the file CommitID from .gitlet, get the file of filename, and push out

### Commit

#### Description
* This class represents a commit 

#### Fields
* private final Repository repo: This is the repository the commit is in

* private String? date: This is the timestamp of the commit
* private TreeMap<Blob> blobsAdded: This contains all of the Blobs that the commit tracks
* private String message: contains the message
* private Commit parent: The parent of this commit

#### Methods
* public Commit(Repository repo, String message): This instantiates the instance variables, clears blobs from stageAdd in repo and creates copies of these blobs in a new file of commitID name within .gitlet file with filenames. Also, remove files from stageRemove


### Blob

#### Description
* This represents a blob, which contains the contents of a file and the filename

#### Fields
* public String contents: the contents of the file

#### Methods
*public Blob(String contents): initializes the instance variable

#### IMPORTANT
* When creating a Blob, must create a file with name as the SHA-1 code of that blob. Blobs do not store their own SHA-1 code.


### Branch

#### Description
*This class describes a branch and where it points

#### Fields
* public String name: Name, either head or master
* public String currentCommit: the commit that the branch is currently on
* private File branchFile: the file of this Branch

#### Methods
* public Branch(String name, Commit current): initializes a new branch
* public moveBranch(Commit to): changes current to be to




## 2. Algorithms


The Main class works by solely reading input, validating the number of arguments of a given command by calling
a method called validateNumArgs(), throwing exit code messages, and calling functions in the Command class. My
Command class works by performing the duties of each command. For example, init() creates the .gitlet directory,
initializes the head, the master branch (and creates files for them), and creates the initial commit. The add()
function deals with the folders and files existing in the .gitlet directory and performs actions accordingly. Each
method simply works with the files that contain other objects, reads them as objects, and performs the necessary tasks.



## 3. Persistence



Each object will be either saving itself (Branches and Head) or will be saved when running commands (commits,blobs).
Most of my program will be stored in files or folders within my .gitlet folder. This sames time and makes things vey clear
when thinking about my program as a whole. When a new commit is made, I save it in the commits folder. Branches
help me find commits by saving the Sha-1 code of the commit that they point to. The Head object is saved in a file and
tells me which branch is the head branch. The staging area is stored as a folder in the .gitlet directory and has two folders:
one for the adding stage area and one for the removing stage area. By saving everything in folders and files, I am very organized and have a good grasp
on how my program interacts with each other.
## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

![](C:\Users\Brent Friedman\Desktop\gitlet diagram.jpg)
