package gr.athenarc.imsi.rawvis.common;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.List;


public class Rectangle {

    private final Range<Float> xRange;
    private final Range<Float> yRange;

    public Rectangle(Range<Float> xRange, Range<Float> yRange) {
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public Range<Float> getXRange() {
        return xRange;
    }

    public Range<Float> getYRange() {
        return yRange;
    }

    public boolean contains(float x, float y) {
        return xRange.contains(x) && yRange.contains(y);
    }

    public boolean intersects(Rectangle other) {
        return this.xRange.isConnected(other.getXRange()) && this.yRange.isConnected(other.getYRange());
    }

    public boolean encloses(Rectangle other) {
        return this.xRange.encloses(other.getXRange()) && this.yRange.encloses(other.getYRange());
    }
    public List toList() {
        List<Range<Float>> list = new ArrayList<>(2);
        list.add(this.xRange);
        list.add(this.yRange);
        return list;
    }


    @Override
    public String toString() {
        //return xRange.lowerEndpoint() + ":" + xRange.upperEndpoint() + "," + yRange.lowerEndpoint() + ":" + yRange.upperEndpoint();
        return xRange.toString() + "," + yRange.toString();
    }
}
