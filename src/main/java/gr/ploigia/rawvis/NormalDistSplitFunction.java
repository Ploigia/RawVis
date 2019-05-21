package gr.ploigia.rawvis;

import com.google.common.collect.Range;
import org.apache.commons.math3.distribution.NormalDistribution;

public class NormalDistSplitFunction implements InitialSplitFunction {


    private NormalDistribution distributionX;
    private NormalDistribution distributionY;
    private int noOfSubtiles;


    public NormalDistSplitFunction(Rectangle query, int noOfSubTiles) {
        double queryCenterX = (query.getXRange().lowerEndpoint() + query.getXRange().upperEndpoint()) / 2d;
        double queryCenterY = (query.getYRange().lowerEndpoint() + query.getYRange().upperEndpoint()) / 2d;
        double queryXSize = query.getXRange().upperEndpoint() - query.getXRange().lowerEndpoint();
        double queryYSize = query.getYRange().upperEndpoint() - query.getYRange().lowerEndpoint();
        this.distributionX = new NormalDistribution(queryCenterX, queryXSize);
        this.distributionY = new NormalDistribution(queryCenterY, queryYSize);

        // noOfSubtiles is the number of tile distributed based on the prob.
        this.noOfSubtiles = noOfSubTiles;
    }

    @Override
    public int value(Range<Float> xRange, Range<Float> yRange) {
        int splitSize = (int) Math.floor(Math.sqrt(noOfSubtiles * distributionX.probability(xRange.lowerEndpoint(), xRange.upperEndpoint()) * distributionY.probability(yRange.lowerEndpoint(), yRange.upperEndpoint())));
        return splitSize;
    }

    @Override
    public int value(double dist) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int value(int dist) {
        throw new UnsupportedOperationException();
    }

}
