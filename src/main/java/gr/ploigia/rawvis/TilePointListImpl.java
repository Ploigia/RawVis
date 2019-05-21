package gr.ploigia.rawvis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TilePointListImpl implements TilePointList {

    private List<Point> points;

    public TilePointListImpl() {
        points = new ArrayList<>();
    }

    @Override
    public List<Point> getPointList() {
        return points;
    }

    @Override
    public void addPoint(Point point) {
        points.add(point);
    }

    @Override
    public void destroy() {
        points = null;
    }

}