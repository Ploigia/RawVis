package gr.ploigia.rawvis;

public interface TileCreator {

    Tile createTile(Rectangle bounds, TilePointListFactory tilePointListFactory);

}