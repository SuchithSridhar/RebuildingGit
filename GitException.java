package git;
/** General exception indicating a Git error.  For fatal errors, the
 *  result of .getMessage() is the error message to be printed.
 *  @author P. N. Hilfinger
 */
class GitException extends RuntimeException {


    /** A GitException with no message. */
    GitException() {
        super();
    }

    /** A GitException MSG as its message. */
    GitException(String msg) {
        super(msg);
    }

}
