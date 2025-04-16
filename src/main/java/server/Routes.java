package server;

import model.HttpResponse;
import model.HttpStatusCode;

public class Routes {
    public static Router createRouter() {
        Router router = new Router("");

        router.addPath("/", (request, params) ->
            new HttpResponse(request.getProtocol(), HttpStatusCode.OK)
        );

        router.addPath("/user-agent", (request, params) -> {
            String userAgent = request.getHeaderValue("user-agent");
            return new HttpResponse(request.getProtocol(), HttpStatusCode.OK, userAgent);
        });

        router.addPath("/echo/{str}", (request, params) -> {
            String str = params.get("str");
            return new HttpResponse(request.getProtocol(), HttpStatusCode.OK, str);
        });

        return router;
    }
}