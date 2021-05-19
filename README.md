# Rebuilding Git

Trying to rebuild a version control system called git using Java.
Trying to achieve most features including:

- Tracking files.
- Being able to revert entire commit or individual files.
- Create new branches.
- Merge different branches.
- Find perticular commits.
- Checkout different branches.
- Get a log of current branch or a global log.

Check out a demo and a more elobrate description at:  
<https://suchicodes.info/projects/projects-done/RebuilingGit/>

## Set up

There is already a complied version stored in the "git" folder.  
Incase that is not the most updated or does not exist you create it
with these commands:

```bash
javac *.java
mkdir git
mv *.class git
```

## Useage

To use the program, all you have to do is copy the "git" folder to the
folder in which you have your code.  
Once this is set you can do `java git.Main` to run the program.

Use `alias g="java git.Main"` to make it easier to use the command.

Here is the list of commands that this git (my version) takes:

- init
- add \<files>
- commit "commit message"
- status
- log
- global-log
- rm \<file>
- merge \<branch>
- reset \<commit id>
- rm-branch \<branch-name>
- branch \<branch-name>
- checkout \<branch-name>
- checkout -- \<file-name>
- checkout \<commit-ID> -- \<file-name>
