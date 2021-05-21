package fr.ul.miage.m1.projetreseau;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

public class HTTPRequest extends Thread {

	// Fichier racine du site
	private final File root;
	// Object communiquant avec le serversocket afin d'envoyer/recevoir les infos du
	// navigateur
	private final Socket socket;
	//Nom de domaine du site : "www.exemple.fr"
    private final String domaine;

	/**
	 * Constructeur
	 * 
	 * @param socket  passerelle de communication entre notre serveur et le
	 *                navigateur (client)
	 * @param rootDir repertoire racine du projet
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
	private static void sendHeader(BufferedOutputStream out, int code, String contentType, long contentLength,
			long lastModified, File file, String domaine) throws IOException {
        System.out.println("Send    to   "+domaine+" ["+new Date()+"]"+" HTTP/1.0 GET, code :"+code+", "+contentType+", contentLength:"+contentLength);
		String head = null;
		switch (code) {
		case 200:
			// OK
			head = "HTTP/1.0 " + code + " OK\r\n" + "Date: " + new Date() + "\r\n" + "Content-Type: " + contentType
					+ "\r\n" + ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "")
					+ "Last-modified: " + new Date(lastModified) + "\r\n";
			// compression utilisée : gzip

			if (HTTPServer.estCompresse(file)) {
				head += "Content-Encoding: gzip \r\n";
			}
			head += "\r\n";
			break;

		case 401:
			// Unauthorized
			head = "HTTP/1.0 " + code + " Unauthorized\r\n" + "Date: " + new Date() + "\r\n" + "Content-Type: "
					+ contentType + "\r\n" + ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "")
					+ "Last-modified: " + new Date(lastModified) + "\r\n"
					+ "WWW-Authenticate: Basic realm=\"Access to the staging site\"\r\n";
			break;

		case 403:
			// Forbidden
			head = "HTTP/1.0 " + code + " Forbidden\r\n" + "Date: " + new Date() + "\r\n";
			break;

		default: // 400
			// Bad Request
			head = "HTTP/1.0 " + 400 + " Bad Request\r\n" + "Date: " + new Date() + "\r\n";
			break;
		}
		try {
			out.write(head.getBytes());
		} catch (Exception e) {
			// e.printStackTrace();
		}

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
		sendHeader(out, code, "text/html", message.length(), System.currentTimeMillis(), null, domaine);
		out.write(message.getBytes());
		out.flush();
		out.close();
	}

	/**
	 * Teste si le répertoire est protégé
	 * 
	 * @throws IOException cas ou le fichier n'existe pas
	 * @return true si le repertoire contient un fichier htpsswd, false sinon
	 */
	public boolean repertoireProtege() {

		try {
			// Teste si la ressource est protegee
			File htpsswdFile = new File(root, ".htpasswd");
			if (htpsswdFile.isFile()) {
				return true;
			}
		} catch (Exception e) {
			// Si le fichier n'existe pas
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Méthode volumineuse qui s'occupe de gérer une requete
	 */
	public void run() {
		InputStream reader = null;
		try {
			// socket.setSoTimeout(30000);
			// Buffer permettant de lire les données envoyés par le navigateur
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Buffer permettant d'écrire les données pour ensuite les envoyer au navigateur
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			boolean enable = true; // Par défaut non protégé

			// Recupération de la première ligne de la requete
			String request = in.readLine();
            System.out.println("Request from "+domaine+" ["+new Date()+"] "+request);

			// Vérification de l'entête de la requete (si c'est pas un GET, ça dégage)
			if (request == null || !request.startsWith("GET ")
					|| !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
				sendError(out, 500, "Invalid Method.");
				return;
			}

			// Si root contient un fichier .htpasswd
			if (repertoireProtege()) {
				// On envoie l'erreur 401
				// Lecture requête
				try {
					String ligne;
					enable = false;
					boolean connected = false;

					while (!(ligne = in.readLine()).isEmpty()) { // && !connected
						// Vérifie si dans la requête il y a un login + mdp
						if (ligne.startsWith("Authorization")) {
							connected = true;

							// On récupère le login + mdp codés en base 64
							String[] logmdp = ligne.split(" ");
							// On le décode
							String decodedString = new String(Base64.getDecoder().decode(logmdp[2]));
							// On stocke le login logmdp[0] et le mdp logmdp[1]
							logmdp = decodedString.split(":");
							// On code le MDP en MD5

							// Si l'utilisateur et le mdp entrés correspondent avec ceux du fichier .htpsswd
							// On récupère le fichier htpsswd et on le parcourt via le bufferReader
							FileReader fr = new FileReader(new File(root, ".htpasswd"));
							BufferedReader br = new BufferedReader(fr);
							String line;

							while (((line = br.readLine()) != null) && !enable) {
								// On teste si le login de l'utilisateur ainsi que son mdp correspondent
								String mdpEnMD5 = DigestUtils.md5Hex(logmdp[1]); // clé de hachage (MD5)
								if (line.split(":")[0].equals(logmdp[0]) && line.split(":")[1].equals(mdpEnMD5)) {
									enable = true;
								}
							}
							if (!enable) {
								// Si ce n'est pas le cas on envoie l'erreur 403
								sendError(out, 403, "Forbidden");
							}
							br.close();
							fr.close();
						}
					}
					if (!connected) {
						// Si ce n'est pas le cas on envoie l'erreur 401
						sendError(out, 401, "Unhautorized");
						return;
					}

				} catch (Exception ex) {
					// System.err.println("Error : " + ex.getMessage());
				}
			}
			if (enable) {

				String path = request.substring(4, request.length() - 9);
				File file = new File(root, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();

				if (file.isDirectory()) {
					// Récupération du fichier index.html
					File indexFile = new File(file, "index.html");
					if (indexFile.exists() && !indexFile.isDirectory())
						file = indexFile;
				}

				if (!file.toString().startsWith(root.toString())) {
					// Là on pourrait afficher les fichiers du serveur (option du projet)
					sendError(out, 403, "Permission Denied");
				}

				// File note found
				else if (!file.exists()) {
					sendError(out, 404, "Not Found"); // /:ErrorDocument 404 /notfound.html
				}

				// Chargement des assets et images
				else {
					reader = new BufferedInputStream(new FileInputStream(file));
					// Récupération du bon content type a partir de l'extention du fichier
					String contentType = HTTPServer.MIME_TYPES.get(HTTPServer.getExtension(file));
					// Cas où on a un content type non supporté par le projet
					if (contentType == null) {
						sendError(out, 415, "Unsupported Media Type");
					}

					sendHeader(out, 200, contentType, file.length(), file.lastModified(), file, domaine);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = reader.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					reader.close();
				}
				out.flush();
				out.close();
			}
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