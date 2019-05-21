package gr.ploigia.rawvis;

public class TilePointListFactoryImpl implements TilePointListFactory {


    @Override
    public TilePointList createTilePointsList(Rectangle bounds) {
        return new TilePointListImpl();
    }
}