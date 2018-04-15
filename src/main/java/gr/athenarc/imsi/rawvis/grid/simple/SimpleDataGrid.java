package gr.athenarc.imsi.rawvis.grid.simple;

import gr.athenarc.imsi.rawvis.common.Rectangle;
import gr.athenarc.imsi.rawvis.grid.DataGrid;
import gr.athenarc.imsi.rawvis.grid.GridTile;


public class SimpleDataGrid extends DataGrid {


    public SimpleDataGrid(Rectangle bounds, int rowCount, int colCount, int threshold) {
        super(bounds, rowCount, colCount, threshold);
    }

    @Override
    public GridTile createTile(Rectangle bounds) {
        return new SimpleGridTile(bounds, threshold);
    }
}
