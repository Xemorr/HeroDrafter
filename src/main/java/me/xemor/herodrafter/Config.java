package me.xemor.herodrafter;

import com.google.gson.annotations.SerializedName;
import me.xemor.herodrafter.match.TrueSkill.TrueSkill;

import java.util.List;
import java.util.Set;

public class Config {

    private List<Hero> heroes;

    @SerializedName(value = "join_queue_message")
    private String joinQueueMessage;

    @SerializedName(value = "leave_queue_message")
    private String leaveQueueMessage;

    @SerializedName(value = "invalid_permissions")
    private String invalidPermissions;

    @SerializedName(value = "admins")
    private Set<Long> adminIDs;

    @SerializedName(value = "need_profile")
    private String needProfileMessage;

    @SerializedName(value = "new_preferences")
    private String newPreferencesMessage;

    @SerializedName(value = "invalid_preferences")
    private String invalidPreferencesMessage;

    @SerializedName(value = "already_has_profile")
    private String alreadyHasProfile;

    @SerializedName(value = "profile_created")
    private String profileCreated;

    @SerializedName(value = "invalid_hero_name")
    private String invalidHeroName;

    @SerializedName(value = "successfully_added_hero")
    private String successfullyAddedHeroMessage;

    @SerializedName(value = "successfully_removed_hero")
    private String successfullyRemovedHeroMessage;

    @SerializedName(value = "missing_roles")
    private String missingRolesMessage;

    @SerializedName(value = "empty_queue_message")
    private String emptyQueueMessage;

    @SerializedName(value = "no_matches_ongoing")
    private String noOngoingMatchesMessage;

    private List<String> maps;

    @SerializedName(value = "color", alternate = "colour")
    private int color;

    @SerializedName(value = "default_player")
    private Player defaultPlayer;

    @SerializedName(value = "queue_vc_error")
    private String queueVCErrorMessage;

    private List<List<String>> compositions;

    @SerializedName(value = "true_skill")
    private TrueSkill trueSkill;

    public TrueSkill getTrueSkill() {
        return trueSkill;
    }

    public String getQueueVCErrorMessage() {
        return queueVCErrorMessage;
    }

    public String getNoOngoingMatchesMessage() {
        return noOngoingMatchesMessage;
    }

    public String getEmptyQueueMessage() {
        return emptyQueueMessage;
    }

    public List<String> getMaps() {
        return maps;
    }

    public String getMissingRolesMessage() {
        return missingRolesMessage;
    }

    public String getSuccessfullyRemovedHeroMessage() {
        return successfullyRemovedHeroMessage;
    }

    public String getSuccessfullyAddedHeroMessage() {
        return successfullyAddedHeroMessage;
    }

    public String getInvalidHeroName() {
        return invalidHeroName;
    }

    public List<List<String>> getCompositions() {
        return compositions;
    }

    public String getProfileCreated() {
        return profileCreated;
    }

    public String getAlreadyHasProfile() {
        return alreadyHasProfile;
    }

    public Player getDefaultPlayer() {
        return defaultPlayer;
    }

    public String getInvalidPreferencesMessage() {
        return invalidPreferencesMessage;
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public String getNewPreferencesMessage() {
        return newPreferencesMessage;
    }

    public String getJoinQueueMessage() {
        return joinQueueMessage;
    }

    public String getLeaveQueueMessage() {
        return leaveQueueMessage;
    }

    public Set<Long> getAdminIDs() {
        return adminIDs;
    }

    public String getInvalidPermissions() {
        return invalidPermissions;
    }

    public String getNeedProfileMessage() {
        return needProfileMessage;
    }

    public int getColor() {
        return color;
    }

}