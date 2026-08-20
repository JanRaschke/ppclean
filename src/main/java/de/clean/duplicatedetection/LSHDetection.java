package de.clean.duplicatedetection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import de.clean.Helper;
import de.clean.data.Duplicate;
import de.clean.data.Record;
import de.clean.data.Table;
import de.clean.similarity.RecordSimilarity;

public class LSHDetection implements DuplicateDetection {

    // Hashing
    int HASH_BASE = 17;
    int HASH_PRIME = 19;
    // Tokenization
    int tokenSize;
    List<String> tokenUniverse;
    boolean[][] tokenMatrix;
    // MinHashing
    int numMinHashs;
    int[][] signatureMatrix;
    // Locality Sensitive Hashing
    int numBands;
    ArrayList<Hashtable<Integer, List<Integer>>> LSH;
    // Duplicate Detection
    double threshold;

    /**
     * @param tokenSize Number of characters per token
     * @param numMinHashs Number of min hashes
     * @param numBands Number of bands
     * @param threshold Similarity threshold between 0 and 1 to use for
     * filtering duplicates
     */
    public LSHDetection(int tokenSize, int numMinHashs, int numBands, double threshold) {
        if (numMinHashs % numBands != 0) {
            throw new IllegalArgumentException("numMinHashs needs to be divisible by numBands");
        }
        this.tokenSize = tokenSize;
        this.numMinHashs = numMinHashs;
        this.numBands = numBands;
        this.threshold = threshold;
    }

    /**
     * Calculates {@link LSHDetection#tokenUniverse}: a list of all tokens in
     * the entire table and {@link LSHDetection#tokenMatrix}: a boolean matrix
     * with as many rows as there are tokens in the tokenUniverse and as many
     * columns as there are records in the table. A true boolean value in cell
     * (i, j) means that the i-th token appears in the j-th record. The size of
     * tokens is determined by {@link LSHDetection#tokenSize}.
     *
     * @param table Table to use to calculate tokens
     */
    private void calculateTokens(Table table) {
        // BEGIN SOLUTION
        this.tokenUniverse = new ArrayList<>();
        List<Record> records = table.getData();
        for (Record r : records) {
            String s = r.toString();
            for (int i = 0; i <= s.length() - tokenSize; i++) {
                String token = s.substring(i, i + tokenSize);
                if (!this.tokenUniverse.contains(token)) {
                    this.tokenUniverse.add(token);
                }
            }
        }
        int numTokens = this.tokenUniverse.size();
        int numRecords = records.size();
        this.tokenMatrix = new boolean[numTokens][numRecords];
        for (int j = 0; j < numRecords; j++) {
            String s = records.get(j).toString();
            for (int i = 0; i <= s.length() - tokenSize; i++) {
                String token = s.substring(i, i + tokenSize);
                int tokenIdx = this.tokenUniverse.indexOf(token);
                if (tokenIdx != -1) {
                    this.tokenMatrix[tokenIdx][j] = true;
                }
            }
        }
        // END SOLUTION
    }

    /**
     * Calculates {@link LSHDetection#signatureMatrix}: a matrix with
     * {@link LSHDetection#numMinHashs} many rows and as many columns as there
     * are records in the table. An integer value k at cell (i,j) says that for
     * the i-th permutation of the {@link LSHDetection#tokenMatrix} and for the
     * j-th record in the table, a token of record j is at row k and rows 0 to
     * k-1 have no tokens of record j.
     *
     * @param table Table used to calculate min hashes
     */
    private void calculateMinHashes(Table table) {
        // BEGIN SOLUTION
        int numRecords = table.getData().size();
        this.signatureMatrix = new int[this.numMinHashs][numRecords];
        for (int i = 0; i < this.numMinHashs; i++) {
            if (i > 0) {
                Helper.shuffleMatrixRows(this.tokenMatrix);
            }
            for (int j = 0; j < numRecords; j++) {
                for (int k = 0; k < this.tokenMatrix.length; k++) {
                    if (this.tokenMatrix[k][j]) {
                        this.signatureMatrix[i][j] = k;
                        break;
                    }
                }
            }
        }
        // END SOLUTION
    }

    private int hash(int[] band) {
        int hash = this.HASH_BASE;
        for (int i : band) {
            hash = hash * this.HASH_PRIME + i;
        }
        return hash;
    }

    /**
     * Calculates a hashtable for every band and adds it to
     * {@link LSHDetection#LSH}. Uses {@link LSHDetection#hash(int[])} to hash a
     * band to an integer. For every hash value we store a list of record ids,
     * these lists represent buckets of duplicate candidates.
     */
    private void calculateHashBuckets() {
        // BEGIN SOLUTION
        this.LSH = new ArrayList<Hashtable<Integer, List<Integer>>>();
        int rowsPerBand = this.numMinHashs / this.numBands;
        int numRecords = this.signatureMatrix[0].length;

        for (int b = 0; b < this.numBands; b++) {
            Hashtable<Integer, List<Integer>> hashtable = new Hashtable<>();
            for (int j = 0; j < numRecords; j++) {
                int[] band = new int[rowsPerBand];
                for (int r = 0; r < rowsPerBand; r++) {
                    band[r] = this.signatureMatrix[b * rowsPerBand + r][j];
                }
                int hashKey = hash(band);
                if (!hashtable.containsKey(hashKey)) {
                    hashtable.put(hashKey, new ArrayList<>());
                }
                hashtable.get(hashKey).add(j);
            }
            this.LSH.add(hashtable);
        }
        // END SOLUTION
    }

    /**
     * First calculates tokens, minHashes and hash buckets. Then iterates over
     * all hashtables in {@link LSHDetection#LSH} and over all hash keys to
     * compare all records who share at least one hash bucket.
     *
     * @param table Table to check for duplicates
     * @param recSim Similarity measure to use for comparing two records
     * @return Set of detected duplicates
     */
    @Override
    public Set<Duplicate> detect(Table table, RecordSimilarity recSim) {
        Set<Duplicate> duplicates = new HashSet<>();
        int numComparisons = 0;
        calculateTokens(table);
        calculateMinHashes(table);
        calculateHashBuckets();
        // BEGIN SOLUTION
        List<Record> records = table.getData();
        int numRecords = records.size();
        boolean[][] compared = new boolean[numRecords][numRecords];

        for (Hashtable<Integer, List<Integer>> hashtable : this.LSH) {
            for (List<Integer> bucket : hashtable.values()) {
                if (bucket.size() > 1) {
                    for (int m = 0; m < bucket.size(); m++) {
                        for (int n = m + 1; n < bucket.size(); n++) {
                            int id1 = bucket.get(m);
                            int id2 = bucket.get(n);
                            if (id1 == id2) continue;
                            int minId = Math.min(id1, id2);
                            int maxId = Math.max(id1, id2);
                            if (!compared[minId][maxId]) {
                                compared[minId][maxId] = true;
                                Record r1 = records.get(minId);
                                Record r2 = records.get(maxId);
                                numComparisons++;
                                if (recSim.compare(r1, r2) >= threshold) {
                                    duplicates.add(new Duplicate(r1, r2));
                                }
                            }
                        }
                    }
                }
            }
        }
        // END SOLUTION
        System.out.printf("LSH Detection found %d duplicates after %d comparisons%n", duplicates.size(), numComparisons);
        return duplicates;
    }
}
