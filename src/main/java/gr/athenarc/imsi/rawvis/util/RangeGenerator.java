package gr.athenarc.imsi.rawvis.util;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that generates sql range queries on float columns with uniformly distributed values over the given min, max range
 */
public class RangeGenerator {

    private Range<Float> valueRange;

    public RangeGenerator(Range<Float> valueRange) {
        this.valueRange = valueRange;
    }

    public Range<Float>[] getEqualSizedRanges(int rangeCount, float totalSelectivity) {
        Range<Float>[] ranges = new Range[rangeCount];
        float rangeSize = (float) ((valueRange.upperEndpoint() - valueRange.lowerEndpoint()) * Math.pow(totalSelectivity, 1.0 / rangeCount));
        float totalMax = valueRange.upperEndpoint() - rangeSize;
        for (int i = 0; i < rangeCount; i++) {
            float rangeMin = (float) ThreadLocalRandom.current().nextDouble(valueRange.lowerEndpoint(), totalMax);
            float rangeMax = rangeMin + rangeSize;
            ranges[i] = Range.open(rangeMin, rangeMax);
        }
        return ranges;
    }

    public Range<Float> getValueRange() {
        return valueRange;
    }

    public void setValueRange(Range<Float> valueRange) {
        this.valueRange = valueRange;
    }
}
