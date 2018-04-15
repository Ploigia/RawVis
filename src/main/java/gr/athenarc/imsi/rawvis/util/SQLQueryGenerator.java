package gr.athenarc.imsi.rawvis.util;

import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.List;


public class SQLQueryGenerator {

    /**
     * @param tableName
     * @param ranges    the ranges to be applied to the given cols
     * @param cols      the names of the columns to be used in the where clause and in the select clause
     * @return a select all sql range query
     */
    public static String getSQLSelectQuery(String tableName, List<Range<Float>> ranges, String... cols) {
        String query = "select ";
        String sep = "";
        for (String col : cols) {
            query += sep + col;
            sep = ",";
        }
        query += " from " + tableName + " where " + generateWhereClause(ranges, cols) + ";";
        return query;
    }


    /**
     * @param tableName
     * @param ranges    the ranges to be applied to the given cols
     * @param cols      the names of the columns to be used in the where clause
     * @return a count all sql range query
     */
    public static String getSQLCountQuery(String tableName, List<Range<Float>> ranges, String... cols) {
        String query = "select ";
        query += "count(*) from " + tableName + " where " + generateWhereClause(ranges, cols) + ";";
        return query;
    }


    public static String getSQLAggQuery(String tableName, List<Range<Float>> ranges, String aggCol, String... cols) {
        String query = "select ";
        query += "count(*) as count, min(" + aggCol + ") as min, max(" + aggCol + ") as max, sum(" + aggCol + ") as sum, avg(" + aggCol + ") as avg  from " + tableName + " where " + generateWhereClause(ranges, cols) + ";";
        return query;
    }


    private static String generateWhereClause(List<Range<Float>> ranges, String... cols) {
        String whereClause = "";
        int i = 0;
        for (Range<Float> range : ranges) {
            String col = cols[i];
            whereClause += col + " > " + range.lowerEndpoint() + " AND " + col + " < " + range.upperEndpoint();
            if (i < ranges.size() - 1) {
                whereClause += " AND ";
            }
            i++;
        }
        return whereClause;
    }

}
