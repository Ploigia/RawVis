package gr.ploigia.rawvis.grid.multilevel.quadtree;

import gr.ploigia.rawvis.common.Rectangle;
import gr.ploigia.rawvis.grid.DataGrid;
import gr.ploigia.rawvis.grid.GridTile;

public class QuadTreeGrid extends DataGrid {


    public QuadTreeGrid(Rectangle bounds, int rowCount, int colCount, int threshold) {
        super(bounds, rowCount, colCount, threshold);
    }

    @Override
    public GridTile createTile(Rectangle bounds) {
        return new QuadTreeTile(bounds, threshold);
    }

}
