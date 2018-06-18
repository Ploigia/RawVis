package gr.ploigia.rawvis.grid.multilevel.kdtree;

import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.grid.DataGrid;
import gr.ploigia.rawvis.grid.GridTile;

public class KDTreeGrid extends DataGrid {

    public KDTreeGrid(Rectangle bounds, int rowCount, int colCount, int threshold) {
        super(bounds, rowCount, colCount, threshold);
    }

    @Override
    public GridTile createTile(Rectangle bounds) {
        return new KDTreeTile(bounds, threshold);
    }
}