package gr.athenarc.imsi.rawvis.grid.multilevel.kdtree;

import gr.athenarc.imsi.rawvis.common.Rectangle;
import gr.athenarc.imsi.rawvis.grid.DataGrid;
import gr.athenarc.imsi.rawvis.grid.GridTile;

public class KDTreeGrid extends DataGrid {

    public KDTreeGrid(Rectangle bounds, int rowCount, int colCount, int threshold) {
        super(bounds, rowCount, colCount, threshold);
    }

    @Override
    public GridTile createTile(Rectangle bounds) {
        return new KDTreeTile(bounds, threshold);
    }
}