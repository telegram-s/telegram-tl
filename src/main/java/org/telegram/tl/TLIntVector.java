package org.telegram.tl;

/**
 * TL Vector of integers. {@link org.telegram.tl.TLVector}
 *
 * @author Stepan Ex3NDR Korshakov (me@ex3ndr.com)
 */
public class TLIntVector extends TLVector<Integer> {
    public TLIntVector() {
        setDestClass(Integer.class);
    }

    @Override
    public String toString() {
        return "vector<int>#1cb5c415";
    }

    /**
     * Converting vector to int array
     *
     * @return int array
     */
    public int[] toIntArray() {
        int[] res = new int[size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = get(i);
        }
        return res;
    }
}
