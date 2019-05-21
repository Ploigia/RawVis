package gr.ploigia.rawvis.util.csv;

import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CsvProcessorUnivImpl extends CsvProcessor {

    private AbstractParser parser;
    private long currentRowOffset;
    private boolean withHeader = false;
    private char delimiter = ',';
    private Integer[] fieldIndexes = null;

    public CsvProcessorUnivImpl() {
        this(null);
    }

    public CsvProcessorUnivImpl(Reader reader) {
        super(reader);
    }

    @Override
    public void close() {
        if (this.parser != null) {
            this.parser.stopParsing();
        }
    }


    @Override
    public Iterator<String[]> iterator() {
        if (this.reader == null) {
            throw new IllegalStateException("Reader has not been instantiated.");
        }



        this.parser = getUnivParser();
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
        this.fieldIndexes = fieldIndexes;
    }


    @Override
    public long currentRowOffset() {
        return currentRowOffset;
    }

    @Override
    public char getDelimiter() {
        return this.delimiter;
    }

    @Override
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String[] parseLine(String line) {
        if (this.parser == null) {
            this.parser = this.getUnivParser();
        }
        return this.parser.parseLine(line);
    }

    @Override
    public boolean getWithHeader() {
        return this.withHeader;
    }

    @Override
    public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }


    private AbstractParser getUnivParser(){
        if (delimiter == '\t'){
            TsvParserSettings parserSettings = new TsvParserSettings();
            parserSettings.setIgnoreLeadingWhitespaces(false);
            parserSettings.setIgnoreTrailingWhitespaces(false);
            parserSettings.setHeaderExtractionEnabled(withHeader);
            parserSettings.selectIndexes(fieldIndexes);
            return new TsvParser(parserSettings);

        } else {
            CsvParserSettings parserSettings = new CsvParserSettings();
            parserSettings.setIgnoreLeadingWhitespaces(false);
            parserSettings.setIgnoreTrailingWhitespaces(false);
            parserSettings.setHeaderExtractionEnabled(withHeader);
            parserSettings.getFormat().setDelimiter(delimiter);
            parserSettings.selectIndexes(fieldIndexes);
            return new CsvParser(parserSettings);
        }
    }


}
