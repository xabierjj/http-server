package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.HttpRequest;
import model.HttpResponse;
import model.HttpStatusCode;

public class Server {

    private Router router;

    public Server(Router router) {
        this.router = router;
    }

    public void start(Integer port) {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("accepted new connection");
                this.runThread(s);

            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    public void runThread(Socket s) {
        Thread thread = new Thread(() -> {

            try {
                InputStream in = s.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                List<String> requestLines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                    requestLines.add(line);
                }
                HttpRequest httpRequest = new HttpRequest(requestLines);

                String contentLengthHeader = httpRequest.getHeaderValue("Content-Length");

                if (contentLengthHeader!=null) {
                    
                    // Read content and add body to request
                    int contentLength = Integer.parseInt(contentLengthHeader);
                    System.out.println("contentLength" +contentLength);

                    char[] bodyChars = new char[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        System.out.println("totalReads" +totalRead);

                        int read = bufferedReader.read(bodyChars, totalRead, contentLength - totalRead);
                        System.out.println("Lineee" +read);

                        if (read == -1) break; 
                        totalRead += read;
                    }


                    String requestBody = new String(bodyChars, 0, totalRead);
                    System.out.println(requestBody);


                }
                RouteHandler requestHandler = this.router.getHandler(httpRequest.getPath());
                HttpResponse httpResponse;
                if (requestHandler == null) {
                    httpResponse = new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.NOT_FOUND);
                } else {
                    Map<String, String> paramMap = requestHandler.params();
                    httpResponse = requestHandler.requestHandler().handle(httpRequest, paramMap);
                }

                OutputStream outputStream = s.getOutputStream();
                // Use OutputStream directly instead of PrintWriter to avoid corrupting binary
                // data.
                // PrintWriter is character-based and not suitable for binary responses like
                // images or downloads.
                // This ensures we correctly send both text and binary content (e.g.
                // application/octet-stream).
                outputStream.write(httpResponse.getResponseBytes());
                outputStream.flush();
            } catch (IOException exception) {
                System.out.println("IOException: " + exception.getMessage());

            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });

        thread.start();
    }

}
