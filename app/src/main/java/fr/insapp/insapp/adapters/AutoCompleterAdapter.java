package fr.insapp.insapp.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.insapp.insapp.R;
import fr.insapp.insapp.http.ServiceGenerator;
import fr.insapp.insapp.models.SearchTerms;
import fr.insapp.insapp.models.User;
import fr.insapp.insapp.models.UserSearchResults;
import fr.insapp.insapp.utility.GlideApp;
import fr.insapp.insapp.utility.Utils;
import retrofit2.Call;

/**
 * Created by thomas on 27/02/2017.
 */

public class AutoCompleterAdapter extends ArrayAdapter<User> implements Filterable {

    private Context context;

    private List<User> filteredUsers;

    public AutoCompleterAdapter(Context context, int resource) {
        super(context, resource);

        this.context = context;
        this.filteredUsers = new ArrayList<>();
    }

    public int getCount() {
        return filteredUsers.size();
    }

    public User getItem(int index) {
        return filteredUsers.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dropdown, parent, false);
        }

        final User user = getItem(position);
        final Resources resources = context.getResources();

        if (user != null) {
            ((TextView) convertView.findViewById(R.id.dropdown_textview)).setText(String.format(resources.getString(R.string.tag), user.getUsername()));
        }

        // get the drawable of avatar

        final int id = resources.getIdentifier(Utils.INSTANCE.drawableProfileName(user.getPromotion(), user.getGender()), "drawable", context.getPackageName());
        GlideApp.with(context).load(id).into(((ImageView) convertView.findViewById(R.id.dropdown_avatar)));

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                // this method is called async

                final FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    Call<UserSearchResults> call = ServiceGenerator.create().searchUsers(new SearchTerms(constraint.toString()));
                    UserSearchResults results;
                    try {
                        results = call.execute().body();

                        List<User> users = null;
                        if (results != null) {
                            users = results.getUsers();
                        }
                        List<User> filteredUsers = new ArrayList<>();

                        if (users != null) {
                            if (users.size() > 0) {
                                for (int i = 0; i < Math.min(users.size(), 10); i++) {
                                    filteredUsers.add(users.get(i));
                                }
                            }

                            filterResults.values = filteredUsers;
                            filterResults.count = filteredUsers.size();
                        }
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    filteredUsers = (List<User>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object result) {
                return ((User) result).getUsername();
            }
        };
    }

    public List<User> getFilteredUsers() {
        return filteredUsers;
    }
}