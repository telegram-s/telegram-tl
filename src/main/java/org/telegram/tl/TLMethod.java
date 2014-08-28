package org.telegram.tl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basic object for RPC methods. It contains special methods for deserializing result of RPC method call.
 *
 * @param <T> return type of method
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public abstract class TLMethod<T extends TLObject> extends TLObject {
    /**
     * Deserialization of method response
     *
     * @param data    data response
     * @param context tl context
     * @return deserialized response
     * @throws IOException reading exceptions
     */
    public T deserializeResponse(byte[] data, TLContext context) throws IOException {
        return deserializeResponse(new ByteArrayInputStream(data), context);
    }

    /**
     * Deserialization of method response
     *
     * @param stream  source stream
     * @param context tl context
     * @return deserizlied response
     * @throws IOException reading exceptions
     */
    public abstract T deserializeResponse(InputStream stream, TLContext context) throws IOException;
}
