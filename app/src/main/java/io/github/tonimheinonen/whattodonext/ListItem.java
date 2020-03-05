package io.github.tonimheinonen.whattodonext;

public class ListItem {

    private String name;
    private int bonus;
    private int peril;

    public ListItem(String name, int bonus, int peril) {
        this.name = name;
        this.bonus = bonus;
        this.peril = peril;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public int getPeril() {
        return peril;
    }

    public void setPeril(int peril) {
        this.peril = peril;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "name='" + name + '\'' +
                ", bonus=" + bonus +
                ", peril=" + peril +
                '}';
    }
}
