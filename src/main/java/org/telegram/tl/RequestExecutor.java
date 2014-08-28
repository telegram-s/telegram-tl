package org.telegram.tl;

import java.io.IOException;

/**
 * Basic class of rpc method index class
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
@Deprecated
public abstract class RequestExecutor {
    public abstract byte[] doRpcCall(byte[] request) throws IOException;
}
