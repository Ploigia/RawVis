package gr.ploigia.rawvis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QueryDistTileEvictionManager implements TilePointListFactory {

    private static final Logger LOG = LogManager.getLogger(QueryDistTileEvictionManager.class);
    private static QueryDistTileEvictionManager instance;
    private int maxSize;
    private int inMemoryCount = 0;
    private List<QueryDistDiskTilePointList> list;
    private Rectangle firstQuery;
    private Path tmpDir;
    private Comparator<QueryDistDiskTilePointList> queryDistComparator;


    private QueryDistTileEvictionManager(int maxSize, String cachePath, Rectangle firstQuery) throws IOException {
        this.maxSize = maxSize;
        this.firstQuery = firstQuery;
        tmpDir = Files.createTempDirectory(Paths.get(cachePath), "valinor");
        list = new ArrayList<>();
        queryDistComparator
                = Comparator.comparingDouble(diskTilePointList -> diskTilePointList.getTileBounds().distanceFrom(firstQuery));
    }

    public static QueryDistTileEvictionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TileEvictionManager has not been initialized");
        } else {
            return instance;
        }
    }

    public static QueryDistTileEvictionManager init(int maxSize, String cachePath, Rectangle firstQuery) throws IOException {
        if (instance != null) {
            throw new IllegalStateException("TileEvictionManager has already been initialized");
        }
        return instance = new QueryDistTileEvictionManager(maxSize, cachePath, firstQuery);
    }

    public void add(QueryDistDiskTilePointList diskTilePointList) {
        list.add(diskTilePointList);
        inMemoryCount = inMemoryCount + diskTilePointList.getInMemoryCount();
        maintainSize();
    }

    public void remove(QueryDistDiskTilePointList diskTilePointList) {
        list.remove(diskTilePointList);
        inMemoryCount = inMemoryCount - diskTilePointList.getInMemoryCount();
    }

    public void pointAdded() {
        inMemoryCount++;
        maintainSize();
    }

    private void maintainSize() {
        while (inMemoryCount > maxSize) {
            QueryDistDiskTilePointList diskTilePointList = list.stream().filter(tilePointList -> tilePointList.getInMemoryCount() > 1000)
                    .max(queryDistComparator).orElseThrow(() -> new IllegalStateException("problem maintaining size"));
            inMemoryCount = inMemoryCount - diskTilePointList.getInMemoryCount();
            list.remove(diskTilePointList);
            diskTilePointList.evict();
        }
    }

    public String getTmpDir() {
        return tmpDir.toString();
    }

    @Override
    public TilePointList createTilePointsList(Rectangle bounds) {
        return new QueryDistDiskTilePointList(bounds);
    }

    public int getNumberOfTilesManaged() {
        return list.size();
    }
}
