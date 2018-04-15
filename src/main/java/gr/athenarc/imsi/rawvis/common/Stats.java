package gr.athenarc.imsi.rawvis.common;

public class Stats {

    private float sum = 0;
    private int count = 0;
    private float min;
    private float max;


    public Stats() {
    }

    public Stats(int count, float sum, float min, float max) {
        this.count = count;
        this.sum = sum;
        this.min = min;
        this.max = max;
    }

    public void adjust(float value) {
        if (count == 0) {
            min = value;
            max = value;
        } else {
            min = Math.min(value, min);
            max = Math.max(value, max);
        }
        count++;
        sum += value;
    }

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getAvg() {
        return sum / count;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "sum=" + sum +
                ", count=" + count +
                ", min=" + min +
                ", max=" + max +
                ", avg=" + getAvg() +
                '}';
    }
}
