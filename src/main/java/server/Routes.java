package server;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import model.HttpResponse;
import model.HttpStatusCode;
import services.FileService;

public class Routes {
    public static Router createRouter(String fileDirectory) {
        FileService fileService = new FileService(fileDirectory);
        Router router = new Router();

        router.get("/", (request, params) -> new HttpResponse(request.getProtocol(), HttpStatusCode.OK));

        router.get("/user-agent", (request, params) -> {
            String userAgent = request.getHeaderValue("user-agent");
            return new HttpResponse(request.getProtocol(), HttpStatusCode.OK, userAgent);
        });

        router.get("/echo/{str}", (request, params) -> {
            String str = params.get("str");
            return new HttpResponse(request.getProtocol(), HttpStatusCode.OK, str);
        });

        router.get("/files/{filename}", (request, params) -> {
            String filename = params.get("filename");
            try {
                byte[] fileBytes = fileService.readFile(filename);
                HttpResponse response = new HttpResponse(request.getProtocol(), HttpStatusCode.OK,
                        fileBytes);
                response.addHeader("Content-Type", "application/octet-stream");
                return response;
            } catch (NoSuchFileException e) {
                return new HttpResponse(request.getProtocol(), HttpStatusCode.NOT_FOUND);

            } catch (IOException e) {
                return new HttpResponse(request.getProtocol(), HttpStatusCode.INTERNAL_SERVER_ERROR);

            }
        });

        router.post("/files/{filename}", (request, params) -> {
            String filename = params.get("filename");
            try {
                fileService.writeFile(filename, request.getBody());
                return new HttpResponse(request.getProtocol(), HttpStatusCode.CREATED);
            } catch (IOException e) {
                return new HttpResponse(request.getProtocol(), HttpStatusCode.INTERNAL_SERVER_ERROR);
            }
            
       
        });

        return router;
    }
}