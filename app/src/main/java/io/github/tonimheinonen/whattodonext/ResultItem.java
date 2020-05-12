package io.github.tonimheinonen.whattodonext;

import java.io.Serializable;

import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

public class ResultItem implements Serializable {

    public static final int RESET = 0, BONUS = 1, PERIL = 2;

    private int position;
    private String name;
    private int oldBonus, newBonus, oldPeril, newPeril;
    private boolean dropped, reseted;

    public ResultItem(int position, ListItem item, int destiny) {
        this.position = position;
        this.name = item.getName();
        oldBonus = item.getBonus();
        oldPeril = item.getPeril();

        switch (destiny) {
            case RESET:
                newBonus = 0;
                newPeril = 0;
                reseted = true;
                break;
            case BONUS:
                newBonus = oldBonus + 1;
                newPeril = oldPeril;
                break;
            case PERIL:
                newBonus = oldBonus;
                newPeril = oldPeril + 1;
                if (newPeril > GlobalPrefs.loadMaxPerilPoints()) {
                    newPeril = 0;
                    dropped = true;
                }
                break;
        }

        Debug.print("ResultItem", "result", toString(), 1);
    }

    @Override
    public String toString() {
        return "ResultItem{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", oldBonus=" + oldBonus +
                ", newBonus=" + newBonus +
                ", oldPeril=" + oldPeril +
                ", newPeril=" + newPeril +
                ", dropped=" + dropped +
                ", reseted=" + reseted +
                '}';
    }
}
