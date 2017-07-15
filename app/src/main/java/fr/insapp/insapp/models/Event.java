package fr.insapp.insapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.insapp.insapp.utility.Operation;

/**
 * Created by Antoine on 25/02/2016.
 */

public class Event implements Parcelable, Comparable<Event> {

    @SerializedName("ID")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("association")
    private String association;

    @SerializedName("description")
    private String description;

    @SerializedName("participants")
    private List<String> attendees;

    @SerializedName("maybe")
    private List<String> maybe;

    @SerializedName("notgoing")
    private List<String> notgoing;

    @SerializedName("comments")
    private List<Comment> comments;

    @SerializedName("status")
    private String status;

    @SerializedName("dateStart")
    private Date dateStart;

    @SerializedName("dateEnd")
    private Date dateEnd;

    @SerializedName("image")
    private String image;

    @SerializedName("promotions")
    private List<String> promotions;

    @SerializedName("plateforms")
    private List<String> plateforms;

    @SerializedName("bgColor")
    private String bgColor;

    @SerializedName("fgColor")
    private String fgColor;

    public enum PARTICIPATE {
        YES,
        MAYBE,
        NO,
        UNDEFINED
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {

        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }

    };

    public Event(String id, String name, String association, String description, List<String> attendees, List<String> maybe, List<String> notgoing, List<Comment> comments, String status, Date dateStart, Date dateEnd, String image, List<String> promotions, List<String> plateforms, String bgColor, String fgColor) {
        this.id = id;
        this.name = name;
        this.association = association;
        this.description = description;
        this.attendees = attendees;
        this.maybe = maybe;
        this.notgoing = notgoing;
        this.comments = comments;
        this.status = status;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.image = image;
        this.promotions = promotions;
        this.plateforms = plateforms;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    public Event(JSONObject json) throws JSONException {
        refresh(json);
    }

    public Event(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.association = in.readString();
        this.description = in.readString();

        this.attendees = new ArrayList<>();
        this.maybe = new ArrayList<>();
        this.notgoing = new ArrayList<>();

        final int nbAttendees = in.readInt();
        if (nbAttendees > 0)
            in.readStringList(this.attendees);

        final int nbMaybe = in.readInt();
        if (nbMaybe > 0)
            in.readStringList(this.maybe);

        final int nbNotgoing = in.readInt();
        if (nbNotgoing > 0)
            in.readStringList(this.notgoing);

        this.comments = new ArrayList<>();

        final int nbComments = in.readInt();
        if (nbComments > 0)
            in.readTypedList(comments, Comment.CREATOR);

        this.status = in.readString();

        this.dateStart = new Date(in.readLong());
        this.dateEnd = new Date(in.readLong());

        this.image = in.readString();
        this.bgColor = in.readString();
        this.fgColor = in.readString();
    }

    public void refresh(JSONObject json) throws JSONException {
        this.id = json.getString("ID");
        this.name = json.getString("name");
        this.association = json.getString("association");
        this.description = json.getString("description");

        this.attendees = new ArrayList<>();
        this.maybe = new ArrayList<>();
        this.notgoing = new ArrayList<>();

        JSONArray jsonarray1 = json.optJSONArray("participants");
        if (jsonarray1 != null) {
            for (int i = 0; i < jsonarray1.length(); i++)
                attendees.add(jsonarray1.getString(i));
        }

        JSONArray jsonarray2 = json.optJSONArray("maybe");
        if (jsonarray2 != null) {
            for (int i = 0; i < jsonarray2.length(); i++)
                maybe.add(jsonarray2.getString(i));
        }

        JSONArray jsonarray3 = json.optJSONArray("notgoing");
        if (jsonarray3 != null) {
            for (int i = 0; i < jsonarray3.length(); i++)
                notgoing.add(jsonarray3.getString(i));
        }

        this.comments = new ArrayList<>();

        JSONArray jsonarray4 = json.optJSONArray("comments");
        if (jsonarray4 != null) {
            for (int i = 0; i < jsonarray4.length(); i++)
                comments.add(new Comment(jsonarray4.getJSONObject(i)));
        }

        this.status = json.getString("status");
        this.dateStart = Operation.stringToDate("yyyy-MM-dd'T'HH:mm:ss'Z'", json.getString("dateStart"), true);
        this.dateEnd = Operation.stringToDate("yyyy-MM-dd'T'HH:mm:ss'Z'", json.getString("dateEnd"), true);
        this.image = json.getString("image");
        this.bgColor = json.getString("bgColor");
        this.fgColor = json.getString("fgColor");
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(association);
        dest.writeString(description);

        if (attendees != null) {
            dest.writeInt(attendees.size());
            if (attendees.size() > 0) {
                dest.writeStringList(attendees);
            }
        }

        if (maybe != null) {
            dest.writeInt(maybe.size());
            if (maybe.size() > 0) {
                dest.writeStringList(maybe);
            }
        }

        if (notgoing != null) {
            dest.writeInt(notgoing.size());
            if (notgoing.size() > 0) {
                dest.writeStringList(notgoing);
            }
        }

        dest.writeInt(comments.size());
        if (comments.size() > 0)
            dest.writeTypedList(comments);

        dest.writeString(status);

        dest.writeLong(dateStart.getTime());
        dest.writeLong(dateEnd.getTime());

        dest.writeString(image);
        dest.writeString(bgColor);
        dest.writeString(fgColor);
    }

    public PARTICIPATE getStatusForUser(String userID) {
        for (final String id : getNotgoing()) {
            if (userID.equals(id))
                return Event.PARTICIPATE.NO;
        }

        for (final String id : getMaybe()) {
            if (userID.equals(id))
                return Event.PARTICIPATE.MAYBE;
        }

        for (final String id : getAttendees()) {
            if (userID.equals(id))
                return Event.PARTICIPATE.YES;
        }

        return PARTICIPATE.UNDEFINED;
    }

    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Event)) return false;

        Event otherMyClass = (Event) other;

        return otherMyClass.getId().equals(this.id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAssociation() {
        return association;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public List<String> getMaybe() {
        return maybe;
    }

    public List<String> getNotgoing() {
        return notgoing;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public String getStatus() {
        return status;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public String getImage() {
        return image;
    }

    public List<String> getPromotions() {
        return promotions;
    }

    public List<String> getPlateforms() {
        return plateforms;
    }

    public String getBgColor() {
        return bgColor;
    }

    public String getFgColor() {
        return fgColor;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public int compareTo(@NonNull Event another) {
        return this.getDateStart().compareTo(another.getDateStart());
    }
}
