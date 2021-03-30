package fr.ul.miage.projetreseau;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class CustomHttpServer implements Runnable {

    private static final Logger LOG = Logger.getLogger(CustomHttpServer.class.getName());

    private static final File WEB_ROOT = new File(".");
    private static final File INDEX_HTML = new File(WEB_ROOT, "Ressources_exte/index.html");
    private static final File NOT_FOUND = new File(WEB_ROOT, "Ressources_exte/404.html");
    private static final File ERROR = new File(WEB_ROOT, "Ressources_exte/400.html");
    private static final DateTimeFormatter HTTP_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");

    private Socket request;

    private CustomHttpServer(Socket request) {
        this.request = request;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
             PrintWriter out = new PrintWriter(request.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(request.getOutputStream())) {

            String header = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(header);
            String method = tokenizer.nextToken().toUpperCase();
            String resource = tokenizer.nextToken().toLowerCase();
            String protocol = tokenizer.nextToken();

            String status;
            File file;

            //Récupération des information sur la requete
            if (method.equals("GET")) {
                if (resource.endsWith("/")) {
                    file = INDEX_HTML;
                    status = " 200 OK";
                } else {
                    file = NOT_FOUND;
                    status = " 404 Not Found";
                }
            } else {
                file = ERROR;
                status = "400 Bad Request";
            }

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
            String date = now.format(HTTP_FORMATTER);

            System.out.printf("%s %s%s %s\n", method, resource, status, date);
            byte[] data = readFile(file);

            // écriture de l'entête
            out.println(protocol + status);
            out.println("Server : CustomHttpServer v1.0");
            out.println("Date: " + date);
            out.println("Content-Type: text/html; charset=utf-8");
            out.println("Content-Length: " + data.length);
            out.println();
            out.flush();

            // écriture du fichier html
            dataOut.write(data, 0, data.length);
            dataOut.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private byte[] readFile(File file) throws IOException {
        byte[] res;
        try (FileInputStream fis = new FileInputStream(file)) {
            int length = (int) file.length();
            res = new byte[length];
            fis.read(res, 0, length);
        }
        return res;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("HttpServer started and listening to port 8080");
            // infinite loop
            while (true) {
                CustomHttpServer server = new CustomHttpServer(serverSocket.accept());

                Thread thread = new Thread(server);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}