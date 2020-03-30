package io.github.tonimheinonen.whattodonext.voteactivity;

import java.util.ArrayList;

import io.github.tonimheinonen.whattodonext.Profile;
import io.github.tonimheinonen.whattodonext.listsactivity.ListOfItems;

public abstract class VoteMaster {

    private static ListOfItems selectedList;
    private static ArrayList<Profile> selectedProfiles;

    public static ListOfItems getSelectedList() {
        return selectedList;
    }

    public static void setSelectedList(ListOfItems list) {
        selectedList = list;
    }

    public static ArrayList<Profile> getSelectedProfiles() {
        return selectedProfiles;
    }

    public static void setSelectedProfiles(ArrayList<Profile> profiles) {
        selectedProfiles = profiles;
    }
}
