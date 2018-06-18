package gr.ploigia.rawvis.csv;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class CharReader {


    private Reader reader;

    /**
     * Current position in the buffer
     */
    public int i;

    /**
     * The buffer itself
     */
    public char[] buffer;

    /**
     * Number of characters available in the buffer.
     */
    public int length = -1;

    private long charCount;


    public CharReader(Reader reader, int bufferSize) {
        this.reader = reader;
        this.buffer = new char[bufferSize];
    }


    public final void start() {
        updateBuffer();
        if (length > 0) {
            i++;
        }
    }


    public final char nextChar() throws EOFException {
        if (length == -1) {
            throw new EOFException();
        }

        char ch = buffer[i - 1];

        if (i >= length) {
            updateBuffer();
        }

        i++;
        return ch;
    }

    public long charCount() {
        return charCount + i;
    }

    public void close(){
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error closing input", e);
        }
    }

    /**
     * Requests the next batch of characters and updates
     * the character count.
     *
     * <p> If there are no more characters in the input, the reading will stop by invoking the close() method.
     */
    private void updateBuffer() {
        reloadBuffer();
        charCount += i;
        i = 0;
        if (length == -1) {
            close();
        }
    }


    /**
     * Copies a sequence of characters from the input into the buffer, and updates the length to the number of characters read.
     */
    public void reloadBuffer() {
        try {
            this.length = reader.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading from input", e);
        }
    }

}
