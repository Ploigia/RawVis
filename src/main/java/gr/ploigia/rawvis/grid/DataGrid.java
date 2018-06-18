package gr.ploigia.rawvis.grid;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.csv.CsvProcessor;
import gr.ploigia.rawvis.csv.CsvProcessorUnivImpl;
import gr.ploigia.rawvis.query.KWayMergePointIterator;
import gr.ploigia.rawvis.query.QueryResults;
import gr.ploigia.rawvis.query.QueryResultsTraversalPolicy;
import gr.ploigia.rawvis.query.RoundRobinPointIterator;
import gr.ploigia.rawvis.util.ContainmentExaminer;
import gr.ploigia.rawvis.util.XContainmentExaminer;
import gr.ploigia.rawvis.util.XYContainmentExaminer;
import gr.ploigia.rawvis.util.YContainmentExaminer;
import gr.ploigia.rawvis.util.io.RandomAccessReader;
import gr.ploigia.rawvis.common.Stats;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class DataGrid {

    private GridTile[][] tiles;
    private int pointsCount = 0;
    private Rectangle bounds;
    private int rowCount;
    private int colCount;
    private float rowTileSize;
    private float colTileSize;
    public int threshold;
    private RangeMap<Float, Integer> xRangeMap = TreeRangeMap.create();
    private RangeMap<Float, Integer> yRangeMap = TreeRangeMap.create();
    private int xColumn = 0;
    private int yColumn = 1;
    private boolean isGridPopulated = false;

    private String csvFilePath;

    private RandomAccessReader randomAccessReader;


    public DataGrid(Rectangle bounds, int rowCount, int colCount, int threshold) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.bounds = bounds;
        this.threshold = threshold;
        this.rowTileSize = (bounds.getYRange().upperEndpoint() - bounds.getYRange().lowerEndpoint()) / rowCount;
        this.colTileSize = (bounds.getXRange().upperEndpoint() - bounds.getXRange().lowerEndpoint()) / colCount;
        generateTiles();
    }

    private void generateTiles() {
        tiles = new GridTile[rowCount][colCount];
        float upperY = this.bounds.getYRange().lowerEndpoint();
        for (int i = 0; i < rowCount; i++) {
            Range<Float> rowTileRange;
            if (i == 0) {
                rowTileRange = Range.lessThan((upperY += rowTileSize));
            } else if (i == rowCount - 1) {
                rowTileRange = Range.atLeast(upperY);
            } else {
                rowTileRange = Range.closedOpen(upperY, (upperY += rowTileSize));
            }
            yRangeMap.put(rowTileRange, i);
            float upperX = this.bounds.getXRange().lowerEndpoint();
            for (int j = 0; j < colCount; j++) {
                Range<Float> colTileRange;
                if (j == 0) {
                    colTileRange = Range.lessThan((upperX += colTileSize));
                } else if (j == colCount - 1) {
                    colTileRange = Range.atLeast(upperX);
                } else {
                    colTileRange = Range.closedOpen(upperX, (upperX += colTileSize));
                }
                tiles[i][j] = this.createTile(new Rectangle(colTileRange, rowTileRange));
            }
        }

        for (int j = 0; j < colCount; j++) {
            Range<Float> colTileRange = tiles[0][j].bounds.getXRange();
            xRangeMap.put(colTileRange, j);
        }
    }

    public void populateGrid(String csvFilePath) {
        if (isGridPopulated) {
            throw new IllegalStateException("Grid already populated from csv file");
        }
        this.csvFilePath = csvFilePath;
        try {
            CsvProcessor csvProcessor = new CsvProcessorUnivImpl(new FileReader(csvFilePath));
            csvProcessor.selectIndexes(xColumn, yColumn);
            for (String[] row : csvProcessor) {
                long rowOffset = csvProcessor.currentRowOffset();
                if (row[0] != null && row[1] != null) {
                    this.addDataPoint(Float.parseFloat(row[0]), Float.parseFloat(row[1]), rowOffset);
                }
            }
            csvProcessor.close();
            isGridPopulated = true;

        } catch (IOException e) {
            throw new IllegalStateException("Csv file IO error", e);
        }
    }


    /**
     * Get tile at row i and column j
     *
     * @param i
     * @param j
     * @return
     */
    public GridTile getTile(int i, int j) {
        if (tiles != null) {
            return tiles[i][j];
        } else {
            return null;
        }
    }

    /**
     * Get the tile that point (x,y) belongs to
     *
     * @param x
     * @param y
     * @return
     */
    private GridTile getContainingTile(float x, float y) {
        try {
            int i = getGridRowIndex(y);
            int j = getGridColIndex(x);

            return getTile(i, j);

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Value (" + x + ", " + y + ") is not contained in any tile.");
        }
    }


    private int getGridRowIndex(float y) {
        return yRangeMap.get(y);
        /*//float yMin = this.bounds.getYRange().lowerEndpoint();
        float yMin = this.tiles[0][0].bounds.getYRange().upperEndpoint();
        int i = (int) ((y - yMin) / (double) rowTileSize);


        if (i < 0) {
            i = 0;
        } else {
            i++;
        }

        if (i > colCount - 1) {
            i = colCount - 1;
        }
        return i;*/
    }

    //todo handle case when x is the limit of the tile range
    private int getGridColIndex(float x) {
        return xRangeMap.get(x);

        /*float xMin = this.tiles[0][0].bounds.getXRange().upperEndpoint();
        //float xMin = this.bounds.getXRange().lowerEndpoint();
        int j = (int) ((x - xMin) / (double) colTileSize);
        if (j < 0) {
            j = 0;
        } else {
            j++;
        }

        if (j > rowCount - 1) {
            j = rowCount - 1;
        }
        return j;*/
    }

    public void addDataPoint(float x, float y, long fileOffset) {
        this.getContainingTile(x, y).addPoint(new Point(x, y, fileOffset));
        pointsCount++;
    }

    //returns the grid's leaf tiles that contain the specified query. it is assumed for simplicity that the query ranges are open
    private List<GridTile> getContainingLeafTiles(Rectangle query, Integer aggCol) {
        Range<Float> queryXRange = query.getXRange();
        Range<Float> queryYRange = query.getYRange();
        ArrayList<GridTile> tiles = new ArrayList<>();
        int iMin = getGridRowIndex(queryYRange.lowerEndpoint());
        int iMax = getGridRowIndex(queryYRange.upperEndpoint());
        if (getTile(iMax, 0).getBounds().getYRange().lowerEndpoint().equals(queryYRange.upperEndpoint()) && iMax > 0) {
            iMax--;
        }
        int jMin = getGridColIndex(queryXRange.lowerEndpoint());
        int jMax = getGridColIndex(queryXRange.upperEndpoint());
        if (getTile(0, jMax).getBounds().getXRange().lowerEndpoint().equals(queryXRange.upperEndpoint()) && jMax > 0) {
            jMax--;
        }
        for (int i = iMin; i <= iMax; i++) {
            for (int j = jMin; j <= jMax; j++) {
                getTile(i, j).getLeafTiles(query, tiles, aggCol);
            }
        }
        /*for (int j = 0; j < this.tiles[0].length; j++) {
            for (int i = 0; i < this.tiles.length; i++) {
                GridTile tile = this.tiles[i][j];
                if (queryXRange.isConnected(tile.bounds.getXRange()) && queryYRange.isConnected(tile.bounds.getYRange())) {
                    tiles.add(tile);
                }
            }
        }*/
        return tiles;
    }

    public void executeFilterQuery(Rectangle rectangle, DataPointProcessor pointProcessor, int filterColumn, FilterPredicate filterPredicate) throws IOException {
        if (filterColumn == this.xColumn || filterColumn == this.yColumn) {
            throw new IllegalArgumentException();
        }
        this.executeDetailsQuery(rectangle, (point, attrs) -> {
            if (filterPredicate.test(Float.parseFloat(attrs[0]))){
                pointProcessor.process(point, attrs);
            }
        }, filterColumn);
    }

    public Stats executeAggQuery(Rectangle rectangle, int column) throws IOException {
        AggCalculator aggCalculator = new AggCalculator();
        this.executeDetailsQuery(rectangle, aggCalculator, column);
        return aggCalculator.getStats();
    }

    public QueryResults executeOptimizedAggQuery(Rectangle rectangle, int column) throws IOException {
        AggCalculator aggCalculator = new AggCalculator();
        CsvProcessor csvProcessor = new CsvProcessorUnivImpl();
        csvProcessor.selectIndexes(column);
        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(this.csvFilePath));
        }
        List<TileIterator> tileIterators = this.getTileIterators(rectangle, column);
        List<TileIterator> rawAccessTileIterators = new ArrayList<>();
        List<TileIterator> tileIteratorsToInit = new ArrayList<>();

        int fullyContainedCount = 0;
        for (TileIterator tileIterator : tileIterators) {
            Stats stats = tileIterator.getTile().getStats(column);
            if (tileIterator.isFullyContained()) {
                fullyContainedCount++;
                if (stats != null) {
                    aggCalculator.processStats(stats);
                } else {
                    rawAccessTileIterators.add(tileIterator);
                    tileIteratorsToInit.add(tileIterator);
                    tileIterator.getTile().initStats(column);
                }
            } else {
                rawAccessTileIterators.add(tileIterator);
            }

        }

        KWayMergePointIterator pointIterator = new KWayMergePointIterator(rawAccessTileIterators);
        int rawReads = 0;
        while (pointIterator.hasNext()) {
            rawReads++;
            Point point = pointIterator.next();
            try {
                randomAccessReader.seek(point.getFileOffset());
                String line = randomAccessReader.readLine();
                if (line != null) {
                    String[] row = csvProcessor.parseLine(line);
                    float value = Float.parseFloat(row[0]);
                    if (pointIterator.isFullyContained()) {
                        pointIterator.getCurrentTile().adjustStats(column, value);
                    } else {
                        aggCalculator.process(point, value);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading from raw file", e);
            }
        }

        for (TileIterator tileIterator : tileIteratorsToInit) {
            Stats stats = tileIterator.getTile().getStats(column);
            if (stats != null) {
                aggCalculator.processStats(stats);
            }
        }

        QueryResults queryResults = new QueryResults();
        queryResults.setStats(aggCalculator.getStats());
        queryResults.setTileCount(tileIterators.size());
        queryResults.setRawAccessTileCount(rawAccessTileIterators.size());
        queryResults.setFullyContainedTileCount(fullyContainedCount);
        queryResults.setQuery(rectangle);
        queryResults.setRawRowsRead(rawReads);

        return queryResults;
    }


    public void executeWindowQuery(Rectangle query, DataPointProcessor pointProcessor) {
        this.executeWindowQuery(query, pointProcessor, QueryResultsTraversalPolicy.DEFAULT);
    }


    public void executeWindowQuery(Rectangle rectangle, DataPointProcessor pointProcessor, QueryResultsTraversalPolicy policy) {
        List<TileIterator> tileIterators = this.getTileIterators(rectangle, null);

        Iterator<Point> pointIterator = this.getQueryResultsIterator(policy, tileIterators);

        while (pointIterator.hasNext()) {
            pointProcessor.process(pointIterator.next(), null);
        }
    }

    private Iterator<Point> getQueryResultsIterator(QueryResultsTraversalPolicy policy, List<TileIterator> tileIterators) {
        switch (policy) {
            case K_WAY_MERGE:
                return new KWayMergePointIterator(tileIterators);
            case ROUND_ROBIN:
                return new RoundRobinPointIterator(tileIterators);
            default:
                return Iterators.concat(tileIterators.iterator());
        }
    }

    private TileIterator getTileIterator(GridTile tile, Rectangle query) {

        Range<Float> queryXRange = query.getXRange();
        Range<Float> queryYRange = query.getYRange();
        boolean checkX = !queryXRange.encloses(tile.getBounds().getXRange());
        boolean checkY = !queryYRange.encloses(tile.getBounds().getYRange());

        ContainmentExaminer containmentExaminer = null;
        if (checkX && checkY) {
            containmentExaminer = new XYContainmentExaminer(queryXRange, queryYRange);
        } else if (checkX) {
            containmentExaminer = new XContainmentExaminer(queryXRange);
        } else if (checkY) {
            containmentExaminer = new YContainmentExaminer(queryYRange);
        }
        return new TileIterator(tile, containmentExaminer);
    }


    public List<TileIterator> getTileIterators(Rectangle query, Integer aggCol) {
        if (!isGridPopulated) {
            throw new IllegalStateException("Grid not initialized");
        }

        List<GridTile> tiles = this.getContainingLeafTiles(query, aggCol);
        List<TileIterator> tileIterators = new ArrayList<>();
        for (GridTile tile : tiles) {
            tileIterators.add(getTileIterator(tile, query));
        }

        return tileIterators;
    }


    public void executeDetailsQuery(Rectangle rectangle, DataPointProcessor pointProcessor, Integer... columns) throws IOException {
        CsvProcessor csvProcessor = new CsvProcessorUnivImpl();
        csvProcessor.selectIndexes(columns);
        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(this.csvFilePath));
        }

        KWayMergePointIterator pointIterator = new KWayMergePointIterator(this.getTileIterators(rectangle, null));

        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();
            try {
                randomAccessReader.seek(point.getFileOffset());
                String line = randomAccessReader.readLine();
                if (line != null) {
                    String[] row = csvProcessor.parseLine(line);
                    pointProcessor.process(point, row);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading from raw file", e);
            }
        }
    }


    public int getPointsCount() {
        return pointsCount;
    }

    public int getColCount() {
        return colCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getxColumn() {
        return xColumn;
    }

    public void setxColumn(int xColumn) {
        this.xColumn = xColumn;
    }

    public int getyColumn() {
        return yColumn;
    }

    public void setyColumn(int yColumn) {
        this.yColumn = yColumn;
    }

    abstract public GridTile createTile(Rectangle bounds);

    @Override
    public String toString() {
        return xRangeMap.toString() + "\n" + yRangeMap.toString();
    }
}
