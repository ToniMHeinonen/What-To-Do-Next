package io.github.tonimheinonen.whattodonext.listsactivity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ListItem {

    private String name;
    private int total;
    private int bonus;
    private int peril;

    public ListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ListItem.class)
    }

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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("total", total);
        result.put("bonus", bonus);
        result.put("perik", peril);

        return result;
    }
}
