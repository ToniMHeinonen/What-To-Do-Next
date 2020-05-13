package io.github.tonimheinonen.whattodonext;

import java.io.Serializable;

import io.github.tonimheinonen.whattodonext.database.DatabaseValue;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.tools.Debug;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

public class SavedResultItem implements Serializable, DatabaseValue {

    public static final int RESET = 0, BONUS = 1, PERIL = 2;

    public final int position;
    public final String name;
    public final String bonusSign, perilSign;
    public final int oldBonus, newBonus, oldPeril, newPeril, bonusDiff, perilDiff;
    public final boolean dropped, reset;

    public SavedResultItem(int position, ListItem item, int destiny) {
        this.position = position;
        this.name = item.getName();
        oldBonus = item.getBonus();
        oldPeril = item.getPeril();

        switch (destiny) {
            case RESET:
                newBonus = 0;
                newPeril = 0;
                reset = true;
                dropped = false;
                break;
            case BONUS:
                newBonus = oldBonus + 1;
                newPeril = oldPeril;
                reset = false;
                dropped = false;
                break;
            case PERIL:
                newBonus = oldBonus;
                int peril = oldPeril + 1;
                if (peril > GlobalPrefs.loadMaxPerilPoints()) {
                    newPeril = 0;
                    dropped = true;
                } else {
                    newPeril = peril;
                    dropped = false;
                }
                reset = false;
                break;
            default:
                newBonus = -1;
                newPeril = -1;
                dropped = false;
                reset = false;
                break;
        }

        bonusDiff = newBonus - oldBonus;
        perilDiff = newPeril - oldPeril;

        bonusSign = bonusDiff > 0 ? "+" : "";
        perilSign = perilDiff > 0 ? "+" : "";

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
                ", reset=" + reset +
                '}';
    }
}
