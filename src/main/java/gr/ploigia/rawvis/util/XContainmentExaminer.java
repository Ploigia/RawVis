package gr.ploigia.rawvis.util;


import com.google.common.collect.Range;
import gr.ploigia.rawvis.Point;

public class XContainmentExaminer implements ContainmentExaminer {

    private Range<Float> xRange;

    public XContainmentExaminer(Range<Float> xRange) {
        this.xRange = xRange;
    }

    @Override
    public boolean contains(Point point) {
        return xRange.contains(point.getX());
    }
}
