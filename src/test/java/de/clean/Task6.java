package de.clean;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.duplicatedetection.LSHDetection;
import de.clean.performance.Performance;
import de.clean.performance.Score;
import de.clean.similarity.SingleAttributeEquality;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Task6 {

    Table inputTable = TableFactory.getDefaultInputTable();
    Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
    Performance performance = Performance.initInstance(groundTruth);

    @Test
    void testLSHDetection() {
        System.out.println("[Task6: testLSHDetection]");
        System.out.println("Creating SingleAttributeEquality for name comparison (attributeIndex: 1)...");
        SingleAttributeEquality nameAttributeEquality = new SingleAttributeEquality(1);
        System.out.println("Creating LSHDetection with the following parameteres:");
        System.out.println("  similarity threshold: 0.8, token size: 2, number min hashes: 60, number bands: 20");
        LSHDetection LSHDetection = new LSHDetection(2, 60, 20, 0.8);
        System.out.println("Applying LSHDetection with SingleAttributeEquality...");
        Set<Duplicate> duplicates = LSHDetection.detect(inputTable, nameAttributeEquality);
        Score s = performance.evaluate(duplicates);
        System.out.println("Expected results (circa due to random permutations): Precision: ~1.0, Recall: ~0.7, F1-Score: ~0.8");
        assertEquals(1.0, s.getPrecision(), 0.1);
        assertEquals(0.7, s.getRecall(), 0.1);
        assertEquals(0.8, s.getF1(), 0.1);
    }
}
