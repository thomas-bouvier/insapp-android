package fr.insapp.insapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.insapp.insapp.R;
import fr.insapp.insapp.activities.ClubActivity;
import fr.insapp.insapp.http.ServiceGenerator;
import fr.insapp.insapp.models.Club;
import fr.insapp.insapp.models.Event;
import fr.insapp.insapp.models.EventComparator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by thomas on 18/11/2016.
 */

public class EventRecyclerViewAdapter extends BaseRecyclerViewAdapter<EventRecyclerViewAdapter.EventViewHolder> {

    private RequestManager requestManager;

    protected List<Event> events;

    private boolean past;
    private int layout;

    private OnEventItemClickListener listener;

    public interface OnEventItemClickListener {
        void onEventItemClick(Event event);
    }

    public EventRecyclerViewAdapter(Context context, RequestManager requestManager, boolean past, int layout) {
        this.events = new ArrayList<>();
        this.context = context;
        this.requestManager = requestManager;
        this.past = past;
        this.layout = layout;
    }

    public void setOnItemClickListener(OnEventItemClickListener listener) {
        this.listener = listener;
    }

    public void addItem(Event event) {
        this.events.add(event);
        Collections.sort(events, new EventComparator(past));

        this.notifyDataSetChanged();
    }

    public void updateEvent(int position, Event event) {
        events.set(position, event);
        notifyItemChanged(position);
    }

    public void removeItem(String eventId) {
        for (final Event event : events) {
            if (event.getId().equals(eventId)) {
                events.remove(event);

                this.notifyDataSetChanged();
                return;
            }
        }
    }

    @NonNull
    @Override
    public EventRecyclerViewAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new EventRecyclerViewAdapter.EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventRecyclerViewAdapter.EventViewHolder holder, int position) {
        final Event event = events.get(position);

        if (layout == R.layout.row_event_with_avatars) {
            Call<Club> call = ServiceGenerator.create().getClubFromId(event.getAssociation());
            call.enqueue(new Callback<Club>() {
                @Override
                public void onResponse(@NonNull Call<Club> call, @NonNull Response<Club> response) {
                    if (response.isSuccessful()) {
                        final Club club = response.body();

                        if (club != null) {
                            requestManager
                                .load(ServiceGenerator.CDN_URL + club.getProfilePicture())
                                .apply(RequestOptions.circleCropTransform())
                                .transition(withCrossFade())
                                .into(holder.avatar);

                            holder.avatar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    context.startActivity(new Intent(context, ClubActivity.class).putExtra("club", club));
                                }
                            });
                        }
                    }
                    else {
                        Toast.makeText(context, "EventRecyclerViewAdapter", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Club> call, @NonNull Throwable t) {
                    Toast.makeText(context, "EventRecyclerViewAdapter", Toast.LENGTH_LONG).show();
                }
            });
        }

        requestManager
            .load(ServiceGenerator.CDN_URL + event.getImage())
            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(8)))
            .into(holder.thumbnail);

        holder.name.setText(event.getName());

        final int nbAttendees = (event.getAttendees() == null) ? 0 : event.getAttendees().size();
        if (nbAttendees <= 1) {
            holder.attendees.setText(String.format(context.getString(R.string.x_attendee), nbAttendees));
        }
        else {
            holder.attendees.setText(String.format(context.getString(R.string.x_attendees), nbAttendees));
        }

        final int diffInDays = (int) ((event.getDateEnd().getTime() - event.getDateStart().getTime()) / (1000 * 60 * 60 * 24));
        if (diffInDays < 1 && event.getDateStart().getMonth() == event.getDateEnd().getMonth()) {
            DateFormat dateFormat_oneday = new SimpleDateFormat("'Le' dd/MM 'à' HH:mm");

            holder.date.setText(dateFormat_oneday.format(event.getDateStart()));
        }
        else {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM");
            String dateStart = dateFormat.format(event.getDateStart());
            String dateEnd = dateFormat.format(event.getDateEnd());

            holder.date.setText("Du " + dateStart + " au " + dateEnd);
        }

        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public List<Event> getEvents() {
        return events;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView name;
        public ImageView avatar;
        public TextView date;
        public TextView attendees;

        public EventViewHolder(View view) {
            super(view);

            this.avatar = view.findViewById(R.id.avatar_club_event);

            this.name = view.findViewById(R.id.name_event);
            this.date = view.findViewById(R.id.date_event);
            this.attendees = view.findViewById(R.id.going_event);
            this.thumbnail = view.findViewById(R.id.thumbnail_event);
        }

        public void bind(final Event event, final OnEventItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onEventItemClick(event);
                }
            });
        }
    }
}