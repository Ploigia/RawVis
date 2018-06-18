package gr.ploigia.rawvis.grid;

import gr.ploigia.rawvis.query.AbstractPointIterator;
import gr.ploigia.rawvis.util.ContainmentExaminer;

public class TileIterator extends AbstractPointIterator {

    private GridTile tile;
    private ContainmentExaminer containmentExaminer;
    private int i = -1;


    public TileIterator(GridTile tile, ContainmentExaminer containmentExaminer) {
        this.tile = tile;
        this.containmentExaminer = containmentExaminer;
    }

    protected Point getNext() {
        try {
            if (containmentExaminer == null){
                return tile.getPoints().get(++i);
            }
            Point point;
            while (!containmentExaminer.contains(point = tile.getPoints().get(++i))) ;
            return point;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public GridTile getTile() {
        return tile;
    }

    public boolean isFullyContained() {
        return containmentExaminer == null;
    }
}