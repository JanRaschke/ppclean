package de.clean.similarity;

import java.util.List;

import de.clean.data.Record;

/**
 * Record similarity for comparing two Records attribute by attribute either
 * with {@link Levenshtein} or {@link Jaccard}
 */
public class Hybrid implements RecordSimilarity {

    private List<String> policies;
    private final int JACCARD_N = 3;
    private StringSimilarity jaccard;
    private StringSimilarity levenshtein;

    /**
     * @param policies List of comparison policies, write "L" for
     * {@link Levenshtein}, "J" {@link Jaccard}, and null to skip an attribute.
     * The policies are applied in order of attributes (e.g., first policy to
     * first attribute)
     */
    public Hybrid(List<String> policies) {
        this.policies = policies;
        this.jaccard = new Jaccard(JACCARD_N);
        this.levenshtein = new Levenshtein();
    }

    /**
     * Compares two Records attribute by attribute according to
     * {@link #policies}. For Jaccard similarity, a default window size of
     * {@link #JACCARD_N} is used
     *
     * @param r1
     * @param r2
     * @return Similarity score in range [0,1] (1=same, 0=very different)
     */
    @Override
    public double compare(Record r1, Record r2) {
        double res = 0;
        // BEGIN SOLUTION
        List<String> record1 = r1.getContent();
        List<String> record2 = r2.getContent();

        //calc number of attr
        int n = record1.size();

        //sum of algorith results
        double sum = 0.0;
        //number of used algorithms
        int count = 0;

        for (int i = 0; i < n; i++) {
            String policy = policies.get(i);

            if (policy == null) {
                continue;
            } else if (policy.equals("L")) {
                sum += levenshtein.compare(record1.get(i), record2.get(i));
                count++;
            } else if (policy.equals("J")) {
                sum += jaccard.compare(record1.get(i), record2.get(i));
                count++;
            }

        }

        res = sum / count;
        // END SOLUTION
        return res;
    }
}
