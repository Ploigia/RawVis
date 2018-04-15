package gr.athenarc.imsi.rawvis.query;

import gr.athenarc.imsi.rawvis.common.Rectangle;
import gr.athenarc.imsi.rawvis.common.Stats;

public class QueryResults {

    private Rectangle query;

    private Stats stats;

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

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
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
