package de.clean.similarity;

import java.util.List;

import de.clean.data.Record;

/**
 * Simple heuristic to compare two Records It compares two Records for equality
 * based only on a single attribute
 */
public class SingleAttributeEquality implements RecordSimilarity {

    int attributeIndex;

    /**
     * @param attributeIndex Position of Record content at which to check for
     * equality
     */
    public SingleAttributeEquality(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    /**
     * @param r1
     * @param r2
     * @return 1 if r1 and r2 are equal at position {@link #attributeIndex},
     * else 0
     */
    @Override
    public double compare(Record r1, Record r2) {
        double res = 0;
        // BEGIN SOLUTION
        List<String> record1 = r1.getContent();
        List<String> record2 = r2.getContent();
        if (record1.get(attributeIndex).equals(record2.get(attributeIndex))) {
            res = 1;
        }
        // END SOLUTION
        return res;
    }
}
