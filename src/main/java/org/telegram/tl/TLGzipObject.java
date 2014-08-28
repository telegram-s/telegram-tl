package org.telegram.tl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.telegram.tl.StreamingUtils.readTLBytes;
import static org.telegram.tl.StreamingUtils.writeTLBytes;

/**
 * Basic class of gzipped object
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TLGzipObject extends TLObject {
    public static final int CLASS_ID = 0x3072CFA1;

    private byte[] packedData;

    public TLGzipObject() {

    }

    public TLGzipObject(byte[] packedData) {
        this.packedData = packedData;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /**
     * Getting gzipped content
     *
     * @return GZipped content
     */
    public byte[] getPackedData() {
        return packedData;
    }

    /**
     * Setting packed content
     *
     * @param packedData gzipped content
     */
    public void setPackedData(byte[] packedData) {
        this.packedData = packedData;
    }

    @Override
    public void serializeBody(OutputStream stream) throws IOException {
        writeTLBytes(packedData, stream);
    }

    @Override
    public void deserializeBody(InputStream stream, TLContext context) throws IOException {
        packedData = readTLBytes(stream);
    }

    @Override
    public String toString() {
        return "gzip_packed#3072cfa1";
    }
}
