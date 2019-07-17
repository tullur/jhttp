package jhttp.main;

import jhttp.core.JHTTPCore;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JHTTPMain {

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(JHTTPMain.class.getCanonicalName());

        File docroot;
        try {
            docroot = new File("src/jhttp/main");
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java http docroot port");
            System.out.println(ex);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535)
                port = 80;
        } catch (RuntimeException ex) {
            port = 80;
        }

        try {
            JHTTPCore webserver = new JHTTPCore(docroot, port);
            webserver.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}
