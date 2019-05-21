package gr.ploigia.rawvis;

import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;


public class KWayMergePointIterator extends AbstractPointIterator {

    private Tile currentTile;
    private boolean isFullyContained;
    private PriorityQueue<TilePeekingIterator> tilesPQueue;

    public KWayMergePointIterator(List<TileIterator> tileIterators) {
        Comparator<TilePeekingIterator> comparator = new Ordering<TilePeekingIterator>() {
            @Override
            public int compare(TilePeekingIterator i1, TilePeekingIterator i2) {
                return Long.compare(i1.peek().getFileOffset(), i2.peek().getFileOffset());
            }
        };
        tilesPQueue = new PriorityQueue<>(tileIterators.size() > 0 ? tileIterators.size() : 1, comparator);
        for (TileIterator tileEntryIterator : tileIterators) {
            if (tileEntryIterator.hasNext()) {
                tilesPQueue.add(new TilePeekingIterator(tileEntryIterator));
            }
        }
    }

    protected Point getNext() {
        try {
            TilePeekingIterator tileIterator = tilesPQueue.remove();
            Point point = tileIterator.next();
            currentTile = tileIterator.getTile();
            isFullyContained = tileIterator.isFullyContained();

            if (tileIterator.hasNext()) {
                tilesPQueue.add(tileIterator);
            }
            return point;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Tile getCurrentTile() {
        return currentTile;
    }

    public boolean isFullyContained() {
        return isFullyContained;
    }
}
