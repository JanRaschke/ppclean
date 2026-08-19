package de.clean;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.clean.data.Duplicate;
import de.clean.data.Table;
import de.clean.data.TableFactory;
import de.clean.performance.Performance;
import de.clean.similarity.Hybrid;

public class PPClean {

    public static void main(String[] args) {
        Table inputTable = TableFactory.getDefaultInputTable();
        Set<Duplicate> groundTruth = Helper.readDuplicatesFromDefaultGT();
        Performance performance = Performance.initInstance(groundTruth);
        // Hier könnt ihr nach Belieben rumexperimentieren
        // Zum Bestehen wichtig sind lediglich die Tests

        List<String> policies = Arrays.asList(null, "L", "L", "J", "L", "J");
        Hybrid hybrid = new Hybrid(policies);
        System.out.println(hybrid.compare(inputTable.getData().get(0), inputTable.getData().get(1)) + "");
    }
}
