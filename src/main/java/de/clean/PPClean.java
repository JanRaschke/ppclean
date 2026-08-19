package de.clean;

import java.util.Set;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.duplicatedetection.LSHDetection;
import de.clean.performance.Performance;
import de.clean.similarity.SingleAttributeEquality;

public class PPClean {

    public static void main(String[] args) {
        Table inputTable = TableFactory.getDefaultInputTable();
        Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
        Performance performance = Performance.initInstance(groundTruth);
        // Hier könnt ihr nach Belieben rumexperimentieren
        // Zum Bestehen wichtig sind lediglich die Tests
        SingleAttributeEquality nameAttributeEquality = new SingleAttributeEquality(1);
        LSHDetection LSHDetection = new LSHDetection(2, 60, 20, 0.8);
        LSHDetection.detect(inputTable, nameAttributeEquality);

    }
}
