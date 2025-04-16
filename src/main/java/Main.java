import server.Routes;
import server.Server;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(Routes.createRouter());
    server.start(4221);

  }

}
