package gr.ploigia.rawvis;

import com.google.common.collect.Range;

public interface InitialSplitFunction {
    int value(double dist);

    int value(int dist);

    int value(Range<Float> xRange, Range<Float> yRange);

}
