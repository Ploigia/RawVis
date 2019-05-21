package gr.ploigia.rawvis;

import gr.ploigia.rawvis.util.ContainmentExaminer;


public class TileIterator extends AbstractPointIterator {

    private Tile tile;
    private ContainmentExaminer containmentExaminer;
    private int i = -1;


    public TileIterator(Tile tile, ContainmentExaminer containmentExaminer) {
        this.tile = tile;
        this.containmentExaminer = containmentExaminer;
    }

    protected Point getNext() {
        try {
            if (containmentExaminer == null) {
                return tile.tilePointList.getPointList().get(++i);
            }
            Point point;
            while (!containmentExaminer.contains(point = tile.tilePointList.getPointList().get(++i))) ;
            return point;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isFullyContained() {
        return containmentExaminer == null;
    }
}