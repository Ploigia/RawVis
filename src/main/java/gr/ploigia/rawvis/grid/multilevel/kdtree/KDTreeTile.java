package gr.ploigia.rawvis.grid.multilevel.kdtree;


import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.grid.GridTile;
import gr.ploigia.rawvis.grid.Point;

import java.util.ArrayList;
import java.util.List;

public class KDTreeTile extends GridTile {

    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;

    private KDTreeTile left, right;
    private int depth;

    public KDTreeTile(Rectangle bounds, int threshold) {
        super(bounds, threshold);
        this.depth = 0;
    }

    public KDTreeTile(Rectangle bounds, int depth, int threshold) {
        super(bounds, threshold);
        this.depth = depth;
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
                if (query.intersects(this.left.bounds))
                    leafTiles.add(this.left);
                if (query.intersects(this.right.bounds))
                    leafTiles.add(this.right);

            } else {
                leafTiles.add(this);
            }
        } else {
            //todo oxi etsi
            if (query.intersects(this.left.bounds))
                this.left.getLeafTiles(query, leafTiles, aggCol);
            if (query.intersects(this.right.bounds))
                this.right.getLeafTiles(query, leafTiles, aggCol);
        }
        return leafTiles;
    }

    /**
     * If this is a leaf node, it is converted to a non-leaf node and its points are reinserted into the correct child.
     */
    public void split() {
        int axis = depth % 2;
        Range<Float> range;
        float median;
        if (axis == X_AXIS) {
            range = this.bounds.getXRange();
        } else {
            range = this.bounds.getYRange();
        }
        median = (range.upperEndpoint() + range.lowerEndpoint()) / 2f;
        Range rangeLeft = Range.range(range.lowerEndpoint(), range.lowerBoundType(),
                median, BoundType.CLOSED);
        Range rangeRight = Range.range(median, BoundType.OPEN, range.upperEndpoint(), range.upperBoundType());

        if (axis == X_AXIS) {
            this.left = new KDTreeTile(new Rectangle(rangeLeft, this.bounds.getYRange()), depth + 1);
            this.right = new KDTreeTile(new Rectangle(rangeRight, this.bounds.getYRange()), depth + 1);
        } else {
            this.left = new KDTreeTile(new Rectangle(this.bounds.getXRange(), rangeLeft), depth + 1);
            this.right = new KDTreeTile(new Rectangle(this.bounds.getXRange(), rangeRight), depth + 1);
        }

        for (Point point : this.points) {
            float value = axis == X_AXIS ? point.getX() : point.getY();
            if (value <= median) {
                this.left.addPoint(point);
            } else {
                this.right.addPoint(point);
            }
        }
        this.points = null;
    }
}
