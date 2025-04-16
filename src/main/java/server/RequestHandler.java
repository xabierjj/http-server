package server;

import java.util.Map;

import model.HttpRequest;
import model.HttpResponse;

@FunctionalInterface
public interface RequestHandler {
    HttpResponse handle(HttpRequest request, Map<String,String> params);
}
