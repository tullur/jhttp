package jhttp.core;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
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
        String root = rootDir.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(
                    new BufferedInputStream(connection.getInputStream()), "ASCII"
            );
            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c == '\r' || c == '\n' || c == -1)
                    break;
                requestLine.append((char) c);
            }

            String get = requestLine.toString();
            logger.info(connection.getRemoteSocketAddress() + " " + get);

            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if (method.equals("GET")) {
                String fileName = tokens[1];

                if (fileName.endsWith("/"))
                    fileName += indexFile;

                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);

                if (tokens.length > 2)
                    version = tokens[2];

                File file = new File(rootDir, fileName.substring(1, fileName.length()));

                if (file.canRead() && file.getCanonicalPath().startsWith(root)) {
                    byte[] data = Files.readAllBytes(file.toPath());
                    if (version.startsWith("HTTP/"))
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, data.length);

                    raw.write(data);
                    raw.flush();
                } else {
                    String body = new StringBuilder("<html>\r\n")
                            .append("<head><title>Not Found</title>\r\n")
                            .append("</head>\r\n")
                            .append("<body>")
                            .append("<h1>HTTP Error 501</h1>\r\n")
                            .append("</body> </html>").toString();
                    if (version.startsWith("HTTP/"))
                        sendHeader(out, "HTTP/1.0 501 Not Implemented", "text/html; charset=utf-8", body.length());

                    out.write(body);
                    out.flush();
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error -> " + connection.getRemoteSocketAddress(), ex);
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {}
        }
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
