package gr.ploigia.rawvis;

import com.google.common.collect.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Grid extends Tile {

    private static final Logger LOG = LogManager.getLogger(Grid.class);

    private TileCreator tileCreator;

    private Tile[][] tiles;

    private Range<Float>[] xRanges;
    private Range<Float>[] yRanges;


    private int gridSize;

    private float rowSize;
    private float colSize;


    //private RangeMap<Float, Integer> xRangeMap = TreeRangeMap.create();
    //private RangeMap<Float, Integer> yRangeMap = TreeRangeMap.create();

    public Grid(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        super(bounds, tilePointListFactory);
    }


    public void setTileCreator(TileCreator tileCreator) {
        this.tileCreator = tileCreator;
    }


    public void split(Rectangle query, int gridSize, double subtileRatio) {
        this.gridSize = gridSize;

        if (this.tiles != null)
            return;

        if (tileCreator == null) {
            tileCreator = new SimpleTileCreator();
        }

        this.yRanges = createSubranges(this.bounds.getYRange(), gridSize);
        this.xRanges = createSubranges(this.bounds.getXRange(), gridSize);

        rowSize = yRanges[0].upperEndpoint() - yRanges[0].lowerEndpoint();
        colSize = xRanges[0].upperEndpoint() - xRanges[0].lowerEndpoint();


        tiles = new Tile[gridSize][gridSize];

        //initial splitting starts here.
        InitialSplitFunction initialSplitFunc = null;

        if (query != null) {
            initialSplitFunc = new NormalDistSplitFunction(query, (int) (gridSize * gridSize * subtileRatio));
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int splitSize = 0;
                Range<Float> colTileRange = xRanges[j], rowTileRange = yRanges[i];
                if (initialSplitFunc != null) {
                    splitSize = initialSplitFunc.value(colTileRange, rowTileRange);
                }
                if (splitSize > 1) {
                    Grid subGrid = new Grid(new Rectangle(colTileRange, rowTileRange), tilePointListFactory);
                    subGrid.setTileCreator(this.tileCreator);
                    subGrid.split(null, splitSize, 0d);
                    tiles[i][j] = subGrid;
                    LOG.debug("Initial split of tile " + colTileRange + rowTileRange + " : " + splitSize * splitSize);
                } else {
                    tiles[i][j] = this.tileCreator.createTile(new Rectangle(colTileRange, rowTileRange), tilePointListFactory);
                }

            }
        }

        if (this.tilePointList != null) {
            List<Point> points = this.tilePointList.getPointList();
            for (Point point : points) {
                this.addPoint(point);
            }
            this.tilePointList.destroy();
            this.tilePointList = null;
        }
    }

    private Range[] createSubranges(Range<Float> range, int count) {
        Range<Float>[] subranges = new Range[count];
        float upper = range.lowerEndpoint();
        float rangeSize = (range.upperEndpoint() - range.lowerEndpoint()) / count;
        for (int i = 0; i < count; i++) {
            Range<Float> subrange;
            if (i == count - 1) {
                subrange = Range.closed(upper, range.upperEndpoint());
            } else {
                subrange = Range.closedOpen(upper, (upper += rangeSize));
            }
            subranges[i] = subrange;
        }
        return subranges;
    }

    private Integer getRowIndex(float y) {
        //return yRangeMap.get(y);

        float yMin = this.bounds.getYRange().lowerEndpoint();
        int i = (int) Math.floor((y - yMin) / rowSize);
        if (i == gridSize) {
            i = gridSize - 1;
        }
        return i;
    }

    private Integer getColIndex(float x) {
        //return xRangeMap.get(x);

        float xMin = this.bounds.getXRange().lowerEndpoint();
        int j = (int) Math.floor((x - xMin) / colSize);
        if (j == gridSize) {
            j = gridSize - 1;
        }
        return j;
    }

    private Tile getContainingTile(float x, float y) {
        try {
            return tiles[getRowIndex(y)][getColIndex(x)];
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error(e);
            throw e;
        }
    }

    //returns the valinor's leaf tiles that contain the specified query. it is assumed for simplicity that the query ranges are open
    @Override
    public List<Tile> getLeafTiles(Rectangle query, List leafTiles, Integer aggCol0, Integer aggCol1) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }
        Range<Float> queryXRange, queryYRange;

        try {
            queryXRange = query.getXRange().intersection(this.bounds.getXRange());
            queryYRange = query.getYRange().intersection(this.bounds.getYRange());
        } catch (IllegalArgumentException e) {
            return leafTiles;
        }

        if (queryXRange.isEmpty() || queryYRange.isEmpty()) {
            return leafTiles;
        }

        query = new Rectangle(queryXRange, queryYRange);

        int iMin = getRowIndex(queryYRange.lowerEndpoint());
        int iMax = getRowIndex(queryYRange.upperEndpoint());
        if (tiles[iMax][0].getBounds().getYRange().lowerEndpoint().equals(queryYRange.upperEndpoint()) && iMax > 0) {
            iMax--;
        }
        int jMin = getColIndex(queryXRange.lowerEndpoint());
        int jMax = getColIndex(queryXRange.upperEndpoint());
        if (tiles[0][jMax].getBounds().getXRange().lowerEndpoint().equals(queryXRange.upperEndpoint()) && jMax > 0) {
            jMax--;
        }
        for (int i = iMin; i <= iMax; i++) {
            for (int j = jMin; j <= jMax; j++) {
                tiles[i][j].getLeafTiles(query, leafTiles, aggCol0, aggCol1);
            }
        }

        return leafTiles;
    }

    @Override
    public Tile addPoint(Point point) {
        if (this.tiles == null) {
            this.tilePointList.addPoint(point);
            return this;
        } else {
            return this.getContainingTile(point.getX(), point.getY()).addPoint(point);
        }
    }

    @Override
    public String toString() {
        return tiles.length + "";
    }

    public String printTiles() {
        return Arrays.deepToString(tiles);
    }

    public int getLeafTileCount() {
        if (tiles == null) {
            return 1;
        }

        int count = 0;
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                count += tile.getLeafTileCount();
            }

        }
        return count;
    }

    public int getMaxDepth() {
        int depth = 0;
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                depth = Integer.max(depth, tile.getMaxDepth() + 1);
            }
        }
        return depth;
    }
}