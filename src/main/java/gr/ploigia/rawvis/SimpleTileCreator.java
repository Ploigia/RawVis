package gr.ploigia.rawvis;

public class SimpleTileCreator implements TileCreator {
    @Override
    public Tile createTile(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        return new SimpleTile(bounds, tilePointListFactory);
    }
}