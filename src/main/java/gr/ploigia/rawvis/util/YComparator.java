package gr.ploigia.rawvis.util;

import gr.ploigia.rawvis.grid.Point;

import java.util.Comparator;

public class YComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getY() < o2.getY())
            return -1;
        if (o1.getY() > o2.getY())
            return 1;
        return 0;
    }
}