package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception;

public class InsufficientAccBalanceException extends Exception{
    public InsufficientAccBalanceException(String detailMessage) {
        super(detailMessage);
    }

    public InsufficientAccBalanceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
