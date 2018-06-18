package gr.ploigia.rawvis.util;


import com.google.common.collect.Range;
import gr.ploigia.rawvis.common.Rectangle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class QueryUtils {

    /**
     * @param queryFiles the files to read the queries from
     * @return a list of the queries specified in queryFiles
     * @throws IOException
     * @throws SQLException
     */
    public static List<Rectangle> readQueries(String... queryFiles) {
        List<Rectangle> rangeQueries = new ArrayList<>();

        try {
            for (String queryFile : queryFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(queryFile));
                String row;
                while ((row = reader.readLine()) != null) {
                    String[] ranges = row.split(",");
                    rangeQueries.add(new Rectangle(Utils.convertToRange(ranges[0]), Utils.convertToRange(ranges[1])));
                }
                reader.close();
            }
            return rangeQueries;
        } catch (IOException e) {
            throw new IllegalStateException("Query Files IO error.", e);
        }
    }

    public static Rectangle moveQuery(Rectangle query, float overlap, Direction direction) {

        Range<Float> xRange = query.getXRange();
        Range<Float> yRange = query.getYRange();
        float interval;
        switch (direction) {
            case N:
                interval = (yRange.upperEndpoint() - yRange.lowerEndpoint()) * (1 - overlap);
                yRange = Range.open(yRange.lowerEndpoint() + interval, yRange.upperEndpoint() + interval);
                break;
            case S:
                interval = (yRange.upperEndpoint() - yRange.lowerEndpoint()) * (1 - overlap);
                yRange = Range.open(yRange.lowerEndpoint() - interval, yRange.upperEndpoint() - interval);
                break;
            case W:
                interval = (xRange.upperEndpoint() - xRange.lowerEndpoint()) * (1 - overlap);
                xRange = Range.open(xRange.lowerEndpoint() - interval, xRange.upperEndpoint() - interval);
                break;
            case E:
                interval = (xRange.upperEndpoint() - xRange.lowerEndpoint()) * (1 - overlap);
                xRange = Range.open(xRange.lowerEndpoint() + interval, xRange.upperEndpoint() + interval);
                break;
        }
        return new Rectangle(xRange, yRange);
    }

    public static List<Rectangle> generateQuerySequence(Rectangle query, float overlap, Direction... directions) {
        List<Rectangle> queries = new ArrayList<>();
        queries.add(query);

        for (Direction direction : directions) {
            query = moveQuery(query, overlap, direction);
            queries.add(query);
        }
        return queries;
    }
}