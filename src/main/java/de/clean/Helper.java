package de.clean;

import de.clean.data.Duplicate;
import de.clean.data.Record;
import de.clean.data.Table;
import de.clean.data.TableFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static de.clean.Configuration.DATA_SEPARATOR;
import static de.clean.Configuration.PATH_DUPLICATES_TRUTH;

/**
 * Collection of utility methods
 */
public class Helper {

    /**
     * Reads a file of the following format:
     * id1	id2
     * 1	2
     * 3	4
     * After the header line, all lines represent a Record pair forming a correct Duplicate
     * The Duplicates are re-mapped to Records via the referenceTable (using ID as a position index)
     * @param pathToFile Absolute or relative path to the ground truth data
     * @param dataSeparator Separator between data points (e.g., comma for CSV)
     * @param referenceTable Table for re-mapping Record IDs to Record objects
     * @return Set of Duplicates read from file
     */
    public static Set<Duplicate> readDuplicatesFromFile(String pathToFile, String dataSeparator, Table referenceTable) {
        List<Record> referenceRecords = referenceTable.getData();
        Set<Duplicate> duplicates = new HashSet<>();
        try (BufferedReader Reader = new BufferedReader(new FileReader(pathToFile))) {
            String firstLine = Reader.readLine(); // we assume the first line to contain attribute names
            List<String> attributes = Arrays.asList(firstLine.split(dataSeparator));
            String line = null;
            while ((line = Reader.readLine()) != null) {
                List<String> duplicateIDs = Arrays.asList(line.split(dataSeparator));
                int duplicateID1 = Integer.parseInt(duplicateIDs.get(0))-1;
                int duplicateID2 = Integer.parseInt(duplicateIDs.get(1))-1;
                Record r1 = referenceRecords.get(duplicateID1);
                Record r2 = referenceRecords.get(duplicateID2);
                duplicates.add(new Duplicate(r1, r2));
            }
        } catch (Exception e) {
            System.out.printf("Could not read duplicates from %s%n", pathToFile);
        }
        return duplicates;
    }

    /**
     * @return Set of Duplicates for default ground truth path and default data separator (see {@link Configuration})
     */
    public static Set<Duplicate> readDuplicatesFromDefaultGT() {
        Table inputTable = TableFactory.getDefaultInputTable();
        return readDuplicatesFromFile(PATH_DUPLICATES_TRUTH, DATA_SEPARATOR, inputTable);
    }

    /**
     * Checks if x is greater than y by comparing them character-wise
     * @param x
     * @param y
     * @return true if x > y (character-wise), else false
     */
    public static boolean isStringGreater(String x, String y) {
        for (int i = 0; i < x.length(); i++) {
            if (i == y.length()) { // y has no more characters
                return true;
            }
            int char_xi = Character.getNumericValue(x.charAt(i));
            int char_yi = Character.getNumericValue(y.charAt(i));
            if (char_xi > char_yi)
                return true;
            if (char_xi < char_yi)
                return false;
        }
        return false; // x is a real substring of y
    }

    public static void shuffleMatrixRows(boolean[][] matrix)
    {
        int index;
        Random random = new Random();
        for (int i = matrix.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                boolean[] array = matrix[index];
                matrix[index] = matrix[i];
                matrix[i] = array;
            }
        }
    }

    /**
     * Calculates the transitive closure of a set of Duplicates.
     * If (A, B) and (B, C) are in duplicates, then (A, C) is also added.
     * @param duplicates Initial set of detected duplicates
     * @return Transitively expanded set of duplicates
     */
    public static Set<Duplicate> computeTransitiveClosure(Set<Duplicate> duplicates) {
        java.util.Map<Record, Set<Record>> adj = new java.util.HashMap<>();
        for (Duplicate d : duplicates) {
            Record r1 = d.getRecord1();
            Record r2 = d.getRecord2();
            adj.computeIfAbsent(r1, k -> new HashSet<>()).add(r2);
            adj.computeIfAbsent(r2, k -> new HashSet<>()).add(r1);
        }

        Set<Duplicate> closure = new HashSet<>();
        Set<Record> visited = new HashSet<>();

        for (Record start : adj.keySet()) {
            if (!visited.contains(start)) {
                List<Record> component = new java.util.ArrayList<>();
                java.util.Queue<Record> queue = new java.util.LinkedList<>();
                queue.add(start);
                visited.add(start);

                while (!queue.isEmpty()) {
                    Record curr = queue.poll();
                    component.add(curr);
                    for (Record neighbor : adj.getOrDefault(curr, java.util.Collections.emptySet())) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }

                for (int i = 0; i < component.size(); i++) {
                    for (int j = i + 1; j < component.size(); j++) {
                        closure.add(new Duplicate(component.get(i), component.get(j)));
                    }
                }
            }
        }
        return closure;
    }
}
