package io.github.tonimheinonen.whattodonext.savedresults;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.github.tonimheinonen.whattodonext.database.DatabaseValue;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.tools.GlobalPrefs;

@IgnoreExtraProperties
public class SavedResultItem implements Serializable, DatabaseValue {

    @Exclude
    public static final int RESET = 0, BONUS = 1, PERIL = 2;

    @Exclude
    private String dbID;

    private String resultID;
    private int position;
    private String name;
    private String bonusSign, perilSign;
    private int oldBonus, newBonus, oldPeril, newPeril, bonusDiff, perilDiff;
    private boolean dropped, reset;

    /**
     * Default constructor.
     *
     * Required for calls to DataSnapshot.getValue(SavedResultItem.class).
     */
    public SavedResultItem() {}

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
                break;
            case BONUS:
                newBonus = oldBonus + 1;
                newPeril = oldPeril;
                break;
            case PERIL:
                newBonus = oldBonus;
                int peril = oldPeril + 1;
                if (peril > GlobalPrefs.loadMaxPerilPoints()) {
                    newPeril = 0;
                    dropped = true;
                } else {
                    newPeril = peril;
                }
                break;
        }

        bonusDiff = newBonus - oldBonus;
        perilDiff = newPeril - oldPeril;

        bonusSign = bonusDiff > 0 ? "+" : "";
        perilSign = perilDiff > 0 ? "+" : "";
    }

    public String getResultID() {
        return resultID;
    }

    public void setResultID(String resultID) {
        this.resultID = resultID;
    }

    public String getDbID() {
        return dbID;
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOldBonus() {
        return oldBonus;
    }

    public void setOldBonus(int oldBonus) {
        this.oldBonus = oldBonus;
    }

    public int getNewBonus() {
        return newBonus;
    }

    public void setNewBonus(int newBonus) {
        this.newBonus = newBonus;
    }

    public int getOldPeril() {
        return oldPeril;
    }

    public void setOldPeril(int oldPeril) {
        this.oldPeril = oldPeril;
    }

    public int getNewPeril() {
        return newPeril;
    }

    public void setNewPeril(int newPeril) {
        this.newPeril = newPeril;
    }

    public boolean isDropped() {
        return dropped;
    }

    public void setDropped(boolean dropped) {
        this.dropped = dropped;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public String getBonusSign() {
        return bonusSign;
    }

    public void setBonusSign(String bonusSign) {
        this.bonusSign = bonusSign;
    }

    public String getPerilSign() {
        return perilSign;
    }

    public void setPerilSign(String perilSign) {
        this.perilSign = perilSign;
    }

    public int getBonusDiff() {
        return bonusDiff;
    }

    public void setBonusDiff(int bonusDiff) {
        this.bonusDiff = bonusDiff;
    }

    public int getPerilDiff() {
        return perilDiff;
    }

    public void setPerilDiff(int perilDiff) {
        this.perilDiff = perilDiff;
    }

    /**
     * Maps values for database handling.
     * @return mapped values
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("resultID", resultID);
        result.put("position", position);
        result.put("name", name);
        result.put("oldBonus", oldBonus);
        result.put("newBonus", newBonus);
        result.put("bonusDiff", bonusDiff);
        result.put("bonusSign", bonusSign);
        result.put("oldPeril", oldPeril);
        result.put("newPeril", newPeril);
        result.put("perilDiff", perilDiff);
        result.put("perilSign", perilSign);
        result.put("dropped", dropped);
        result.put("reset", reset);

        return result;
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
