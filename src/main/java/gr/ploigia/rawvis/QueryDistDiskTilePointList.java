package gr.ploigia.rawvis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class QueryDistDiskTilePointList implements TilePointList {

    private static final Logger LOG = LogManager.getLogger(QueryDistDiskTilePointList.class);


    List<Point> pointList;
    private String path;
    private int totalCount;
    private int inDiskCount;
    private int inMemoryCount;

    private Rectangle tileBounds;


    QueryDistDiskTilePointList(Rectangle tileBounds) {
        this.path = QueryDistTileEvictionManager.getInstance().getTmpDir() + "/" + Instant.now().toEpochMilli() + tileBounds;
        this.tileBounds = tileBounds;
        pointList = new ArrayList<>();
        totalCount = 0;
        inDiskCount = 0;
        inMemoryCount = 0;
        QueryDistTileEvictionManager.getInstance().add(this);

    }

    @Override
    public List<Point> getPointList() {
        if (inMemoryCount < totalCount) {

            if (pointList != null) {
                QueryDistTileEvictionManager.getInstance().remove(this);
            }

            List<Point> nonPersistedPoints = pointList;

            pointList = new ArrayList<>();
            if (inDiskCount > 0) {
                try {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.path)));
                    while (in.available() > 0) {
                        float x = in.readFloat();
                        float y = in.readFloat();
                        long offset = in.readLong();
                        pointList.add(new Point(x, y, offset));
                        inMemoryCount++;
                    }
                    in.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (nonPersistedPoints != null) {
                pointList.addAll(nonPersistedPoints);
            }
            QueryDistTileEvictionManager.getInstance().add(this);
        }
        return pointList;
    }

    @Override
    public void addPoint(Point point) {
        if (pointList == null) {
            pointList = new ArrayList<>();
            QueryDistTileEvictionManager.getInstance().add(this);
        }
        pointList.add(point);
        inMemoryCount++;
        totalCount++;
        QueryDistTileEvictionManager.getInstance().pointAdded();
    }

    void evict() {
        if (totalCount > inDiskCount) {
            //LOG.debug("Evicting non-persisted tile");
            try {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.path, true)));
                for (int i = totalCount == inMemoryCount ? inDiskCount : 0; i < pointList.size(); i++) {
                    Point point = pointList.get(i);
                    writePoint(out, point);
                }
                out.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        pointList = null;
        inDiskCount = totalCount;
        inMemoryCount = 0;
    }

    @Override
    public void destroy() {
        if (pointList != null) {
            pointList = null;
            QueryDistTileEvictionManager.getInstance().remove(this);
        }
    }

    private void writePoint(DataOutputStream out, Point point) throws IOException {
        out.writeFloat(point.getX());
        out.writeFloat(point.getY());
        out.writeLong(point.getFileOffset());
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getInDiskCount() {
        return inDiskCount;
    }

    public int getInMemoryCount() {
        return inMemoryCount;
    }

    public Rectangle getTileBounds() {
        return tileBounds;
    }

}