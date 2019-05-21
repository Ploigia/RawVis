package gr.ploigia.rawvis;


public class QuadTreeTileCreator implements TileCreator {

    private int threshold;

    public QuadTreeTileCreator(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Tile createTile(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        return new QuadTreeTile(bounds, threshold, tilePointListFactory);
    }
}