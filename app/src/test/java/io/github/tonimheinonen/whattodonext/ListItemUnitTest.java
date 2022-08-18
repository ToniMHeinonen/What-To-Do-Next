package io.github.tonimheinonen.whattodonext;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.github.tonimheinonen.whattodonext.database.ListItem;

import static org.junit.Assert.assertEquals;

/**
 * Tests function found in Buddy class.
 */
public class ListItemUnitTest {
    @Test
    public void sort_items_by_points() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem item1 = createListItemVotePoints(5);
        ListItem item2 = createListItemVotePoints(15);
        ListItem item3 = createListItemVotePoints(10);
        items.add(item1);
        items.add(item2);
        items.add(item3);

        Collections.sort(items, Comparator.reverseOrder());

        assertEquals(item2, items.get(0));
        assertEquals(item3, items.get(1));
        assertEquals(item1, items.get(2));
    }

    private ListItem createListItemVotePoints(int votePoints) {
        ListItem item = new ListItem("Name", 0, 0);
        item.setVotePoints(votePoints);
        return item;
    }

    @Test
    public void sort_items_by_voter_amount() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem item1 = createListItemVoterAmount(5);
        ListItem item2 = createListItemVoterAmount(4);
        ListItem item3 = createListItemVoterAmount(7);
        items.add(item1);
        items.add(item2);
        items.add(item3);

        Collections.sort(items, Comparator.reverseOrder());

        assertEquals(item3, items.get(0));
        assertEquals(item1, items.get(1));
        assertEquals(item2, items.get(2));
    }

    private ListItem createListItemVoterAmount(int voterAmount) {
        ListItem item = createListItemVotePoints(5);

        for(int i = 0; i < voterAmount; i++)
            item.addVoterAmount();

        return item;
    }

    @Test
    public void sort_items_by_standard_deviation() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem item1 = createListItemStandardDeviation(5, 0, 0);
        ListItem item2 = createListItemStandardDeviation(2, 2, 1);
        ListItem item3 = createListItemStandardDeviation(3, 1, 1);
        ListItem item4 = createListItemStandardDeviation(4, 1, 0);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);

        Collections.sort(items, Comparator.reverseOrder());

        assertEquals(item2, items.get(0));
        assertEquals(item3, items.get(1));
        assertEquals(item4, items.get(2));
        assertEquals(item1, items.get(3));
    }

    private ListItem createListItemStandardDeviation(int vote1, int vote2, int vote3) {
        ListItem item = new ListItem("Name", 0, 0);
        item.addVotePoints(vote1, true);
        item.addVotePoints(vote2, true);
        item.addVotePoints(vote3, true);

        for(int i = 0; i < 3; i++)
            item.addVoterAmount();

        return item;
    }
}