package org.telegram.tl;

/**
 * TL Vector of longs. {@link org.telegram.tl.TLVector}
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TLLongVector extends TLVector<Long> {
    public TLLongVector() {
        setDestClass(Long.class);
    }

    @Override
    public String toString() {
        return "vector<long>#1cb5c415";
    }
}
