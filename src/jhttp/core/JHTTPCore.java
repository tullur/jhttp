package jhttp.core;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JHTTPCore {

    private static final Logger logger = Logger.getLogger(JHTTPCore.class.getCanonicalName());

    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";

    private final File rootDir;
    private final int port;

    public JHTTPCore(File rootDir, int port) throws IOException {
        if (!rootDir.isDirectory())
            throw new IOException(rootDir + "ain't exist");

        this.rootDir = rootDir;
        this.port = port;
    }

    public void start() throws IOException{
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket server = new ServerSocket(port)){
            logger.info("Accepting connection on port: " + server.getLocalPort());
            logger.info("Document root: " + rootDir);

            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new RequestProcessor(rootDir, INDEX_FILE, request);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }
}
