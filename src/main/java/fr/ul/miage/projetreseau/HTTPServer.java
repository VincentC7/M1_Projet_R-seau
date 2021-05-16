package fr.ul.miage.projetreseau;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class HTTPServer extends Thread {

    //Répertoire racine du projet
    private final File root;
    //Object effectant le lien avec notre serveur java et le navigateur, il attend les requetes et envois les infos au navigateur quand il faut
    private final ServerSocket serverSocket;

    //Nom du serveur
    public static final String VERSION = "NotreSereurHTTP";

    //MIME TYPES reconnus par notre serveur
    public static final Hashtable<String, String> MIME_TYPES = new Hashtable<>();

    //Ajouts des différents MIME_TYPES reconnus par notre serveur
    static {
        MIME_TYPES.put(".gif", "images/gif");
        MIME_TYPES.put(".jpg","images/jpeg");
        MIME_TYPES.put(".jpeg", "images/jpeg");
        MIME_TYPES.put(".png", "images/png");
        MIME_TYPES.put(".html", "text/html");
        MIME_TYPES.put(".htm", "text/html");
        MIME_TYPES.put(".txt", "text/plain");
        MIME_TYPES.put(".css", "text/css");
        MIME_TYPES.put(".js", "text/javascript");
    }

    /**
     * Constructeur
     * @param rootDir Repertoire racine du site qu'on doit lancer
     * @param port Port d'écoute
     * @param domaine nom de domaine du serveur
     * @throws IOException
     */
    public HTTPServer(File rootDir, int port, String domaine) throws IOException {
        root = rootDir.getCanonicalFile();
        if (!root.isDirectory()) throw new IOException("Le fichier spécifié n'est pas un répertoire");
        InetAddress address = InetAddress.getByName(domaine);
        serverSocket = new ServerSocket(port,50,address);
        System.out.println("Lancement du serveur :"+domaine+" sur le port :"+port);
        start();
    }


    /**
     * Lance le serveur dans un thread
     */
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


    /**
     * Fonction qui détermine l'extension d'un fichier
     * @param file fichier  où on souhaite récuperer l'extension
     * @return extension du fichier '.html' | '.css' ...
     */
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
            new HTTPServer(new File("Ressources_exte/site1"), 8090, "www.licorne.choquert");
            new HTTPServer(new File("Ressources_exte/site2"), 8091, "www.testserv1.choquert");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
