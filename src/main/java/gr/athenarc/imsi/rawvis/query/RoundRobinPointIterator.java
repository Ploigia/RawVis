package gr.athenarc.imsi.rawvis.query;

import com.google.common.collect.Iterators;
import gr.athenarc.imsi.rawvis.grid.TileIterator;
import gr.athenarc.imsi.rawvis.grid.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class RoundRobinPointIterator extends AbstractPointIterator {

    private Iterator<TileIterator> tilesIt;


    public RoundRobinPointIterator(List<TileIterator> tileIterators) {
        //use LinkedList since it.remove() on linked list is O(1)  (???)
        LinkedList<TileIterator> itList = new LinkedList<>();
        for (TileIterator it : tileIterators){
            itList.add(it);
        }
        this.tilesIt = Iterators.cycle(itList);
    }

    protected Point getNext() {
        TileIterator tileEntryIterator;
        while (this.tilesIt.hasNext()) {
            tileEntryIterator = this.tilesIt.next();
            if (tileEntryIterator.hasNext()) {
                return tileEntryIterator.next();
            } else {
                this.tilesIt.remove();
            }
        }
        return null;
    }


}
