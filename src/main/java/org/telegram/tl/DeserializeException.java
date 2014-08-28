package org.telegram.tl;

import java.io.IOException;

/**
 * Exception while deserizalization
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class DeserializeException extends IOException {
    public DeserializeException() {
    }

    public DeserializeException(String s) {
        super(s);
    }

    public DeserializeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DeserializeException(Throwable throwable) {
        super(throwable);
    }
}
