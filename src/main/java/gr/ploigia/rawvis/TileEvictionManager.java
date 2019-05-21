package gr.ploigia.rawvis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class TileEvictionManager implements TilePointListFactory {

    private static TileEvictionManager instance;
    LinkedList<DiskTilePointList> lruList = new LinkedList<>();
    private int maxSize;
    private int inMemoryCount = 0;
    private Path tmpDir;

    private TileEvictionManager(int maxSize, String cachePath) throws IOException {
        this.maxSize = maxSize;
        tmpDir = Files.createTempDirectory(Paths.get(cachePath), "valinor");
    }

    public static TileEvictionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TileEvictionManager has not been initialized");
        } else {
            return instance;
        }
    }

    public static TileEvictionManager init(int maxSize, String cachePath) throws IOException {
        if (instance != null) {
            throw new IllegalStateException("TileEvictionManager has already been initialized");
        }

        instance = new TileEvictionManager(maxSize, cachePath);

        return instance;
    }


    public void add(DiskTilePointList diskTilePointList) {
        lruList.addFirst(diskTilePointList);
        inMemoryCount += diskTilePointList.getInMemoryCount();
        maintainSize();
    }

    public void remove(DiskTilePointList diskTilePointList) {
        lruList.remove(diskTilePointList);
        inMemoryCount -= diskTilePointList.getInMemoryCount();
    }

    private void maintainSize() {
        while (inMemoryCount > maxSize) {
            DiskTilePointList diskTilePointList = lruList.removeLast();
            inMemoryCount -= diskTilePointList.getInMemoryCount();
            diskTilePointList.evict();
        }
    }

    public void pointAdded(DiskTilePointList diskTilePointList) {
        lruList.remove(diskTilePointList);
        lruList.addFirst(diskTilePointList);
        inMemoryCount++;
        maintainSize();
    }

    public String getTmpDir() {
        return tmpDir.toString();
    }

    @Override
    public TilePointList createTilePointsList(Rectangle bounds) {
        return new DiskTilePointList(bounds);
    }


}
