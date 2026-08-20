package de.clean;

import java.util.Set;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.duplicatedetection.LSHDetection;
import de.clean.performance.Performance;
import de.clean.performance.Score;
import de.clean.similarity.SingleAttributeEquality;

public class PPClean {

    public static void main(String[] args) {
        Table inputTable = TableFactory.getDefaultInputTable();
        Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
        Performance performance = Performance.initInstance(groundTruth);

        System.out.println("--- Running LSH Duplicate Detection ---");
        SingleAttributeEquality nameAttributeEquality = new SingleAttributeEquality(1);
        LSHDetection lshDetection = new LSHDetection(2, 60, 20, 0.8);
        Set<Duplicate> rawDuplicates = lshDetection.detect(inputTable, nameAttributeEquality);

        Score rawScore = performance.evaluate(rawDuplicates);
        System.out.println("Before Transitive Closure:");
        System.out.printf("  Precision: %.4f, Recall: %.4f, F1-Score: %.4f%n",
                rawScore.getPrecision(), rawScore.getRecall(), rawScore.getF1());

        System.out.println("\n--- Applying Transitive Closure (Aufgabe 7 Subtask 3) ---");
        Set<Duplicate> closureDuplicates = Helper.computeTransitiveClosure(rawDuplicates);
        Score closureScore = performance.evaluate(closureDuplicates);
        System.out.println("After Transitive Closure:");
        System.out.printf("  Precision: %.4f, Recall: %.4f, F1-Score: %.4f%n",
                closureScore.getPrecision(), closureScore.getRecall(), closureScore.getF1());
    }
}
