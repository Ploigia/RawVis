package gr.athenarc.imsi.rawvis.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class CustomCsvProcessor extends CsvProcessor {


    //    reader inputstreamreader  bufferedinputstream

    private static int defaultExpectedTokenLength = 80;
    private static int maxColumns = 100;


    private char delimiter = ',';
    private boolean withHeader = false;
    private Integer[] selectedIndexes;
    private char[] valueBuffer = new char[defaultExpectedTokenLength];
    private int valueSize = 0;

    private CharReader charReader;

    private String[] parsedValues = new String[maxColumns];

    public CustomCsvProcessor(Reader reader) {
        super(reader);
        this.charReader = new CharReader(reader, 1024 * 1024);
        this.charReader.start();
    }

    @Override
    public void selectIndexes(Integer... fieldIndexes) {
        Arrays.sort(fieldIndexes);
        this.selectedIndexes = fieldIndexes;
    }

    @Override
    public long currentRowOffset() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getWithHeader() {
        return this.withHeader;
    }

    @Override
    public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }


    @Override
    public void close() throws IOException {
        charReader.close();
    }


    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            String[] current;

            private String[] getNext() {
                return CustomCsvProcessor.this.parseNext();
            }

            @Override
            public boolean hasNext() {
                if (this.current == null) {
                    this.current = this.getNext();
                }
                return this.current != null;
            }

            @Override
            public String[] next() {
                String[] next = this.current;
                this.current = null;

                if (next == null) {
                    // hasNext() wasn't called before
                    next = this.getNext();
                    if (next == null) {
                        throw new NoSuchElementException("No more CSV records available");
                    }
                }
                return next;
            }
        };
    }

    public String[] parseNext() {
        int col = 0;
        char ch;
        int r;

        for (; ; ) {
            try {
                ch = charReader.nextChar();
                //r = reader.read();
            } /*catch (IOException e) {
                throw new IllegalStateException("Error reading from input", e);
            }*/ catch (java.io.EOFException e) {
                parsedValues[col++] = new String(valueBuffer, 0, valueSize);
                valueSize = 0;
                if (col > 1 || !parsedValues[0].isEmpty()) {
                    String[] out = new String[col];
                    System.arraycopy(parsedValues, 0, out, 0, col);
                    return out;
                } else {
                    return null;
                }
            }
           /* if (r < 0) {  // EOF

            }*/
            //ch = (char) r;
            if (ch == '\n') {
                parsedValues[col++] = new String(valueBuffer, 0, valueSize);
                valueSize = 0;
                String[] out = new String[col];
                System.arraycopy(parsedValues, 0, out, 0, col);
                return out;
            } else if (ch == delimiter) {
                parsedValues[col++] = new String(valueBuffer, 0, valueSize);
                valueSize = 0;
            } else {
                valueBuffer[valueSize++] = ch;
            }
        }


/*        if (eol) {
            String str;
            if (s == null) {
                str = new String(cb, startChar, i - startChar);
            } else {
                s.append(cb, startChar, i - startChar);
                str = s.toString();
            }
            nextChar++;
            if (c == '\r') {
                skipLF = true;
            }
            return str;
        }*/


    }

}




