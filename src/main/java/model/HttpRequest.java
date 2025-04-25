package model;

import java.util.List;

public class HttpRequest {

    private String path;
    private String protocol;
    private HttpMethod method;
    private HttpHeaders headers;
    private String body;
  
    public HttpRequest(List<String> requestData) {
      this.headers = new HttpHeaders();
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

    
    public String getBody() {
      return this.body;
    }

    public void setBody(String body) {
      this.body = body;
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