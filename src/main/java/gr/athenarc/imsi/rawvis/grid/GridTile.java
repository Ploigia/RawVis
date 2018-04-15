package gr.athenarc.imsi.rawvis.grid;

import gr.athenarc.imsi.rawvis.common.Rectangle;
import gr.athenarc.imsi.rawvis.common.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GridTile {

    protected Rectangle bounds;
    protected List<Point> points;
    protected Map<Integer, Stats> statsMap;
    protected int threshold;
    public static int errors = 0;

    /**
     * Constructs a data tile with the specified bounds.
     *
     * @param bounds
     */
    public GridTile(Rectangle bounds, int threshold) {
        this.bounds = bounds;
        this.threshold = threshold;
        points = new ArrayList<>();
        statsMap = new HashMap<>();
    }

    public List<Point> getPoints() {
        return points;
    }

    public void addPoint(Point point) {
        /*if (!this.bounds.getXRange().contains(point.getX()) || !this.bounds.getYRange().contains(point.getY())){
            //System.out.println("" + this.bounds + " point " + point.getX() + " ," + point.getY());
            errors++;
        }*/
        if (point != null) {
            points.add(point);
        }
    }

    public int getPointsCount() {
        return points.size();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public abstract List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol);

    public abstract void split();


    public void adjustStats(int column, float value) {
        Stats stats = getStats(column);
        stats.adjust(value);
    }

    @Override
    public String toString() {
        return this.getBounds().getXRange().toString() + this.getBounds().getYRange().toString() + "\t";
    }

    public Stats getStats(Integer col) {
        return statsMap.get(col);
    }

    public void initStats(int col) {
        this.statsMap.put(col, new Stats());
    }

    public int getThreshold() {
        return threshold;
    }
}
