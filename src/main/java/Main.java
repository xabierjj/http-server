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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    TrieRoute routePaths = new TrieRoute("");

    routePaths.addPath("/user-agent", (HttpRequest httpRequest,Map<String,String> params) -> {
      String userAgent = httpRequest.getHeaderValue("user-agent");
      return new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK, userAgent);
    });

    routePaths.addPath("/", (HttpRequest httpRequest,Map<String,String> params) -> {
      System.out.println("Root path");
      return new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK);
    });
    routePaths.addPath("/echo/{str}", (HttpRequest httpRequest,Map<String,String> params) -> {
      System.out.println(params.get("str"));
      
      HttpResponse response = new HttpResponse(httpRequest.getProtocol(), HttpStatusCode.OK,params.get("str") );
      return response;

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
  private HttpHeaders headers = new HttpHeaders();

  HttpResponse(String protocol, HttpStatusCode statusCode, String body) {
    this.protocol = protocol;
    this.statusCode = statusCode;
    this.body = body;

    if (this.body != null) {
      // TODO with this we are just allowing text content type
      // we have to make this dynamic, and in case no content type is set and there is a body, set this as default 
      this.headers.set("Content-Type", "text/plain");

    }
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
    sbd.append("\r\n");

    
    for (Map.Entry<String,String> header: this.headers.entries()) {
      sbd.append(header.getKey());
      sbd.append(":");
      sbd.append(" ");
      sbd.append(header.getValue());
      sbd.append("\r\n");

    }
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

  public void setHeader(String name, String value) {
    this.headers.set(name, value);
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
  private HttpHeaders headers;

  HttpRequest(List<String> requestData) {
    this.headers = new HttpHeaders();
    System.out.println(requestData);
    // First line contains requestLine. ex: GET /user-agent HTTP/1.1
    String requestLine = requestData.get(0);
    String[] parts = requestLine.split(" ");
    this.path = parts[1];
    this.method = HttpMethod.valueOf(parts[0]);
    this.protocol = parts[2];
    // the next lines contain the headers (if there is no body)
    for (int i = 1; i< requestData.size(); i++) {
      String headerLine = requestData.get(i);
      int colonIndex = headerLine.indexOf(":");
      if (colonIndex != -1) {
          String headerName = headerLine.substring(0, colonIndex).trim();
          String headerValue = headerLine.substring(colonIndex + 1).trim();
          this.headers.set(headerName, headerValue);
      } else {
        // TODO Do I have to throw an error ?
      }
    }
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
  public String getHeaderValue(String headerName) {
    return this.headers.get(headerName.toLowerCase());
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

 class HttpHeaders {
  private final Map<String, String> headers;

  public HttpHeaders() {
      this.headers = new HashMap<>();
  }

  public void set(String name, String value) {
      headers.put(name.toLowerCase(), value);
  }

  public String get(String name) {
      return headers.get(name.toLowerCase());
  }

  public boolean contains(String name) {
      return headers.containsKey(name.toLowerCase());
  }

  public void remove(String name) {
      headers.remove(name.toLowerCase());
  }

  public Set<String> names() {
      return headers.keySet();
  }

  public Set<Map.Entry<String, String>> entries() {
      return headers.entrySet();
  }

  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : headers.entrySet()) {
          sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
      }
      return sb.toString();
  }
}