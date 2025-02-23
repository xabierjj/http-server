import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage

    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      Socket s = serverSocket.accept(); // Wait for connection from client.
      InputStream in = s.getInputStream();

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
      List<String> requestLines = new ArrayList<>();
      String line;
      while ((line = bufferedReader.readLine()) != null && !line.isEmpty() ) {
        requestLines.add(line);
      }
      HttpRequest httpRequest = new HttpRequest(requestLines);

      HttpResponse httpResponse = new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK);
      if (!httpRequest.getPath().equals("/")) {
        httpResponse = new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.NOT_FOUND);
      }


      OutputStream outputStream = s.getOutputStream();

      PrintWriter out = new PrintWriter(outputStream, true);
      System.out.println(httpResponse.getResponse());
      out.println(httpResponse.getResponse());
      out.flush();

      System.out.println("accepted new connection");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

}

class HttpResponse {
  private String protocol;
  private HttpStatusCode statusCode;

  HttpResponse(String protocol, HttpStatusCode statusCode) {
    this.protocol = protocol;
    this.statusCode = statusCode;
  }

  public String getResponse() {
    StringBuilder sbd = new StringBuilder();
    // HTTP/1.1 404 Not Found\r\n\r\n
    sbd.append(protocol);
    sbd.append(" ");
    sbd.append(this.statusCode.getCode());
    sbd.append(" ");
    sbd.append(this.statusCode.getMessage());
    sbd.append("\\r\\n\\r\\\\n");

    return sbd.toString();
  }

  public String getProtocol() {
    return this.protocol;
  }

  public HttpStatusCode getStatusCode() {
    return this.statusCode;
  }


  @Override
  public String toString() {
    return "{" +
      " protocol='" + getProtocol() + "'" +
      ", statusCode='" + getStatusCode() + "'" +
      "}";
  }


  
}

class HttpRequest {

  private String path;
  private String protocol;
  private HttpMethod method;

  HttpRequest(List<String> requestData) {
    String requestLine = requestData.get(0);
    String[] parts = requestLine.split(" ");
    this.path = parts[1];
    this.method = HttpMethod.valueOf(parts[0]);
    this.protocol = parts[2];
  }

  public String getPath() {
    return this.path;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public HttpMethod getMethod() {
    return this.method;
  }

  @Override
  public String toString() {
    return "{" +
      " path='" + getPath() + "'" +
      ", protocol='" + getProtocol() + "'" +
      ", method='" + getMethod() + "'" +
      "}";
  }

  
}

enum HttpMethod {
  GET,
  POST,
  PUT,
  PATCH,
  DELETE,
}

enum HttpStatusCode {
  OK(200, "OK"),
  NOT_FOUND(404, "Not Found");

  private int code;
  private String message;

  HttpStatusCode(int value, String message) {
    this.code = value;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
