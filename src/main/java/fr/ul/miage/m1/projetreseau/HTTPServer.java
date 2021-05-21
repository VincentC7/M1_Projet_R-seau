package fr.ul.miage.m1.projetreseau;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class HTTPServer extends Thread {

	// Répertoire racine du projet
	private final File root;
	// Objet effectuant le lien entre notre serveur (java) et le navigateur, il attend
	// les requetes et envoie les infos au navigateur quand il faut
	private final ServerSocket serverSocket;

	// Nom du serveur
	public static final String VERSION = "NotreSereurHTTP";

	// MIME TYPES reconnus par notre serveur
	public static final Hashtable<String, String> MIME_TYPES = new Hashtable<>();

	// Ajouts des différents MIME_TYPES reconnus par notre serveur
	static {
		MIME_TYPES.put(".gif", "images/gif");
		MIME_TYPES.put(".jpg", "images/jpeg");
		MIME_TYPES.put(".jpeg", "images/jpeg");
		MIME_TYPES.put(".png", "images/png");
		MIME_TYPES.put(".html", "text/html");
		MIME_TYPES.put(".htm", "text/html");
		MIME_TYPES.put(".txt", "text/plain");
		MIME_TYPES.put(".css", "text/css");
		MIME_TYPES.put(".js", "text/javascript");
		MIME_TYPES.put(".woff", "font/woff");
		MIME_TYPES.put(".woff2", "font/woff2");
		MIME_TYPES.put(".ttf", "font/ttf");
		MIME_TYPES.put(".ttf2", "font/ttf2");
		MIME_TYPES.put(".ico", "image/x-icon");
	}

	/**
	 * Constructeur
	 * 
	 * @param rootDir Repertoire racine du site qu'on doit lancer
	 * @param port    Port d'écoute
	 * @param domaine nom de domaine du serveur
	 * @throws IOException
	 */
	public HTTPServer(File rootDir, int port, String domaine) throws IOException {
		root = rootDir.getCanonicalFile();
		if (!root.isDirectory())
			throw new IOException("Le fichier spécifié n'est pas un répertoire");
		InetAddress address = InetAddress.getByName(domaine);
		serverSocket = new ServerSocket(port, 50, address);
		System.out.println("Lancement du serveur :" + domaine + " sur le port :" + port);
		start();
	}

	/**
	 * Lance le serveur dans un thread
	 */
	public void run() {
		while (true) {
			try {
				// Ecoute une connexion à etablir avec ce socket et s'il y en a une, l'accepte
				Socket socket = serverSocket.accept();

				// Lecture d'une requête envoyée par le navigateur (client) au serveur, analyse
				// et réponse : envoi d'une requête serveur vers navigateur (client)
				HTTPRequest requestThread = new HTTPRequest(socket, root,serverSocket.getInetAddress().getHostName());
				requestThread.start();

			} catch (IOException e) {
				System.exit(1);
			}
		}
	}

	/**
	 * Fonction qui vérifie si un fichier est compressé via son nom
	 * 
	 * @param file fichier où on souhaite savoir s'il ets compressé
	 * @return true s'il est compressé, false sinon
	 */
	public static boolean estCompresse(java.io.File file) {

		String filename = file.getName();

		String[] filenameParts = filename.split(".");
		for (int i = 0; i < filenameParts.length; i++) {
			if (filenameParts[i].equals("min")) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Fonction qui détermine l'extension d'un fichier
	 * 
	 * @param file fichier où on souhaite récuperer l'extension
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

}
