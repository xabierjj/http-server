import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    TrieRoute routePaths = new TrieRoute("");

    routePaths.addPath("/", (HttpRequest httpRequest,Map<String,String> params) -> {
      System.out.println("Root path");
      return new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK);
    });
    routePaths.addPath("/echo/{str}", (HttpRequest httpRequest,Map<String,String> params) -> {
      System.out.println(params.get("str"));
      
      List<HttpHeader> headers = List.of(new HttpHeader("Content-Type", "text/plain") );

      return new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK,params.get("str"),headers );

    });

    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket s = serverSocket.accept(); // Wait for connection from client.
        InputStream in = s.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        List<String> requestLines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
          requestLines.add(line);
        }
        HttpRequest httpRequest = new HttpRequest(requestLines);

        RequestHandlerResponse requestHandler = routePaths.getHandler(httpRequest.getPath());
        HttpResponse httpResponse;
        if (requestHandler==null) {
          httpResponse = new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.NOT_FOUND);
        } else {
          Map<String,String> paramMap = requestHandler.params();
          httpResponse = requestHandler.requestHandler().handle(httpRequest,paramMap);
        }

  
       

        OutputStream outputStream = s.getOutputStream();

        PrintWriter out = new PrintWriter(outputStream, true);
        out.print(httpResponse.getResponse());
        out.flush();
        s.close();
        System.out.println("accepted new connection");
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

}



class HttpResponse {
  private String protocol;
  private HttpStatusCode statusCode;
  private String body;
  private List<HttpHeader> headers = new ArrayList<>();

  HttpResponse(String protocol, HttpStatusCode statusCode, String body,List<HttpHeader> headers) {
    this.protocol = protocol;
    this.statusCode = statusCode;
    this.body = body;
  }
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
    sbd.append("\r\n");

    // TODO add headers
    sbd.append("Content-Length: ");
    sbd.append(this.body != null ? this. body.getBytes(StandardCharsets.UTF_8).length : 0);
    
    for (HttpHeader header: this.headers) {
      sbd.append(header.getName());
      sbd.append(" ");
      sbd.append(header.getValue());
    }
    sbd.append("\r\n");
    sbd.append("\r\n");

    if (this.body != null) {
      sbd.append(this.body);

    }

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
  NOT_FOUND(404, "Not Found"),
  FORBIDDEN(403, "Forbidden");

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

class HttpHeader {
  String name;
  String value;

  public HttpHeader(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}