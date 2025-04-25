package server;

import java.util.HashMap;
import java.util.Map;

import model.HttpMethod;
import model.HttpRequest;

public class Router {
    private final Map<HttpMethod, RouteTrie> methodTries = new HashMap<>();

    public Router() {
        methodTries.put(HttpMethod.GET, new RouteTrie(""));
        methodTries.put(HttpMethod.POST, new RouteTrie(""));
    }

    public void post(String path, RequestHandler handler) {
        this.methodTries.get(HttpMethod.POST).addPath(path, handler);
    }
    public void get(String path, RequestHandler handler) {
        this.methodTries.get(HttpMethod.GET).addPath(path, handler);
    }
    public RouteHandler getHandler(HttpRequest httpRequest) {
        return this.methodTries.get(httpRequest.getMethod()).getHandler(httpRequest.getPath());
    }

    
}

class RouteTrie {
    Map<String, RouteTrie> childrens = new HashMap<>();
    boolean isChild;
    RequestHandler handler;
    String name;

    public RouteTrie(String name) {
        this.name = name;
    }

    public void setIsChild(boolean isChild) {
        this.isChild = isChild;
    }

    public void setHandler(RequestHandler handler) {
        this.handler = handler;
    }

    public void addPath(String path, RequestHandler handler) {
        String[] elements = path.split("/");

        RouteTrie currElement = this;

        int index = 0;
        // iterate the array until we no longer find a child and add the missing elements
        while (index < elements.length) {
            String element = elements[index];
            if (this.isWildCard(element)) {
                element = ":param";
            }
            if (!currElement.childrens.containsKey(element)) {
                break;
            }
            currElement = currElement.childrens.get(element);
            index++;
        }

        // element at index is not in the treenode
        for (int i = index; i < elements.length; i++) {
            String element = elements[i];
            String name = element;
            if (this.isWildCard(element)) {
                name = name.replace("{", "").replace("}", "");
                element = ":param";
            }
            RouteTrie trieRoute = new RouteTrie(name);
            currElement.childrens.put(element, trieRoute);
            currElement = trieRoute;
        }

        currElement.setIsChild(true);
        currElement.setHandler(handler);

    }

    private boolean isWildCard(String name) {
        return name.startsWith("{") && name.endsWith("}");
    }

    public RouteHandler getHandler(String path) {
        String[] elements = path.split("/");
        int index = 0;
        // iterate the array until we no longer find a child and add the missing
        // elements
        RouteTrie currElement = this;
        Map<String, String> paramMap = new HashMap<>();
        while (index < elements.length) {
            String element = elements[index];
            if (!currElement.childrens.containsKey(element) && !currElement.childrens.containsKey(":param")) {
                break;
            }
            if (currElement.childrens.containsKey(element)) {
                currElement = currElement.childrens.get(element);
            } else {
                currElement = currElement.childrens.get(":param");
                paramMap.put(currElement.getName(), element);

            }
            index++;
        }

        if (index == elements.length && currElement.isChild) {
            // We have found the handler
            return new RouteHandler(currElement.handler, paramMap);
        } else {
            // TODO should I throw error here?
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                " childrens='" + getChildrens() + "'" +
                ", isChild='" + isIsChild() + "'" +
                ", name='" + getName() + "'" +
                "}";
    }

    public Map<String, RouteTrie> getChildrens() {
        return this.childrens;
    }

    public void setChildrens(Map<String, RouteTrie> childrens) {
        this.childrens = childrens;
    }

    public boolean isIsChild() {
        return this.isChild;
    }

    public boolean getIsChild() {
        return this.isChild;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
