package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception;

public class SameAccountCreationException extends Exception {
    public SameAccountCreationException(String detailMessage) {
        super(detailMessage);
    }

    public SameAccountCreationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
