package model;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponse {
  private String protocol;
  private HttpStatusCode statusCode;
  private byte[] body;
  private HttpHeaders headers = new HttpHeaders();

  public HttpResponse(String protocol, HttpStatusCode statusCode, String bodyText) {
    this(protocol, statusCode, bodyText != null ? bodyText.getBytes(StandardCharsets.UTF_8) : null);
    if (bodyText != null) {
        headers.set("Content-Type", "text/plain");
    }
}

public HttpResponse(String protocol, HttpStatusCode statusCode, byte[] body) {
    this.protocol = protocol;
    this.statusCode = statusCode;
    this.body = body;

    if (body != null) {
        headers.set("Content-Length", String.valueOf(body.length));
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

    for (Map.Entry<String, String> header : this.headers.entries()) {
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

  public byte[] getResponseBytes() {
    StringBuilder sbd = new StringBuilder();

    sbd.append(protocol).append(" ")
       .append(this.statusCode.getCode()).append(" ")
       .append(this.statusCode.getMessage()).append("\r\n");

    for (Map.Entry<String, String> header : this.headers.entries()) {
        sbd.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
    }

    sbd.append("\r\n");
    byte[] headerBytes = sbd.toString().getBytes(StandardCharsets.UTF_8);

    if (this.body == null) return headerBytes;

    byte[] full = new byte[headerBytes.length + body.length];
    System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
    System.arraycopy(body, 0, full, headerBytes.length, body.length);
    return full;
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
