import java.util.HashMap;
import java.util.Map;



@FunctionalInterface
interface RequestHandler {
    HttpResponse handle(HttpRequest request, Map<String,String> params);
}



public class TrieRoute {
    Map<String, TrieRoute> childrens = new HashMap<>();
    boolean isChild;
    RequestHandler handler; 
    String name;

    TrieRoute(String name) {
        this.name = name;
    }
    public void setIsChild(boolean isChild) {
        this.isChild = isChild;
    }
    public void setHandler(RequestHandler handler) {
        this.handler = handler;
    }

    //TODO how do we map the handler
    public void addPath(String path,RequestHandler handler) {
        // split "/"
        String[] elements = path.split("/");

        //  "user" "phone"

        TrieRoute currElement = this;
       

        int index = 0;
        // iterate the array until we no longer find a child and add the missing elements
        while ( index < elements.length) {
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
        for (int i = index; i < elements.length; i ++) {
            String element = elements[i];
            String name = element;
            if (this.isWildCard(element)) {
                name = name.replace("{", "").replace("}", "");
                element = ":param";
            }
            TrieRoute trieRoute = new TrieRoute(name);
            currElement.childrens.put(element, trieRoute);
            currElement = trieRoute;
        }

        currElement.setIsChild(true);
        currElement.setHandler(handler);


        // when we reach to the last element int the array -> set the handler to the leaf node
    }

    private boolean isWildCard(String name) {
        return name.startsWith("{") && name.endsWith("}");
    }

    public RequestHandlerResponse getHandler(String path) {
        String[] elements = path.split("/");
        int index = 0;
        // iterate the array until we no longer find a child and add the missing elements
        TrieRoute currElement = this;
        Map<String,String> paramMap = new HashMap<>();
        while ( index < elements.length) {
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
            return new RequestHandlerResponse( currElement.handler,paramMap);
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


    public Map<String,TrieRoute> getChildrens() {
        return this.childrens;
    }

    public void setChildrens(Map<String,TrieRoute> childrens) {
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


 
