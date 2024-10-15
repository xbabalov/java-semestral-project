package data;

public class DataException extends RuntimeException {

    public DataException(String msg) {
        super(msg);
    }

    public DataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
