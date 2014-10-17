package org.telegram.tl;

import org.telegram.tl.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * TypeLanguage context object. It performs deserialization of objects and vectors.
 * All known classes might be registered in context for deserialization.
 * Often this might be performed from inherited class in init() method call.
 * If TL-Object contains static int field CLASS_ID, then it might be used for registration,
 * but it uses reflection so it might be slow in some cases. It recommended to manually pass CLASS_ID
 * to registerClass method.
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public abstract class TLContext {
    private final SparseArray<Class> registeredClasses = new SparseArray<Class>();
    private final SparseArray<Class> registeredCompatClasses = new SparseArray<Class>();

    public TLContext() {
        init();
    }

    /**
     * Registering of all known classes might be here
     */
    protected void init() {

    }

    /**
     * Is object supported by this context
     *
     * @param object source object
     * @return is object supported
     */
    public boolean isSupportedObject(TLObject object) {
        return isSupportedObject(object.getClassId());
    }

    /**
     * Is class supported by this context
     *
     * @param classId class id
     * @return is class supported
     */
    public boolean isSupportedObject(int classId) {
        return registeredClasses.indexOfKey(classId) >= 0;
    }

    /**
     * Registering class for serialization
     *
     * @param tClass source class
     * @param <T>    TLObject class
     */
    public <T extends TLObject> void registerClass(Class<T> tClass) {
        try {
            int classId = tClass.getField("CLASS_ID").getInt(null);
            registeredClasses.put(classId, tClass);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registering class for serialization. It work faster than {@link org.telegram.tl.TLContext#registerClass(Class)}
     * because it does not use reflection for class id.
     *
     * @param clazzId class id
     * @param tClass  source class
     * @param <T>     TLObject class
     */
    public <T extends TLObject> void registerClass(int clazzId, Class<T> tClass) {
        registeredClasses.put(clazzId, tClass);
    }

    /**
     * Registering compatibility class
     *
     * @param tClass compat class
     * @param <T>    TLObject class
     */
    public <T extends TLObject> void registerCompatClass(Class<T> tClass) {
        try {
            int classId = tClass.getField("CLASS_ID").getInt(null);
            registeredCompatClasses.put(classId, tClass);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registering compatibility class
     *
     * @param clazzId compat class id
     * @param tClass  compat class
     * @param <T>     TLObject class
     */
    public <T extends TLObject> void registerCompatClass(int clazzId, Class<T> tClass) {
        registeredCompatClasses.put(clazzId, tClass);
    }

    /**
     * Override for providing compatibility between old classes and current scheme
     *
     * @param src compat object
     * @return new object
     */
    protected TLObject convertCompatClass(TLObject src) {
        return src;
    }

    /**
     * Deserializing message from bytes
     *
     * @param data message bytes
     * @return result
     * @throws IOException reading exception
     */
    public TLObject deserializeMessage(byte[] data) throws IOException {
        return deserializeMessage(new ByteArrayInputStream(data));
    }

    /**
     * Deserializing message from stream
     *
     * @param clazzId class id
     * @param stream  source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLObject deserializeMessage(int clazzId, InputStream stream) throws IOException {
        if (clazzId == TLGzipObject.CLASS_ID) {
            TLGzipObject obj = new TLGzipObject();
            obj.deserializeBody(stream, this);
            BufferedInputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(obj.getPackedData())));
            int innerClazzId = StreamingUtils.readInt(gzipInputStream);
            return deserializeMessage(innerClazzId, gzipInputStream);
        }

        if (clazzId == TLBoolTrue.CLASS_ID) {
            return new TLBoolTrue();
        }

        if (clazzId == TLBoolFalse.CLASS_ID) {
            return new TLBoolFalse();
        }

        if (registeredCompatClasses.indexOfKey(clazzId) >= 0) {
            try {
                Class messageClass = registeredCompatClasses.get(clazzId);
                TLObject message = (TLObject) messageClass.getConstructor().newInstance();
                message.deserializeBody(stream, this);
                return convertCompatClass(message);
            } catch (DeserializeException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Unable to deserialize data");
            }
        }

        try {
            Class messageClass = registeredClasses.get(clazzId);
            if (messageClass != null) {
                TLObject message = (TLObject) messageClass.getConstructor().newInstance();
                message.deserializeBody(stream, this);
                return message;
            } else {
                throw new DeserializeException("Unsupported class: #" + Integer.toHexString(clazzId));
            }
        } catch (DeserializeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to deserialize data");
        }
    }

    /**
     * Deserializing message from stream
     *
     * @param stream source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLObject deserializeMessage(InputStream stream) throws IOException {
        int clazzId = StreamingUtils.readInt(stream);
        return deserializeMessage(clazzId, stream);
    }

    /**
     * Deserializing object vector
     *
     * @param stream source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLVector deserializeVector(InputStream stream) throws IOException {
        int clazzId = StreamingUtils.readInt(stream);
        if (clazzId == TLVector.CLASS_ID) {
            TLVector res = new TLVector();
            res.deserializeBody(stream, this);
            return res;
        } else if (clazzId == TLGzipObject.CLASS_ID) {
            TLGzipObject obj = new TLGzipObject();
            obj.deserializeBody(stream, this);
            BufferedInputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(obj.getPackedData())));
            return deserializeVector(gzipInputStream);
        } else {
            throw new IOException("Unable to deserialize vector");
        }
    }

    /**
     * Deserializing int vector
     *
     * @param stream source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLIntVector deserializeIntVector(InputStream stream) throws IOException {
        int clazzId = StreamingUtils.readInt(stream);
        if (clazzId == TLVector.CLASS_ID) {
            TLIntVector res = new TLIntVector();
            res.deserializeBody(stream, this);
            return res;
        } else if (clazzId == TLGzipObject.CLASS_ID) {
            TLGzipObject obj = new TLGzipObject();
            obj.deserializeBody(stream, this);
            BufferedInputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(obj.getPackedData())));
            return deserializeIntVector(gzipInputStream);
        } else {
            throw new IOException("Unable to deserialize vector");
        }
    }

    /**
     * Deserializing long vector
     *
     * @param stream source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLLongVector deserializeLongVector(InputStream stream) throws IOException {
        int clazzId = StreamingUtils.readInt(stream);
        if (clazzId == TLVector.CLASS_ID) {
            TLLongVector res = new TLLongVector();
            res.deserializeBody(stream, this);
            return res;
        } else if (clazzId == TLGzipObject.CLASS_ID) {
            TLGzipObject obj = new TLGzipObject();
            obj.deserializeBody(stream, this);
            BufferedInputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(obj.getPackedData())));
            return deserializeLongVector(gzipInputStream);
        } else {
            throw new IOException("Unable to deserialize vector");
        }
    }

    /**
     * Deserializing string vector
     *
     * @param stream source stream
     * @return result
     * @throws IOException reading exception
     */
    public TLStringVector deserializeStringVector(InputStream stream) throws IOException {
        int clazzId = StreamingUtils.readInt(stream);
        if (clazzId == TLVector.CLASS_ID) {
            TLStringVector res = new TLStringVector();
            res.deserializeBody(stream, this);
            return res;
        } else if (clazzId == TLGzipObject.CLASS_ID) {
            TLGzipObject obj = new TLGzipObject();
            obj.deserializeBody(stream, this);
            BufferedInputStream gzipInputStream = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(obj.getPackedData())));
            return deserializeStringVector(gzipInputStream);
        } else {
            throw new IOException("Unable to deserialize vector");
        }
    }

    /**
     * Allocating TLBytes object. Override for providing memory usage optimizations
     *
     * @param size required minimum size of data
     * @return allocated TLBytes
     */
    public TLBytes allocateBytes(int size) {
        return new TLBytes(new byte[size], 0, size);
    }

    /**
     * Releasing unused TLBytes for reuse in allocation
     *
     * @param unused unused TLBytes
     */
    public void releaseBytes(TLBytes unused) {

    }
}
