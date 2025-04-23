package model;

public enum HttpStatusCode {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    FORBIDDEN(403, "Forbidden"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    ;
  
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