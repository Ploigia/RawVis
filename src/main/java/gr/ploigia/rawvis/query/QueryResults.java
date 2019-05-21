package gr.ploigia.rawvis.query;

import gr.ploigia.rawvis.Rectangle;

public class QueryResults {

    private Rectangle query;

    private Object stats;

    private int fullyContainedTileCount;

    private int rawAccessTileCount;

    private int tileCount;

    private int rawRowsRead;

    public Rectangle getQuery() {
        return query;
    }

    public void setQuery(Rectangle query) {
        this.query = query;
    }

    public Object getStats() {
        return stats;
    }

    public void setStats(Object stats) {
        this.stats = stats;
    }

    public int getFullyContainedTileCount() {
        return fullyContainedTileCount;
    }

    public void setFullyContainedTileCount(int fullyContainedTileCount) {
        this.fullyContainedTileCount = fullyContainedTileCount;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public int getRawRowsRead() {
        return rawRowsRead;
    }

    public void setRawRowsRead(int rawRowsRead) {
        this.rawRowsRead = rawRowsRead;
    }

    public int getRawAccessTileCount() {
        return rawAccessTileCount;
    }

    public void setRawAccessTileCount(int rawAccessTileCount) {
        this.rawAccessTileCount = rawAccessTileCount;
    }

    @Override
    public String toString() {
        return "QueryResults{" +
                "query=" + query +
                ", stats=" + stats +
                ", fullyContainedTileCount=" + fullyContainedTileCount +
                ", rawAccessTileCount=" + rawAccessTileCount +
                ", tileCount=" + tileCount +
                ", rawRowsRead=" + rawRowsRead +
                '}';
    }
}
