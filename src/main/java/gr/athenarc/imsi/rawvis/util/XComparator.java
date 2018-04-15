package gr.athenarc.imsi.rawvis.util;

import gr.athenarc.imsi.rawvis.grid.Point;

import java.util.Comparator;

public class XComparator implements Comparator<Point> {

    @Override
    public int compare(Point o1, Point o2) {
        if (o1.getX() < o2.getX())
            return -1;
        if (o1.getX() > o2.getX())
            return 1;
        return 0;
    }
}