package fr.ul.miage.projetreseau;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Logger;

public class HTTPRequest extends Thread {

    private File root;
    private Socket socket;
    private String domaine;

    public HTTPRequest(Socket socket, File rootDir,String domaine) {
        this.socket = socket;
        root = rootDir;
        this.domaine=domaine;
    }

    /**
     * Methode permettant d'envoyer l'entêtes d'une ressource (html, css, js, images ...)
     * @param out Object qui permet d'écrire les données et les envoie au navigateur
     * @param code d'envoie (200 = tout ce passe bien, 400 = erreur)
     * @param contentType type de ressource envoyé (js, html ...)
     * @param contentLength taille de la ressource
     * @param lastModified date de modification de la ressource
     * @throws IOException cas ou les données ne peuvent pas être écrite dans le buffer
     */
    private void sendHeader(BufferedOutputStream out, int code, String contentType, long contentLength, long lastModified) throws IOException {
        System.out.println("Send    to   "+domaine+" ["+new Date()+"]"+" HTTP/1.0 GET, code :"+code+", "+contentType+", contentLength:"+contentLength);
        String head =
                "HTTP/1.0 " + code + " OK\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") +
                "Last-modified: " + new Date(lastModified) + "\r\n" +
                "\r\n";
        out.write(head.getBytes());
    }

    private void sendError(BufferedOutputStream out, int code, String message) throws IOException {
        sendHeader(out, code, "text/html", message.length(), System.currentTimeMillis());
        out.write(message.getBytes());
        out.flush();
        out.close();
    }

    public void run() {
        InputStream reader = null;
        try {
            socket.setSoTimeout(30000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

            String request = in.readLine();
            System.out.println("Request from "+domaine+" ["+new Date()+"] "+request);
            if (request == null || !request.startsWith("GET ") || !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
                sendError(out, 500, "Invalid Method.");
                return;
            }
            String path = request.substring(4, request.length() - 9);
            File file = new File(root, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();
            if (file.isDirectory()) {
                // Check to see if there is an index file in the directory.
                File indexFile = new File(file, "index.html");
                if (indexFile.exists() && !indexFile.isDirectory()) {
                    file = indexFile;
                }
            }

            if (!file.toString().startsWith(root.toString())) {
                // Uh-oh, it looks like some lamer is trying to take a peek
                // outside of our web root directory.
                sendError(out, 403, "Permission Denied.");
            }
            else if (!file.exists()) {
                sendError(out, 404, "File Not Found.");
            }
            else if (file.isDirectory()) {
                // print directory listing
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                File[] files = file.listFiles();
                sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
                String title = "Index of " + path;
                out.write(("<html><head><title>" + title + "</title></head><body><h3>Index of " + path + "</h3><p>\n").getBytes());
                for (int i = 0; i < files.length; i++) {
                    file = files[i];
                    String filename = file.getName();
                    String description = "";
                    if (file.isDirectory()) {
                        description = "&lt;DIR&gt;";
                    }
                    out.write(("<a href=\"" + path + filename + "\">" + filename + "</a> " + description + "<br>\n").getBytes());
                }
                out.write(("</p><hr><p>" + SimpleWebServer.VERSION + "</p></body><html>").getBytes());
            }
            else {
                reader = new BufferedInputStream(new FileInputStream(file));
                String contentType = (String)SimpleWebServer.MIME_TYPES.get(SimpleWebServer.getExtension(file));
                if (contentType == null) {
                    contentType = "application/octet-stream";
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
        }
        catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception anye) {
                    // Do nothing.
                }
            }
        }
    }

}

