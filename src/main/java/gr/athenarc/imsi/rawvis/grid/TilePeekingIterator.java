package gr.athenarc.imsi.rawvis.grid;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

public class TilePeekingIterator implements PeekingIterator<Point> {

    private PeekingIterator<Point> peekingIterator;
    private GridTile tile;
    private boolean isFullyContained;

    public TilePeekingIterator(TileIterator tileIterator) {
        peekingIterator = Iterators.peekingIterator(tileIterator);
        tile = tileIterator.getTile();
        isFullyContained = tileIterator.isFullyContained();
    }


    @Override
    public Point peek() {
        return peekingIterator.peek();
    }

    @Override
    public boolean hasNext() {
        return peekingIterator.hasNext();
    }

    @Override
    public Point next() {
        return peekingIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public GridTile getTile() {
        return tile;
    }

    public boolean isFullyContained() {
        return isFullyContained;
    }
}
