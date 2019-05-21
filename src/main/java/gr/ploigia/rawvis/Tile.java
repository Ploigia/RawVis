package gr.ploigia.rawvis;

import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.StatsAccumulator;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Tile {

    protected Rectangle bounds;
    protected TilePointListFactory tilePointListFactory;
    protected TilePointList tilePointList;
    private Map<Integer, StatsAccumulator> statsMap;
    private Map<ImmutablePair<Integer, Integer>, PairedStatsAccumulator> pairedStatsMap;


    public Tile(Rectangle bounds, TilePointListFactory tilePointListFactory) {
        this.bounds = bounds;
        this.tilePointListFactory = tilePointListFactory;
        tilePointList = tilePointListFactory.createTilePointsList(this.bounds);
    }

    public abstract Tile addPoint(Point point);


    public Rectangle getBounds() {
        return bounds;
    }

    public abstract List<Tile> getLeafTiles(Rectangle query, List<Tile> leafTiles, Integer aggCol0, Integer aggCol1);


    void adjustStats(int col, float value) {
        StatsAccumulator statsAccumulator = getStats(col);
        if (statsAccumulator == null) {
            statsAccumulator = this.initStats(col);
        }
        statsAccumulator.add(value);
    }

    void adjustPairedStats(int aggCol0, int aggCol1, float value0, float value1) {
        PairedStatsAccumulator pairedStatsAcc = getPairedStats(aggCol0, aggCol1);
        if (pairedStatsAcc == null) {
            pairedStatsAcc = this.initPairedStats(aggCol0, aggCol1);
        }
        pairedStatsAcc.add(value0, value1);
    }

    public StatsAccumulator getStats(int col) {
        if (statsMap == null) {
            return null;
        }
        return statsMap.get(col);
    }

    public PairedStatsAccumulator getPairedStats(int aggCol0, int aggCol1) {
        if (pairedStatsMap == null) {
            return null;
        }
        return pairedStatsMap.get(new ImmutablePair(aggCol0, aggCol1));
    }

    public boolean hasStats(Integer aggCol0, Integer aggCol1) {
        if (aggCol0 == null) {
            return false;
        } else if (aggCol1 == null) {
            return this.getStats(aggCol0) != null;
        } else {
            return this.getPairedStats(aggCol0, aggCol1) != null;
        }
    }

    private StatsAccumulator initStats(int col) {
        StatsAccumulator statsAccumulator = new StatsAccumulator();
        if (this.statsMap == null) {
            this.statsMap = new HashMap<>();
        }
        this.statsMap.put(col, statsAccumulator);
        return statsAccumulator;
    }

    private PairedStatsAccumulator initPairedStats(int aggCol0, int aggCol1) {
        PairedStatsAccumulator pairedStatsAccumulator = new PairedStatsAccumulator();
        if (this.pairedStatsMap == null) {
            this.pairedStatsMap = new HashMap<>();
        }
        this.pairedStatsMap.put(new ImmutablePair(aggCol0, aggCol1), pairedStatsAccumulator);
        return pairedStatsAccumulator;
    }

    @Override
    public String toString() {
        return "{" + this.bounds.toString() + " , " + (tilePointList != null ? tilePointList.getPointList().size() : null) + "}";
    }

    public abstract int getLeafTileCount();

}