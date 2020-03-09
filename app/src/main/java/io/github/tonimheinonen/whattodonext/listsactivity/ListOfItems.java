package io.github.tonimheinonen.whattodonext.listsactivity;

import java.util.ArrayList;

public class ListOfItems<E> extends ArrayList<E> {

    private String name;
    private ArrayList<E> items = new ArrayList<>();

    public ListOfItems(String name) {
        this.name = name;
    }

    public ListOfItems(String name, ArrayList<E> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<E> getItems() {
        return items;
    }

    public void setItems(ArrayList<E> items) {
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
