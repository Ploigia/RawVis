package gr.ploigia.rawvis;

import java.util.ArrayList;
import java.util.List;

public class SimpleTile extends Tile {


    public SimpleTile(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        super(bounds, tilePointListFactory);
    }

    @Override
    public Tile addPoint(Point point) {
        tilePointList.addPoint(point);
        return this;
    }

    @Override
    public int getLeafTileCount() {
        return 1;
    }


    @Override
    public List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol0, Integer aggCol1) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }
        leafTiles.add(this);
        return leafTiles;
    }

    public int getMaxDepth() {
        return 0;
    }


}
