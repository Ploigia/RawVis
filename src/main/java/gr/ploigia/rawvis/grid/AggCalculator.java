package gr.ploigia.rawvis.grid;

import gr.ploigia.rawvis.common.Stats;

public class AggCalculator implements DataPointProcessor {

    private float sum = 0;
    private int count = 0;
    private float min;
    private float max;

    public void process(Point point, float attrValue) {
        if (count == 0) {
            min = attrValue;
            max = attrValue;
        } else {
            min = Math.min(attrValue, min);
            max = Math.max(attrValue, max);
        }
        count++;
        sum += attrValue;
    }

    @Override
    public void process(Point point, String[] attrs) {
        float attrValue = Float.parseFloat(attrs[0]);
        this.process(point, attrValue);
    }

    public void processStats(Stats stats) {
        if (count == 0) {
            min = stats.getMin();
            max = stats.getMax();
        } else {
            min = Math.min(stats.getMin(), min);
            max = Math.max(stats.getMax(), max);
        }
        sum += stats.getSum();
        count += stats.getCount();
    }

    public Stats getStats() {
        return new Stats(count, sum, min, max);
    }


    public float getSum() {
        return sum;
    }

    public int getCount() {
        return count;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

}
