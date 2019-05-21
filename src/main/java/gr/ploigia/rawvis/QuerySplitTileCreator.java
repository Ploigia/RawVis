package gr.ploigia.rawvis;


public class QuerySplitTileCreator implements TileCreator {

    private int threshold;

    public QuerySplitTileCreator(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Tile createTile(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        return new QuerySplitTile(bounds, threshold, tilePointListFactory);
    }
}