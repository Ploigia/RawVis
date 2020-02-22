package gr.ploigia.rawvis;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class QuadTreeTile extends Tile {

    private static final Logger LOG = LogManager.getLogger(QuadTreeTile.class);

    private QuadTreeTile topLeft, topRight, bottomLeft, bottomRight;
    private int threshold;

    public QuadTreeTile(Rectangle bounds, int threshold, TilePointListFactory tilePointListFactory) {
        super(bounds, tilePointListFactory);
        this.threshold = threshold;
    }

    @Override
    public List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol0, Integer aggCol1) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }

        if (this.tilePointList != null) {
            boolean isFullyContained = query.encloses(this.bounds);
            if (this.tilePointList.getPointList().size() > threshold && (!isFullyContained || !this.hasStats(aggCol0, aggCol1))) {
                this.split();
                if (query.intersects(this.topRight.bounds))
                    leafTiles.add(topRight);
                if (query.intersects(this.bottomRight.bounds))
                    leafTiles.add(bottomRight);
                if (query.intersects(this.bottomLeft.bounds))
                    leafTiles.add(bottomLeft);
                if (query.intersects(this.topLeft.bounds))
                    leafTiles.add(topLeft);
            } else {
                leafTiles.add(this);
            }
        } else {
            //todo oxi etsi
            if (query.intersects(this.topRight.bounds))
                this.topRight.getLeafTiles(query, leafTiles, aggCol0, aggCol1);
            if (query.intersects(this.bottomRight.bounds))
                this.bottomRight.getLeafTiles(query, leafTiles, aggCol0, aggCol1);
            if (query.intersects(this.bottomLeft.bounds))
                this.bottomLeft.getLeafTiles(query, leafTiles, aggCol0, aggCol1);
            if (query.intersects(this.topLeft.bounds))
                this.topLeft.getLeafTiles(query, leafTiles, aggCol0, aggCol1);
        }
        return leafTiles;
    }

    private void split() {
        //LOG.debug("Splitting tile " + bounds + " with " + this.tilePointList.getPointList().size() + " objects");

        Range<Float> xRange = this.bounds.getXRange();
        float xMiddle = (xRange.upperEndpoint() + xRange.lowerEndpoint()) / 2f;
        Range<Float> yRange = this.bounds.getYRange();
        float yMiddle = (yRange.upperEndpoint() + yRange.lowerEndpoint()) / 2f;
        Range rangeLeft = Range.range(xRange.lowerEndpoint(), xRange.lowerBoundType(),
                xMiddle, BoundType.CLOSED);
        Range rangeRight = Range.range(xMiddle, BoundType.OPEN, xRange.upperEndpoint(), xRange.upperBoundType());
        Range rangeBottom = Range.range(yRange.lowerEndpoint(), yRange.lowerBoundType(),
                yMiddle, BoundType.CLOSED);
        Range rangeTop = Range.range(yMiddle, BoundType.OPEN, yRange.upperEndpoint(), yRange.upperBoundType());
        this.topLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeTop), threshold, tilePointListFactory);
        this.topRight = new QuadTreeTile(new Rectangle(rangeRight, rangeTop), threshold, tilePointListFactory);
        this.bottomLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeBottom), threshold, tilePointListFactory);
        this.bottomRight = new QuadTreeTile(new Rectangle(rangeRight, rangeBottom), threshold, tilePointListFactory);

        for (Point point : this.tilePointList.getPointList()) {
            this.addPoint(point);
        }
        this.tilePointList.destroy();
        this.tilePointList = null;
    }

    @Override
    public Tile addPoint(Point point) {
        if (this.topLeft == null) {
            this.tilePointList.addPoint(point);
            return this;
        } else {
            boolean left = point.getX() <= this.bottomLeft.bounds.getXRange().upperEndpoint();
            boolean bottom = point.getY() <= this.bottomLeft.bounds.getYRange().upperEndpoint();
            QuadTreeTile tmp;
            if (left) {
                if (bottom) {
                    tmp = this.bottomLeft;
                } else {
                    tmp = this.topLeft;
                }
            } else {
                if (bottom) {
                    tmp = this.bottomRight;
                } else {
                    tmp = this.topRight;
                }
            }
            return tmp.addPoint(point);
        }
    }

    @Override
    public int getLeafTileCount() {
        if (topLeft == null) {
            return 1;
        }
        return topLeft.getLeafTileCount() + topRight.getLeafTileCount()
                + bottomLeft.getLeafTileCount() + bottomRight.getLeafTileCount();
    }

    @Override
    public int getMaxDepth() {
        if (topLeft == null) {
            return 0;
        }
        int depth = 0;
        depth = Integer.max(depth, topLeft.getMaxDepth() + 1);
        depth = Integer.max(depth, topRight.getMaxDepth() + 1);
        depth = Integer.max(depth, bottomLeft.getMaxDepth() + 1);
        depth = Integer.max(depth, bottomRight.getMaxDepth() + 1);
        return depth;
    }
}