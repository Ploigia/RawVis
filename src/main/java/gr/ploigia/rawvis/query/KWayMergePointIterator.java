package gr.ploigia.rawvis.query;

import com.google.common.collect.Ordering;
import gr.ploigia.rawvis.grid.TileIterator;
import gr.ploigia.rawvis.grid.GridTile;
import gr.ploigia.rawvis.grid.Point;
import gr.ploigia.rawvis.grid.TilePeekingIterator;

import java.util.*;


public class KWayMergePointIterator extends AbstractPointIterator {

    private GridTile currentTile;
    private boolean isFullyContained;
    private PriorityQueue<TilePeekingIterator> tilesPQueue;

    public KWayMergePointIterator(List<TileIterator> tileIterators) {
        Comparator<TilePeekingIterator> comparator = new Ordering<TilePeekingIterator>() {
            @Override
            public int compare(TilePeekingIterator i1, TilePeekingIterator i2) {
                return Long.compare(i1.peek().getFileOffset(), i2.peek().getFileOffset());
            }
        };
        tilesPQueue = new PriorityQueue<>(tileIterators.size(), comparator);
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

    public GridTile getCurrentTile() {
        return currentTile;
    }

    public boolean isFullyContained() {
        return isFullyContained;
    }
}
