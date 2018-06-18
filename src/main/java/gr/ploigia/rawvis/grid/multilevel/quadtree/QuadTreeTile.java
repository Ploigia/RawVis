package gr.ploigia.rawvis.grid.multilevel.quadtree;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.grid.Point;
import gr.ploigia.rawvis.grid.GridTile;

import java.util.ArrayList;
import java.util.List;

public class QuadTreeTile extends GridTile {

    private QuadTreeTile topLeft, topRight, bottomLeft, bottomRight;


    public QuadTreeTile(Rectangle bounds, int threshold) {
        super(bounds, threshold);
    }

    @Override
    public List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }
        if (this.points != null) {
            boolean isFullyContained = query.encloses(this.bounds);
            if (this.points.size() > threshold && (!isFullyContained || this.getStats(aggCol) == null)) {
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
                this.topRight.getLeafTiles(query, leafTiles, aggCol);
            if (query.intersects(this.bottomRight.bounds))
                this.bottomRight.getLeafTiles(query, leafTiles, aggCol);
            if (query.intersects(this.bottomLeft.bounds))
                this.bottomLeft.getLeafTiles(query, leafTiles, aggCol);
            if (query.intersects(this.topLeft.bounds))
                this.topLeft.getLeafTiles(query, leafTiles, aggCol);
        }
        return leafTiles;
    }

    /**
     * If this is a leaf node, it is converted to a non-leaf node and its points are reinserted into the correct child.
     */
    public void split() {
        Range<Float> xRange = this.bounds.getXRange();
        float xMiddle = (xRange.upperEndpoint() + xRange.lowerEndpoint()) / 2f;
        Range<Float> yRange = this.bounds.getYRange();
        float yMiddle = (yRange.upperEndpoint() + yRange.lowerEndpoint()) / 2f;
        Range rangeLeft = Range.range(xRange.lowerEndpoint(), xRange.lowerBoundType(),
                xMiddle, BoundType.CLOSED);
        Range rangeRight = Range.range(xMiddle, BoundType.OPEN, xRange.upperEndpoint(), xRange.upperBoundType());
        Range rangeTop = Range.range(yRange.lowerEndpoint(), yRange.lowerBoundType(),
                yMiddle, BoundType.CLOSED);
        Range rangeBottom = Range.range(yMiddle, BoundType.OPEN, yRange.upperEndpoint(), yRange.upperBoundType());
        this.topLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeTop), threshold);
        this.topRight = new QuadTreeTile(new Rectangle(rangeRight, rangeTop), threshold);
        this.bottomLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeBottom), threshold);
        this.bottomRight = new QuadTreeTile(new Rectangle(rangeRight, rangeBottom), threshold);
        for (Point point : this.points) {
            boolean left = point.getX() <= xMiddle;
            boolean top = point.getY() <= yMiddle;
            QuadTreeTile tmp;
            if (left) {
                if (top) {
                    tmp = this.topLeft;
                } else {
                    tmp = this.bottomLeft;
                }
            } else {
                if (top) {
                    tmp = this.topRight;
                } else {
                    tmp = this.bottomRight;
                }
            }
            tmp.addPoint(point);
        }
        this.points = null;
    }
}