package gr.ploigia.rawvis;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import gr.ploigia.rawvis.query.FilterPredicate;
import gr.ploigia.rawvis.query.QueryResults;
import gr.ploigia.rawvis.query.QueryResultsTraversalPolicy;
import gr.ploigia.rawvis.util.ContainmentExaminer;
import gr.ploigia.rawvis.util.XContainmentExaminer;
import gr.ploigia.rawvis.util.XYContainmentExaminer;
import gr.ploigia.rawvis.util.YContainmentExaminer;
import gr.ploigia.rawvis.util.csv.CsvProcessor;
import gr.ploigia.rawvis.util.csv.CsvProcessorUnivImpl;
import gr.ploigia.rawvis.util.io.RandomAccessReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Valinor {

    private static final Logger LOG = LogManager.getLogger(Valinor.class);

    private String csvFilePath;
    private RandomAccessReader randomAccessReader;

    private Grid grid;

    private int xColumn = 0;
    private int yColumn = 1;

    private Character delimiter;

    private Rectangle bounds;

    private TileCreator tileCreator;

    private TilePointListFactory tilePointListFactory = new TilePointListFactoryImpl();

    private Integer gridSize;

    private double subtileRatio = 0;


    private boolean isInitialized = false;

    public Valinor(String csvFilePath, Rectangle bounds) {
        this.csvFilePath = csvFilePath;
        this.bounds = bounds;
    }

    public void initialize() {
        initialize(null, null, null);
    }

    public QueryResults initialize(Rectangle query, Integer aggCol1, Integer aggCol2) {
        if (isInitialized)
            throw new IllegalStateException("Grid is already initialized");

        LOG.debug("Generating top level grid with " + gridSize * gridSize + " tiles and subtileRatio " + subtileRatio);

        grid = new Grid(bounds, tilePointListFactory);
        grid.setTileCreator(tileCreator);
        grid.split(query, gridSize, subtileRatio);

        QueryResults queryResults = null;

        try {
            CsvProcessor csvProcessor = new CsvProcessorUnivImpl(new FileReader(csvFilePath));
            if (delimiter != null) {
                csvProcessor.setDelimiter(delimiter);
            }

            if (aggCol1 != null) {
                if (aggCol2 != null) {
                    csvProcessor.selectIndexes(xColumn, yColumn, aggCol1, aggCol2);
                } else {
                    csvProcessor.selectIndexes(xColumn, yColumn, aggCol1);
                }
            } else {
                csvProcessor.selectIndexes(xColumn, yColumn);
            }

            StatsAccumulator statsAccumulator = null;
            PairedStatsAccumulator pairedStatsAccumulator = null;

            int i = 0;
            if (aggCol1 == null) {
                for (String[] row : csvProcessor) {
                    i++;
                    long rowOffset = csvProcessor.currentRowOffset();

                    try {
                        Point point = new Point(Float.parseFloat(row[0]), Float.parseFloat(row[1]), rowOffset);
                        this.grid.addPoint(point);

                    } catch (Exception e) {
                        LOG.error("Problem parsing row number " + i + ": " + Arrays.toString(row), e);
                        continue;
                    }

                }

            } else if (aggCol2 == null) {
                csvProcessor.selectIndexes(xColumn, yColumn, aggCol1);

                statsAccumulator = new StatsAccumulator();
                for (String[] row : csvProcessor) {
                    i++;
                    long rowOffset = csvProcessor.currentRowOffset();
                    try {
                        Point point = new Point(Float.parseFloat(row[0]), Float.parseFloat(row[1]), rowOffset);
                        Float value = Float.parseFloat(row[2]);
                        Tile tile = this.grid.addPoint(point);
                        tile.adjustStats(aggCol1, value);
                        if (query != null && query.contains(point)) {
                            statsAccumulator.add(value);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        LOG.error("Problem parsing row number " + i + ": " + Arrays.toString(row), e);
                        continue;
                    }
                }
            } else {
                csvProcessor.selectIndexes(xColumn, yColumn, aggCol1, aggCol2);

                pairedStatsAccumulator = new PairedStatsAccumulator();
                for (String[] row : csvProcessor) {
                    i++;
                    long rowOffset = csvProcessor.currentRowOffset();
                    try {
                        Point point = new Point(Float.parseFloat(row[0]), Float.parseFloat(row[1]), rowOffset);
                        Float value1 = Float.parseFloat(row[2]);
                        Float value2 = Float.parseFloat(row[3]);
                        Tile tile = this.grid.addPoint(point);
                        tile.adjustPairedStats(aggCol1, aggCol2, value1, value2);
                        if (query != null && query.contains(point)) {
                            pairedStatsAccumulator.add(value1, value2);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        LOG.error("Problem parsing row number " + i + ": " + Arrays.toString(row), e);
                        continue;
                    }
                }

            }

            csvProcessor.close();
            isInitialized = true;

            if (query != null) {
                queryResults = new QueryResults();
                queryResults.setQuery(query);
                if (pairedStatsAccumulator != null) {
                    queryResults.setStats(pairedStatsAccumulator.snapshot());
                } else if (statsAccumulator != null) {
                    queryResults.setStats(statsAccumulator.snapshot());
                }
            }
            return queryResults;

        } catch (IOException e) {
            throw new IllegalStateException("Csv file IO error", e);
        }

    }

    public void executeWindowQuery(Rectangle rectangle, DataPointProcessor pointProcessor, QueryResultsTraversalPolicy policy) {
        if (!isInitialized) {
            initialize();
        }
        List<TileIterator> tileIterators = this.getTileIterators(rectangle, null, null);
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

    public void executeDetailsQuery(Rectangle rectangle, DataPointProcessor pointProcessor, Integer... columns) throws IOException {
        CsvProcessor csvProcessor = new CsvProcessorUnivImpl();
        if (delimiter != null) {
            csvProcessor.setDelimiter(delimiter);
        }
        csvProcessor.selectIndexes(columns);
        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(this.csvFilePath));
        }

        KWayMergePointIterator pointIterator = new KWayMergePointIterator(this.getTileIterators(rectangle, null, null));

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


    public void executeFilterQuery(Rectangle rectangle, DataPointProcessor pointProcessor, int filterColumn, FilterPredicate filterPredicate) throws IOException {
        if (filterColumn == this.xColumn || filterColumn == this.yColumn) {
            throw new IllegalArgumentException();
        }
        this.executeDetailsQuery(rectangle, (point, attrs) -> {
            if (filterPredicate.test(Float.parseFloat(attrs[0]))) {
                pointProcessor.process(point, attrs);
            }
        }, filterColumn);
    }

    public Stats executeNonOptimizedAggQuery(Rectangle rectangle, int column) throws IOException {
        StatsAccumulator statsAccumulator = new StatsAccumulator();
        DataPointProcessor pointProcessor = (point, attrs) -> {
            float value = Float.parseFloat(attrs[0]);
            statsAccumulator.add(value);
        };
        this.executeDetailsQuery(rectangle, pointProcessor, column);
        return statsAccumulator.snapshot();
    }

    public QueryResults executeAggQuery(Rectangle rectangle, int aggCol) throws IOException {
        if (!isInitialized) {
            return initialize(rectangle, aggCol, null);
        }

        StatsAccumulator statsAccumulator = new StatsAccumulator();
        CsvProcessor csvProcessor = new CsvProcessorUnivImpl();
        if (delimiter != null) {
            csvProcessor.setDelimiter(delimiter);
        }
        csvProcessor.selectIndexes(aggCol);
        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(this.csvFilePath));
        }
        List<TileIterator> tileIterators = this.getTileIterators(rectangle, aggCol, null);
        List<TileIterator> rawAccessTileIterators = new ArrayList<>();
        List<TileIterator> tileIteratorsToInit = new ArrayList<>();

        int fullyContainedCount = 0;
        for (TileIterator tileIterator : tileIterators) {
            StatsAccumulator tileStatsAcc = tileIterator.getTile().getStats(aggCol);
            if (tileIterator.isFullyContained()) {
                fullyContainedCount++;
                if (tileStatsAcc != null) {
                    statsAccumulator.addAll(tileStatsAcc.snapshot());
                } else {
                    rawAccessTileIterators.add(tileIterator);
                    tileIteratorsToInit.add(tileIterator);
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
                        pointIterator.getCurrentTile().adjustStats(aggCol, value);
                    } else {
                        statsAccumulator.add(value);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading from raw file", e);
            }
        }

        for (TileIterator tileIterator : tileIteratorsToInit) {
            StatsAccumulator tileStatsAcc = tileIterator.getTile().getStats(aggCol);
            if (tileStatsAcc != null) {
                statsAccumulator.addAll(tileStatsAcc.snapshot());
            }
        }

        QueryResults queryResults = new QueryResults();
        queryResults.setStats(statsAccumulator.snapshot());
        queryResults.setTileCount(tileIterators.size());
        queryResults.setRawAccessTileCount(rawAccessTileIterators.size());
        queryResults.setFullyContainedTileCount(fullyContainedCount);
        queryResults.setQuery(rectangle);
        queryResults.setRawRowsRead(rawReads);

        return queryResults;
    }

    public QueryResults executeAggQuery(Rectangle rectangle, int aggCol1, int aggCol2) throws IOException {
        if (!isInitialized) {
            return initialize(rectangle, aggCol1, aggCol2);
        }

        PairedStatsAccumulator statsAccumulator = new PairedStatsAccumulator();
        CsvProcessor csvProcessor = new CsvProcessorUnivImpl();
        if (delimiter != null) {
            csvProcessor.setDelimiter(delimiter);
        }
        csvProcessor.selectIndexes(aggCol1, aggCol2);
        if (randomAccessReader == null) {
            randomAccessReader = RandomAccessReader.open(new File(this.csvFilePath));
        }
        List<TileIterator> tileIterators = this.getTileIterators(rectangle, aggCol1, aggCol2);
        List<TileIterator> rawAccessTileIterators = new ArrayList<>();
        List<TileIterator> tileIteratorsToInit = new ArrayList<>();

        int fullyContainedCount = 0;
        for (TileIterator tileIterator : tileIterators) {
            PairedStatsAccumulator tileStatsAcc = tileIterator.getTile().getPairedStats(aggCol1, aggCol2);
            if (tileIterator.isFullyContained()) {
                fullyContainedCount++;
                if (tileStatsAcc != null) {
                    statsAccumulator.addAll(tileStatsAcc.snapshot());
                } else {
                    rawAccessTileIterators.add(tileIterator);
                    tileIteratorsToInit.add(tileIterator);
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
                    float value1 = Float.parseFloat(row[0]);
                    float value2 = Float.parseFloat(row[1]);

                    if (pointIterator.isFullyContained()) {
                        pointIterator.getCurrentTile().adjustPairedStats(aggCol1, aggCol2, value1, value2);
                    } else {
                        statsAccumulator.add(value1, value2);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error reading from raw file", e);
            }
        }

        for (TileIterator tileIterator : tileIteratorsToInit) {
            PairedStatsAccumulator tileStatsAcc = tileIterator.getTile().getPairedStats(aggCol1, aggCol2);
            if (tileStatsAcc != null) {
                statsAccumulator.addAll(tileStatsAcc.snapshot());
            }
        }

        QueryResults queryResults = new QueryResults();
        queryResults.setStats(statsAccumulator.snapshot());
        queryResults.setTileCount(tileIterators.size());
        queryResults.setRawAccessTileCount(rawAccessTileIterators.size());
        queryResults.setFullyContainedTileCount(fullyContainedCount);
        queryResults.setQuery(rectangle);
        queryResults.setRawRowsRead(rawReads);

        return queryResults;
    }

    private TileIterator getTileIterator(Tile tile, Rectangle query) {

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

    public List<TileIterator> getTileIterators(Rectangle query, Integer aggCol0, Integer aggCol1) {
        List<Tile> tiles = this.grid.getLeafTiles(query, null, aggCol0, aggCol1);
        List<TileIterator> tileIterators = new ArrayList<>();
        for (Tile tile : tiles) {
            tileIterators.add(getTileIterator(tile, query));
        }
        return tileIterators;
    }

    public void setxColumn(int xColumn) {
        this.xColumn = xColumn;
    }

    public void setyColumn(int yColumn) {
        this.yColumn = yColumn;
    }

    public void setTilePointListFactory(TilePointListFactory tilePointListFactory) {
        this.tilePointListFactory = tilePointListFactory;
    }

    public void setTileCreator(TileCreator tileCreator) {
        this.tileCreator = tileCreator;
    }

    public void setDelimiter(Character delimiter) {
        this.delimiter = delimiter;
    }

    public int getLeafTileCount() {
        return this.grid.getLeafTileCount();
    }

    public void setGridSize(Integer gridSize) {
        this.gridSize = gridSize;
    }

    public double getSubtileRatio() {
        return subtileRatio;
    }

    public void setSubtileRatio(double subtileRatio) {
        this.subtileRatio = subtileRatio;
    }

    @Override
    public String toString() {
        return grid.printTiles();
    }
}
