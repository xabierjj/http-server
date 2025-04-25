package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket s = serverSocket.accept();
                System.out.println("Open TCP connection: ");
                System.out.println(s.getLocalAddress());
                this.runThread(s);

            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    public void runThread(Socket s) {
        Thread thread = new Thread(() -> {

            try {
                // TODO Consider using BufferedInputStream and ByteArrayOutputStream if you want to support more advanced body handling (binary or UTF-8-safe).
                InputStream in = s.getInputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

                /*
                 * POST REQUEST EXAMPLE:
                 * 
                 * POST /submit-form HTTP/1.1\r\n
                 * Host: example.com\r\n
                 * Content-Type: application/x-www-form-urlencoded\r\n
                 * Content-Length: 27\r\n
                 * \r\n
                 * name=Xabier&age=30&lang=Java
                 * 
                 * We use readline until there is no more content or until we find an empty
                 * line. After the empty line the we process the body of the request
                 */

                // Maintain TCP connection open to listen for more requests

                while (true) {
                    List<String> requestLines = new ArrayList<>();
                    String line;

                    while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                        requestLines.add(line);
                    }

                    if (requestLines.isEmpty()) {
                        System.out.println("Client disconnected");
                        break;
                    }
                    HttpRequest httpRequest = new HttpRequest(requestLines);

                    String contentLengthHeader = httpRequest.getHeaderValue("Content-Length");

                    if (contentLengthHeader != null) {

                        // TODO: This assumes the body contains only text and uses a character encoding
                        // where 1 character = 1 byte (e.g., ASCII or ISO-8859-1). For encodings like
                        // UTF-8
                        // or UTF-16, this can break if characters use multiple bytes, since
                        // Content-Length
                        // is in bytes but we're reading characters. Use InputStream.read(byte[]) for
                        // binary or multibyte-safe reading.

                        int contentLength = Integer.parseInt(contentLengthHeader);
                        char[] bodyChars = new char[contentLength];
                        int totalRead = 0;
                        while (totalRead < contentLength) {
                            int read = bufferedReader.read(bodyChars, totalRead, contentLength - totalRead);
                            if (read == -1)
                                break;
                            totalRead += read;
                        }

                        String requestBody = new String(bodyChars, 0, totalRead);
                        httpRequest.setBody(requestBody);

                    }
                    RouteHandler requestHandler = this.router.getHandler(httpRequest);
                    HttpResponse httpResponse;
                    if (requestHandler == null) {
                        httpResponse = new HttpResponse(httpRequest, HttpStatusCode.NOT_FOUND);
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

                    // if the request has Connection: close header we have to close the TCP connection
                    String connectionHeader = httpRequest.getHeaderValue("Connection");
                    if ("close".equalsIgnoreCase(connectionHeader)) {
                        System.out.println("connectionHeader");
                        System.out.println(connectionHeader);
                        break;
                    }

                }

            } catch (IOException exception) {
                System.out.println("IOException: " + exception.getMessage());

            } finally {
                try {
                    System.out.println("CLOSE TCP connection: ");
                    System.out.println(s.getLocalAddress());
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });

        thread.start();
    }

}
