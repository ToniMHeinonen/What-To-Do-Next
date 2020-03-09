package io.github.tonimheinonen.whattodonext.listsactivity;

public class ListItem {

    private String name;
    private int total;
    private int bonus;
    private int peril;

    public ListItem(String name, int bonus, int peril) {
        this.name = name;
        this.bonus = bonus;
        this.peril = peril;
        this.total = bonus + peril;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
        this.total = this.bonus + this.peril;
    }

    public int getPeril() {
        return peril;
    }

    public void setPeril(int peril) {
        this.peril = peril;
        this.total = this.bonus + this.peril;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", bonus=" + bonus +
                ", peril=" + peril +
                '}';
    }
}
