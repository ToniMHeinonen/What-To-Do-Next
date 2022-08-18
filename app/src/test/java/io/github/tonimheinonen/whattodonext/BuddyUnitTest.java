package io.github.tonimheinonen.whattodonext;

import org.junit.Test;

import java.util.ArrayList;
import io.github.tonimheinonen.whattodonext.database.ListItem;
import io.github.tonimheinonen.whattodonext.database.ListOfItems;
import io.github.tonimheinonen.whattodonext.tools.Buddy;

import static org.junit.Assert.*;

/**
 * Tests function found in Buddy class.
 */
public class BuddyUnitTest {
    @Test
    public void sort_items_by_name_ascending() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem a = new ListItem("A", 0, 0);
        ListItem b = new ListItem("B", 0, 0);
        items.add(a);
        items.add(b);
        Buddy.sortItemsByName(items, true);
        assertEquals(a, items.get(0));
        assertEquals(b, items.get(1));
    }

    @Test
    public void sort_items_by_name_descending() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem a = new ListItem("A", 0, 0);
        ListItem b = new ListItem("B", 0, 0);
        items.add(a);
        items.add(b);
        Buddy.sortItemsByName(items, false);
        assertEquals(b, items.get(0));
        assertEquals(a, items.get(1));
    }

    @Test
    public void filter_list_by_fallen_true() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem a = new ListItem("A", 0, 0);
        a.setFallen(true);
        ListItem b = new ListItem("B", 0, 0);
        b.setFallen(false);
        items.add(a);
        items.add(b);
        Buddy.filterListByFallen(items, true);
        assertEquals(a, items.get(0));
    }

    @Test
    public void filter_list_by_fallen_false() {
        ArrayList<ListItem> items = new ArrayList<>();
        ListItem a = new ListItem("A", 0, 0);
        a.setFallen(true);
        ListItem b = new ListItem("B", 0, 0);
        b.setFallen(false);
        items.add(a);
        items.add(b);
        Buddy.filterListByFallen(items, false);
        assertEquals(b, items.get(0));
    }

    @Test
    public void halve_total_bonus_points_3_and_3() {
        testTotalPoints(3, 3, 3);
    }

    @Test
    public void halve_total_bonus_points_0_and_6() {
        testTotalPoints(0, 6, 3);
    }

    @Test
    public void halve_total_bonus_points_0_and_0() {
        testTotalPoints(0, 0, 0);
    }

    @Test
    public void halve_total_bonus_points_2_and_1() {
        testTotalPoints(2, 1, 2);
    }

    private void testTotalPoints(int bonus, int peril, int expected) {
        ListOfItems list = new ListOfItems();
        ArrayList<ListItem> items = new ArrayList<>();
        list.setItems(items);
        items.add(new ListItem("Name", bonus, peril));
        Buddy.halveTotalBonusPoints(list);
        assertEquals(expected, items.get(0).getTotal());
    }
}