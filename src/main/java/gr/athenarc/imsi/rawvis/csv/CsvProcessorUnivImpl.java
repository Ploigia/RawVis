package gr.athenarc.imsi.rawvis.csv;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import gr.athenarc.imsi.rawvis.config.Configuration;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CsvProcessorUnivImpl extends CsvProcessor {

    private CsvParserSettings parserSettings;
    private CsvParser parser;
    private long currentRowOffset;

    public CsvProcessorUnivImpl() {
        this(null);
    }

    public CsvProcessorUnivImpl(Reader reader) {
        super(reader);
        this.parserSettings = new CsvParserSettings();
        // does not skip leading whitespaces
        this.parserSettings.setIgnoreLeadingWhitespaces(false);
        // does not skip trailing whitespaces
        this.parserSettings.setIgnoreTrailingWhitespaces(false);
        String delimiter = Configuration.getDelimiter();
        if (delimiter != null && !delimiter.isEmpty()) {
            this.setDelimiter(delimiter.charAt(0));
        }
    }

    @Override
    public void close() throws IOException {
        if (this.parser != null) {
            this.parser.stopParsing();
        }
    }


    @Override
    public Iterator<String[]> iterator() {
        if (this.reader == null) {
            throw new IllegalStateException("Reader has not been instantiated.");
        }
        this.parser = new CsvParser(parserSettings);
        this.parser.beginParsing(this.reader);
        return new Iterator<String[]>() {
            String[] current;
            long tmpOffset;

            private String[] getNext() {
                return CsvProcessorUnivImpl.this.parser.parseNext();
            }

            @Override
            public boolean hasNext() {
                if (this.current == null) {
                    this.tmpOffset = CsvProcessorUnivImpl.this.parser.getContext().currentChar() - 1;
                    this.current = this.getNext();
                }
                return this.current != null;
            }

            @Override
            public String[] next() {
                String[] next = this.current;
                CsvProcessorUnivImpl.this.currentRowOffset = this.tmpOffset;
                this.current = null;

                if (next == null) {
                    // hasNext() wasn't called before
                    CsvProcessorUnivImpl.this.currentRowOffset = CsvProcessorUnivImpl.this.parser.getContext().currentChar() - 1;
                    next = this.getNext();
                    if (next == null) {
                        throw new NoSuchElementException("No more CSV records available");
                    }
                }
                return next;
            }

        };
    }

    @Override
    public void selectIndexes(Integer... fieldIndexes) {
        this.parserSettings.selectIndexes(fieldIndexes);
    }


    @Override
    public long currentRowOffset() {
        return currentRowOffset;
    }

    @Override
    public char getDelimiter() {
        return this.parserSettings.getFormat().getDelimiter();
    }

    @Override
    public void setDelimiter(char delimiter) {
        this.parserSettings.getFormat().setDelimiter(delimiter);
    }

    @Override
    public String[] parseLine(String line) {
        if (this.parser == null) {
            this.parser = new CsvParser(parserSettings);
        }
        return this.parser.parseLine(line);
    }

    @Override
    public boolean getWithHeader() {
        return this.parserSettings.isHeaderExtractionEnabled();
    }

    @Override
    public void setWithHeader(boolean withHeader) {
        this.parserSettings.setHeaderExtractionEnabled(withHeader);
    }

}
