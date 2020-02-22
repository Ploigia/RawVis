package gr.ploigia.rawvis;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class QuerySplitTile extends Tile {

    private static final Logger LOG = LogManager.getLogger(QuerySplitTile.class);

    private int threshold;

    private QuerySplitTile[][] subTiles;

    public QuerySplitTile(Rectangle bounds, int threshold, TilePointListFactory tilePointListFactory) {
        super(bounds, tilePointListFactory);
        this.threshold = threshold;
    }

    @Override
    public List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol0, Integer aggCol1) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }

        if (this.tilePointList != null) {
            boolean isFullyContained = query.encloses(bounds);
            if (this.tilePointList.getPointList().size() > threshold && (!isFullyContained || !this.hasStats(aggCol0, aggCol1))) {
                //todo
                try {
                    if (query.getXRange().lowerEndpoint() <= this.bounds.getXRange().lowerEndpoint() &&
                            query.getXRange().upperEndpoint() >= this.bounds.getXRange().upperEndpoint() &&
                            query.getYRange().lowerEndpoint() <= this.bounds.getYRange().lowerEndpoint() &&
                            query.getYRange().upperEndpoint() >= this.bounds.getYRange().upperEndpoint()) {
                        this.split(null);
                    } else {
                        this.split(query);
                    }

                    for (QuerySplitTile[] subTileRow : subTiles) {
                        for (QuerySplitTile subTile : subTileRow) {
                            if (subTile.getBounds().intersects(query)) {
                                //LOG.debug("Adding leaftile " + subTile.bounds + " with " + subTile.tilePointList.getPointList().size() + " objects");
                                leafTiles.add(subTile);
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    leafTiles.add(this);
                }
            } else {
                leafTiles.add(this);
            }
        } else {
            for (QuerySplitTile[] subTileRow : subTiles) {
                for (QuerySplitTile subTile : subTileRow) {
                    if (subTile.getBounds().intersects(query)) {
                        subTile.getLeafTiles(query, leafTiles, aggCol0, aggCol1);
                    }
                }
            }
        }
        return leafTiles;
    }

    private List<Range<Float>> splitRangeInHalf(Range<Float> range) {
        float middle = (range.upperEndpoint() + range.lowerEndpoint()) / 2f;
        Range rangeLower = Range.range(range.lowerEndpoint(), range.lowerBoundType(),
                middle, BoundType.CLOSED);
        Range rangeUpper = Range.range(middle, BoundType.OPEN, range.upperEndpoint(), range.upperBoundType());
        List<Range<Float>> subRanges = new ArrayList<>(2);
        subRanges.add(rangeLower);
        subRanges.add(rangeUpper);
        return subRanges;

    }

    private List<Range<Float>> splitRangeByQuery(Range<Float> range, Range<Float> queryRange) {
        List<Range<Float>> subRanges = new ArrayList<>();

        Range<Float> intersection = range.intersection(queryRange);

        if (range.lowerEndpoint() < intersection.lowerEndpoint()) {
            subRanges.add(Range.range(range.lowerEndpoint(), range.lowerBoundType(), intersection.lowerEndpoint(), reverseBoundType(intersection.lowerBoundType())));
        }

        BoundType lowerBoundType = range.lowerEndpoint().floatValue() == intersection.lowerEndpoint() ? range.lowerBoundType() : intersection.lowerBoundType();
        BoundType upperBoundType = range.upperEndpoint().floatValue() == intersection.upperEndpoint() ? range.upperBoundType() : intersection.upperBoundType();

        subRanges.add(Range.range(intersection.lowerEndpoint(), lowerBoundType, intersection.upperEndpoint(), upperBoundType));

        if (range.upperEndpoint() > intersection.upperEndpoint()) {
            subRanges.add(Range.range(intersection.upperEndpoint(), reverseBoundType(intersection.upperBoundType()), range.upperEndpoint(), range.upperBoundType()));
        }

        /*RangeSet<Float> rangeSet = null;
        rangeSet = TreeRangeSet.create();
        rangeSet.add(range);
        rangeSet.remove(intersection);
        List<Range<Float>> subRanges = new ArrayList<>(rangeSet.asRanges());
        subRanges.add(intersection);*/
        return subRanges;
    }

    private void split(Rectangle query) {
        try {
            //LOG.debug("Splitting tile " + this.bounds + " with " + this.tilePointList.getPointList().size() + " objects");
            Range<Float> xRange = this.bounds.getXRange();
            Range<Float> yRange = this.bounds.getYRange();

            List<Range<Float>> xSubRanges = query != null ? splitRangeByQuery(xRange, query.getXRange()) : splitRangeInHalf(xRange);
            List<Range<Float>> ySubRanges = query != null ? splitRangeByQuery(yRange, query.getYRange()) : splitRangeInHalf(yRange);

            subTiles = new QuerySplitTile[ySubRanges.size()][xSubRanges.size()];

            for (int i = 0; i < ySubRanges.size(); i++) {
                Range<Float> ySubRange = ySubRanges.get(i);
                for (int j = 0; j < xSubRanges.size(); j++) {
                    Range<Float> xSubRange = xSubRanges.get(j);
                    subTiles[i][j] = new QuerySplitTile(new Rectangle(xSubRange, ySubRange), threshold, tilePointListFactory);
                    //LOG.debug("Subtile " + i + ", " + j + ": " + subTiles[i][j].bounds);
                }
            }

            for (Point point : this.tilePointList.getPointList()) {
                this.addPoint(point);
            }
            this.tilePointList.destroy();
            this.tilePointList = null;
            //LOG.debug("Split tile into " + ySubRanges.size() + " rows and " + xSubRanges.size() + " columns");
        } catch (IllegalArgumentException e) {
            LOG.error(query);
            LOG.error(this.bounds);
            throw e;
        }
    }


    @Override
    public Tile addPoint(Point point) {
        if (this.subTiles == null) {
            this.tilePointList.addPoint(point);
            return this;
        } else {
            int i, j;
            for (j = 0; j < subTiles[0].length; ++j) {
                if (subTiles[0][j].bounds.getXRange().contains(point.getX())) {
                    break;
                }
            }
            for (i = 0; i < subTiles.length; ++i) {
                if (subTiles[i][0].bounds.getYRange().contains(point.getY())) {
                    break;
                }
            }
            return subTiles[i][j].addPoint(point);
        }
    }

    private BoundType reverseBoundType(BoundType boundType) {
        switch (boundType) {
            case OPEN:
                return BoundType.CLOSED;
            case CLOSED:
                return BoundType.OPEN;
            default:
                return null;

        }
    }

    @Override
    public int getLeafTileCount() {
        if (subTiles == null) {
            return 1;
        }
        int count = 0;
        for (Tile[] tileRow : subTiles) {
            for (Tile tile : tileRow) {
                count += tile.getLeafTileCount();
            }
        }
        return count;
    }

    @Override
    public int getMaxDepth() {
        if (subTiles == null) {
            return 0;
        }
        int depth = 0;
        for (Tile[] tileRow : subTiles) {
            for (Tile subTile : tileRow) {
                depth = Integer.max(depth, subTile.getMaxDepth() + 1);
            }
        }
        return depth;
    }

}