import server.Routes;
import server.Server;

public class Main {
  public static void main(String[] args) {
    
    // If directory is not given defaults to tmp folder
    String fileDirectory = "/tmp";
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--directory") && i + 1 < args.length) {
        fileDirectory = args[i + 1];
      }
    }

    Server server = new Server(Routes.createRouter(fileDirectory));
    server.start(4221);

  }

}
