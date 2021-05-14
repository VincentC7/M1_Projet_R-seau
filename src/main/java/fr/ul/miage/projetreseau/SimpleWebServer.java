package fr.ul.miage.projetreseau;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class SimpleWebServer extends Thread {

    private final File root;
    private final ServerSocket serverSocket;

    public static final String VERSION = "HIHI";
    public static final Hashtable MIME_TYPES = new Hashtable();

    static {
        String image = "images/";
        MIME_TYPES.put(".gif", image + "gif");
        MIME_TYPES.put(".jpg", image + "jpeg");
        MIME_TYPES.put(".jpeg", image + "jpeg");
        MIME_TYPES.put(".png", image + "png");

        String text = "text/";
        MIME_TYPES.put(".html", text + "html");
        MIME_TYPES.put(".htm", text + "html");
        MIME_TYPES.put(".txt", text + "plain");
        MIME_TYPES.put(".css", text + "css");
        MIME_TYPES.put(".js", text + "javascript");
    }

    public SimpleWebServer(File rootDir, int port, String domaine) throws IOException {
        root = rootDir.getCanonicalFile();
        if (!root.isDirectory()) throw new IOException("Le fichier spécifié n'est pas un répertoire");
        InetAddress address = InetAddress.getByName(domaine);
        serverSocket = new ServerSocket(port,50,address);
        start();
    }


    public void run() {
        while (true){
            try {
                Socket socket = serverSocket.accept();
                HTTPRequest requestThread = new HTTPRequest(socket, root,serverSocket.getInetAddress().getHostName());
                requestThread.start();
            } catch (IOException e) {
                System.exit(1);
            }
        }
    }


    // Work out the filename extension.  If there isn't one, we keep
    // it as the empty string ("").
    public static String getExtension(java.io.File file) {
        String extension = "";
        String filename = file.getName();
        int dotPos = filename.lastIndexOf(".");
        if (dotPos >= 0) {
            extension = filename.substring(dotPos);

        }
        return extension.toLowerCase();
    }


    public static void main(String[] args) {
        try {
            new SimpleWebServer(new File("Ressources_exte/site1"), 8090, "www.licorne.choquert");
            new SimpleWebServer(new File("Ressources_exte/site2"), 8091, "www.testserv1.choquert");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
