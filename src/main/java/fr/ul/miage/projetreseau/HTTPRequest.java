package fr.ul.miage.projetreseau;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import javax.swing.JFrame;

public class HTTPRequest extends Thread {

	// Fichier racine du site
	private final File root;
	// Object communiquant avec le serversocket afin d'envoyer/recevoir les infos du
	// navigateur
	private final Socket socket;
	// Nom de domaine du site : "www.exemple.fr"
	private final String domaine;

	/**
	 * Constructeur
	 * 
	 * @param socket  pacerelle de communication entre notre serveur et le
	 *                navigateur
	 * @param rootDir repertoire racine du projet
	 * @param domaine
	 */
	public HTTPRequest(Socket socket, File rootDir, String domaine) {
		this.socket = socket;
		root = rootDir;
		this.domaine = domaine;
	}

	/**
	 * Methode permettant d'envoyer l'entêtes d'une ressource (html, css, js, images
	 * ...)
	 * 
	 * @param out           Object qui permet d'écrire les données et les envoie au
	 *                      navigateur
	 * @param code          d'envoie (200 = tout ce passe bien, 400 = erreur)
	 * @param contentType   type de ressource envoyé (js, html ...)
	 * @param contentLength taille de la ressource
	 * @param lastModified  date de modification de la ressource
	 * @throws IOException cas ou les données ne peuvent pas être écrite dans le
	 *                     buffer
	 */
	private void sendHeader(BufferedOutputStream out, int code, String contentType, long contentLength,
			long lastModified) throws IOException {
		//System.out.println("Send    to   " + domaine + " [" + new Date() + "]" + " HTTP/1.0 GET, code :" + code + ", "
				//+ contentType + ", contentLength:" + contentLength);
		String head = "HTTP/1.0 " + code + " OK\r\n" + "Date: " + new Date() + "\r\n" + "Content-Type: " + contentType
				+ "\r\n" + ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "")
				+ "Last-modified: " + new Date(lastModified) + "\r\n" + "\r\n";
		out.write(head.getBytes());
	}

	/**
	 * Méthode permettant d'envoyer des erreurs au navigateur (bad request, not
	 * found ...)
	 * 
	 * @param out     => buffer d'écriture pour écrire l'erreur
	 * @param code    => code erreur (400,404,...)
	 * @param message => message d'erreur
	 * @throws IOException cas ou le message n'a pas pu etre écrit
	 */
	private void sendError(BufferedOutputStream out, int code, String message) throws IOException {
		sendHeader(out, code, "text/html", message.length(), System.currentTimeMillis());
		out.write(message.getBytes());
		out.flush();
		out.close();
	}

	/**
	 * Méthode volumineuse qui s'occupe de gérer la requete
	 */
	public void run() {
		InputStream reader = null;
		try {
			socket.setSoTimeout(30000);
			// Buffer permettant de lire les données envoyées par le navigateur
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Buffer permettant d'écrire les données pour ensuite les envoyer au navigateur
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

			// Recupération de la requete
			String request = in.readLine();
			//System.out.println("Request from " + domaine + " [" + new Date() + "] " + request);

			// Vérifiacation de l'entête de la requete (si c'est pas un GET, ça dégage)
			if (request == null || !request.startsWith("GET ")
					|| !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
				sendError(out, 500, "Invalid Method.");
				return;
			}

			String path = request.substring(4, request.length() - 9);
			File file = new File(root, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();
			
			// Test si la ressource est protegee
			File htpsswdFile = new File(root, ".htpasswd");
			if (htpsswdFile.isFile()) {
				
				// Ouvre une fenetre de connexion
				ConnexionInterface connexion = new ConnexionInterface();
				JFrame connexionInterface = connexion.connexion(htpsswdFile);
				// Attend la reponse de l'utilisateur
				while (connexionInterface.isVisible()) {
				}
				// Récupere la réponse de l'utilisateur et refuse la connexion
				// si le login ou le mdp ne sont pas bons
				if (!connexion.isEnable()) {
					sendError(out, 403, "Permission Denied.");
				}
			}

			if (file.isDirectory()) {
				// Récupération du fichier index.html
				File indexFile = new File(file, "index.html");
				if (indexFile.exists() && !indexFile.isDirectory())
					file = indexFile;
			}

			if (!file.toString().startsWith(root.toString())) {
				// Là on pourrait afficher les fichiers du serveur (option du projet)
				sendError(out, 403, "Permission Denied.");
			}

			// File note found
			else if (!file.exists()) {
				sendError(out, 404, "File Not Found.");
			}

			// Chargement des assets et images
			else {
				reader = new BufferedInputStream(new FileInputStream(file));
				// Récupération du bon content type a partir de l'extention du fichier
				String extension = HTTPServer.getExtension(file);
				String contentType = HTTPServer.MIME_TYPES.get(HTTPServer.getExtension(file));
				// Cas où on a un content type non supporté par le projet
				if (contentType == null) {
					sendError(out, 415, "Content-type inconnu");
				}

				sendHeader(out, 200, contentType, file.length(), file.lastModified());

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = reader.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				reader.close();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e1) {
				}
			}
		}
	}

}
