package server;
import java.util.Map;

public record RouteHandler(RequestHandler requestHandler, Map<String, String> params) {
}

