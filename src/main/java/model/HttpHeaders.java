package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpHeaders {
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