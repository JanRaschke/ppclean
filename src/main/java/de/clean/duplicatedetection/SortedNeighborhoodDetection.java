package de.clean.duplicatedetection;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.Record;
import de.clean.similarity.RecordSimilarity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Duplicate detection algorithm which first sorts the Table (according to a key)
 * and then only compares neighboring Records
 */
public class SortedNeighborhoodDetection implements DuplicateDetection {

    double threshold;
    int windowSize;
    int[] keyComponents;

    /**
     * @param threshold Threshold in range [0,1], Records at least this similar are considered Duplicates
     * @param windowSize Each record is compared with 2*(windowSize-1) neighboring Records
     * @param keyComponents Each component indicates how many characters of the content
     *                      value at the same list position are integrated in the key
     *                      Enter 0 to omit a Record value
     */
    public SortedNeighborhoodDetection(double threshold, int windowSize, int[] keyComponents) {
        this.threshold = threshold;
        this.windowSize = windowSize;
        this.keyComponents = keyComponents;
    }

    /**
     * @param table Table to check for duplicates
     * @param recSim Similarity measure to use for comparing two records
     * @return Set of detected duplicates
     */
    @Override
    public Set<Duplicate> detect(Table table, RecordSimilarity recSim) {
        Set<Duplicate> duplicates = new HashSet<>();
        int numComparisons = 0;
        // BEGIN SOLUTION
        table.generateKeys(this.keyComponents);
        table.sortByKey();
        List<Record> records = table.getData();
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < Math.min(records.size(), i + windowSize); j++) {
                Record r1 = records.get(i);
                Record r2 = records.get(j);
                numComparisons++;
                if (recSim.compare(r1, r2) >= threshold) {
                    duplicates.add(new Duplicate(r1, r2));
                }
            }
        }
        // END SOLUTION
        System.out.printf("Sorted Neighborhood Detection found %d duplicates after %d comparisons%n", duplicates.size(), numComparisons);
        return duplicates;
    }
}
