package gr.athenarc.imsi.rawvis.csv;

import java.io.Closeable;
import java.io.Reader;


public abstract class CsvProcessor implements Iterable<String[]>, Closeable {
    Reader reader;

    boolean readInputOnSeparateThread;

    public CsvProcessor() {
    }


    public CsvProcessor(Reader reader) {
        this.reader = reader;
    }

    /**
     * Selects a sequence of fields for reading by their positions.
     *
     * @param fieldIndexes The indexes to read
     */
    public abstract void selectIndexes(Integer... fieldIndexes);

    /**
     * Returns the index of the last char read from the input so far.
     *
     * @return the index of the last char read from the input so far.
     */
    public abstract long currentRowOffset();

    public abstract char getDelimiter();

    public abstract void setDelimiter(char delimiter);

    public abstract String[] parseLine(String line);

    public abstract boolean getWithHeader();

    public abstract void setWithHeader(boolean withHeader);

}
