package gr.ploigia.rawvis;

import java.util.List;

public interface TilePointList {

    List<Point> getPointList();

    void addPoint(Point point);

    void destroy();
}