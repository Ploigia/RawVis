package gr.ploigia.rawvis.grid.simple;

import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.grid.GridTile;

import java.util.ArrayList;
import java.util.List;

public class SimpleGridTile extends GridTile {


    /**
     * Constructs a data tile with the specified bounds.
     *
     * @param bounds
     * @param threshold
     */
    public SimpleGridTile(Rectangle bounds, int threshold) {
        super(bounds, threshold);
    }

    public List getLeafTiles(Rectangle query, List leafTiles, Integer aggCol) {
        if (leafTiles == null) {
            leafTiles = new ArrayList();
        }
        leafTiles.add(this);
        return leafTiles;
    }

    @Override
    public void split() {
        throw new UnsupportedOperationException();
    }
}
