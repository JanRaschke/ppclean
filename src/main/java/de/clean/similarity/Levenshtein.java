package de.clean.similarity;

/**
 * Levenshtein String similarity
 */
public class Levenshtein implements StringSimilarity {

    public Levenshtein() {
    }

    /**
     * Calculates Levenshtein String similarity for x and y
     *
     * @param x
     * @param y
     * @return Similarity score in range [0,1]
     */
    @Override
    public double compare(String x, String y) {
        double res = 0;
        int m = x.length();
        int n = y.length();
        // BEGIN SOLUTION
        int[][] matrix = new int[m + 1][n + 1];
        //populate matrix with basic cases
        for (int i = 0; i < m + 1; i++) {
            matrix[i][0] = i;
        }
        for (int i = 0; i < n + 1; i++) {
            matrix[0][i] = i;
        }
        for (int i = 0; i < m + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                if (x.charAt(i) == y.charAt(j)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = 1 + Math.min(compare(x.substring(0, m - 1), y.substring(0, n - 1)),)
                }

            }
        }
        //build visual table for understanding
        String s = "";
        for (int i = 0; i < m + 1; i++) {
            for (int j = 0; j < n + 1; j++) {
                s += matrix[i][j] + " ";
            }
            s += "\n";
        }
        System.out.println("hallo" + "\n" + s);
        // END SOLUTION
        return res;
    }
}
