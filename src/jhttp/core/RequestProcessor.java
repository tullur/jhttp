package jhttp.core;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {

    private static final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    private final File rootDir;
    private String indexFile = "index.html";
    private Socket connection;

    public RequestProcessor(File rootDir, String indexFile, Socket connection) {
        if (!rootDir.isDirectory())
            throw new IllegalArgumentException("Root dir must be a dir, not a file");

        try {
            rootDir = rootDir.getCanonicalFile();
        } catch (IOException ex) {}

        if (indexFile != null)
            this.indexFile = indexFile;

        this.rootDir = rootDir;
        this.connection = connection;
    }

    @Override
    public void run() {

    }

    private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
        out.write(responseCode + "\r\n");
        Date date = new Date();
        out.write("Date: " + date + "\r\n");
        out.write("Server jhttp 2.0 \r\n");
        out.write("Content-Length: " + length + "\r\n");
        out.write("Content-Type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}
