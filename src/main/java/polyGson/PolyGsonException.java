package polyGson;

public class PolyGsonException extends RuntimeException {
    public PolyGsonException(String message) {
        super(message);
    }

    public PolyGsonException(Throwable throwable) {
        super(throwable);
    }

    public PolyGsonException() {
    }
}
