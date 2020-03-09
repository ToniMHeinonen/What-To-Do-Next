package io.github.tonimheinonen.whattodonext.listsactivity;

import java.util.ArrayList;

public class ListOfItems {

    private String name;
    private ArrayList<ListItem> items = new ArrayList<>();

    public ListOfItems(String name) {
        this.name = name;
    }

    public ListOfItems(String name, ArrayList<ListItem> items) {
        this.name = name;
        this.items = items;
    }

    public void addItem(ListItem item) {
        items.add(item);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ListItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ListItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ListOfItems{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}
