package de.clean;

import de.clean.data.Duplicate;
import de.clean.data.Record;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.duplicatedetection.NaiveDetection;
import de.clean.performance.Performance;
import de.clean.performance.Score;
import de.clean.similarity.SingleAttributeEquality;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task1 {

    Table inputTable = TableFactory.getDefaultInputTable();
    Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
    Performance performance = Performance.initInstance(groundTruth);

    @Test
    public void testSingleAttributeEquality() {
        System.out.println("[Task1: testSingleAttributeEquality]");
        System.out.println("Creating SingleAttributeEquality for name comparison (attributeIndex: 1)...");
        SingleAttributeEquality nameAttributeEquality = new SingleAttributeEquality(1);
        System.out.println("Comparing first four records with respect to their name attribute...");
        Record r1 = inputTable.getData().get(0);
        Record r2 = inputTable.getData().get(1);
        Record r3 = inputTable.getData().get(2);
        Record r4 = inputTable.getData().get(3);
        System.out.println("Comparing first and second record: Expecting 1 (equality)");
        assertEquals(1, nameAttributeEquality.compare(r1, r2), 0);
        System.out.println("Comparing second and third record: Expecting 0 (inequality)");
        assertEquals(0, nameAttributeEquality.compare(r2, r3), 0);
        System.out.println("Comparing third and fourth record: Expecting 0 (inequality)");
        assertEquals(0, nameAttributeEquality.compare(r3, r4), 0);
    }

    @Test
    void testNaiveDetection() {
        System.out.println("[Task1: testNaiveDetection]");
        System.out.println("Creating SingleAttributeEquality for name comparison (attributeIndex: 1)...");
        SingleAttributeEquality nameAttributeEquality = new SingleAttributeEquality(1);
        System.out.println("Applying NaiveDetection with SingleAttributeEquality...");
        Set<Duplicate> duplicates = new NaiveDetection(1).detect(inputTable, nameAttributeEquality);
        Score s = performance.evaluate(duplicates);
        System.out.println("Expected results: Precision 0.931, Recall: 0.732, F1-Score: 0.820");
        assertEquals(0.931, s.getPrecision(), 0.05);
        assertEquals(0.732, s.getRecall(), 0.05);
        assertEquals(0.820, s.getF1(), 0.05);
    }
}
