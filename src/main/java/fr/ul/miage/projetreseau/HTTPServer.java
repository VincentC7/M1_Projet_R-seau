package fr.ul.miage.projetreseau;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.JFrame;

public class HTTPServer extends Thread {

	// Répertoire racine du projet
	private final File root;
	// Object effectant le lien avec notre serveur java et le navigateur, il attend
	// les requetes et envois les infos au navigateur quand il faut
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
				Socket socket = serverSocket.accept();
				// Buffer permettant d'écrire les données pour ensuite les envoyer au navigateur
				BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

				// Si l'utilisateur et le mdp sont corrects
				if (accesPage(out)) {
					// On accède à la page
					System.out.println("OK");
				}

				HTTPRequest requestThread = new HTTPRequest(socket, root, serverSocket.getInetAddress().getHostName());
				requestThread.start();

			} catch (IOException e) {
				System.exit(1);
			}
		}
	}

	public boolean accesPage(BufferedOutputStream out) throws IOException {

		// Teste si la ressource est protegee
		File htpsswdFile = new File(root, ".htpasswd");
		if (htpsswdFile.isFile()) {

			// Ouvre une fenetre de connexion
			System.out.println("ouverture fenêtre : " + out.toString());
			ConnexionInterface connexion = new ConnexionInterface();
			JFrame connexionInterface = connexion.connexion(htpsswdFile);
			// Attend la reponse de l'utilisateur
			while (connexionInterface.isVisible()) {
				// tant que l'utilisateur n'a pas validé
			}
			System.out.println("Réponse validée");
			// Récupere la réponse de l'utilisateur et refuse la connexion
			// si le login ou le mdp ne sont pas bons
			if (!connexion.isEnable()) {
				System.out.println("Permission Denied : " + connexion.isEnable());
				HTTPRequest.sendError(out, 403, "Permission Denied.");
				return false;
			}
			return true;
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

	/*
	 * public static void main(String[] args) { try { new HTTPServer(new
	 * File("Ressources_exte/site1"), 8090, "www.licorne.choquert"); new
	 * HTTPServer(new File("Ressources_exte/site2"), 8091,
	 * "www.testserv1.choquert"); } catch (IOException e) { System.out.println(e); }
	 * }
	 */
}
