package model;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponse {
    private String protocol;
    private HttpStatusCode statusCode;
    private String body;
    private HttpHeaders headers = new HttpHeaders();
  
    public HttpResponse(String protocol, HttpStatusCode statusCode, String body) {
      this.protocol = protocol;
      this.statusCode = statusCode;
      this.body = body;
  
      if (this.body != null) {
        // TODO with this we are just allowing text content type
        // we have to make this dynamic, and in case no content type is set and there is a body, set this as default 
        this.headers.set("Content-Type", "text/plain");
  
      }
    }
    public HttpResponse(String protocol, HttpStatusCode statusCode) {
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
  
    public void addHeader(String name, String value) {
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
  