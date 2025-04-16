import java.util.Map;

public record RequestHandlerResponse(RequestHandler requestHandler, Map<String, String> params) {
}

