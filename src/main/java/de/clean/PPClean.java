package de.clean;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.performance.Performance;

import java.util.Set;

public class PPClean {

    public static void main(String[] args) {
        Table inputTable = TableFactory.getDefaultInputTable();
        Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
        Performance performance = Performance.initInstance(groundTruth);
        // Hier könnt ihr nach Belieben rumexperimentieren
        // Zum Bestehen wichtig sind lediglich die Tests
    }
}
