package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        router.addPath("/files/{filename}", (request, params) -> {
            String filename = params.get("filename");
            Path filePath = Paths.get("/tmp", filename); 

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                try {
                    byte[] fileBytes = Files.readAllBytes(filePath);

                    HttpResponse response = new HttpResponse(request.getProtocol(), HttpStatusCode.OK, fileBytes.toString());
                    response.addHeader("Content-Type", "application/octet-stream");
                    response.addHeader("Content-Length", String.valueOf(fileBytes.length));
                    return response;

                } catch (IOException e) {
                    return new HttpResponse(request.getProtocol(), HttpStatusCode.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new HttpResponse(request.getProtocol(), HttpStatusCode.NOT_FOUND);
            }
        });


        return router;
    }
}