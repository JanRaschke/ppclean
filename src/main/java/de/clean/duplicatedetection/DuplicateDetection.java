package de.clean.duplicatedetection;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.Record;
import de.clean.similarity.RecordSimilarity;

import java.util.Set;

/**
 * Interface for duplicate detection algorithms
 */
public interface DuplicateDetection {
    /**
     * @param table Table to check for duplicates
     * @param recSim Similarity measure to use for comparing two records
     * @return Set of detected duplicates
     */
    Set<Duplicate> detect(Table table, RecordSimilarity recSim);
}
