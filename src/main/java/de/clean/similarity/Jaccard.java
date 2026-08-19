package de.clean.similarity;

import java.util.HashSet;
import java.util.Set;

/**
 * Jaccard String similarity
 */
public class Jaccard implements StringSimilarity {

    int n;

    /**
     * @param n Length of substrings
     */
    public Jaccard(int n) {
        this.n = n;
    }

    /**
     * Calculates Jaccard String similarity for x and y, using ngrams of length
     * {@link #n}
     *
     * @param x
     * @param y
     * @return Similarity score in range [0,1]
     */
    @Override
    public double compare(String x, String y) {
        double res = 0;
        Set<String> ngramsX = new HashSet<>();
        Set<String> ngramsY = new HashSet<>();
        // BEGIN SOLUTION
        //populate the two hashSets
        for (int i = 0; i <= x.length() - n; i++) {
            ngramsX.add(x.substring(i, i + n));
        }
        for (int i = 0; i <= y.length() - n; i++) {
            ngramsY.add(y.substring(i, i + n));
        }

        for (String s : ngramsX) {
            if (ngramsY.contains(s)) {
                res++;
            }
        }

        // END SOLUTION
        return res;
    }
}
