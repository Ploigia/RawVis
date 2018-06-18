package gr.ploigia.rawvis.util;

import com.google.common.collect.Range;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    /**
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param count    number of pairs to return
     * @return random row, col pairs
     */
    private static int[][] generateRandomRowColPairs(int rowCount, int colCount, int count) {
        int[][] a = new int[count][2];
        for (int i = 0; i < count; i++) {
            a[i][0] = ThreadLocalRandom.current().nextInt(rowCount);
            a[i][1] = ThreadLocalRandom.current().nextInt(colCount);
        }
        return a;
    }

    /**
     * @param min
     * @param max
     * @param count
     * @return an array of pseudorandom ints between the given min (inclusive) and max value (exclusive).
     */
    private static int[] generateRandomInts(int min, int max, int count) {
        int[] a = new int[count];
        for (int i = 0; i < count; i++) {
            a[i] = ThreadLocalRandom.current().nextInt(min, max);
        }
        return a;
    }

    public static void generateDistinctRandomIntsFile(int min, int max, int count, String file) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
        ThreadLocalRandom.current().ints(min, max).distinct().limit(count).forEach(writer::println);
        writer.close();
    }

    /* generates file with the offset of the given rows in the csvFile */
    private static void generatePointersFile(File csvFile, int[] rowIndexes, File outFile) throws IOException {
        Arrays.sort(rowIndexes);
        System.out.println(Arrays.toString(rowIndexes));

        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        long fileOffset = 0;
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        //BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
        try {
            int currentIndex = -1;
            String row = null;
            int rowLength = 0;
            for (int i : rowIndexes) {
                while (currentIndex < i) {
                    fileOffset += rowLength;
                    if ((row = reader.readLine()) == null) {
                        return;
                    }
                    rowLength = row.getBytes().length + 1;
                    currentIndex++;
                }
                if (currentIndex == i) {
                    out.writeLong(fileOffset);
                    //out.write(fileOffset + "\n");
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            out.close();
            reader.close();
        }
    }

    public static Range<Float> convertToRange(String s) {
        String[] values = s.split(":");
        return Range.open(Float.parseFloat(values[0]), Float.parseFloat(values[1]));
    }

    public static void main(String[] args) throws IOException {
        Utils.generateDistinctRandomIntsFile(0, 100000000, 1000000, "random_rows_indices");
    }


}
