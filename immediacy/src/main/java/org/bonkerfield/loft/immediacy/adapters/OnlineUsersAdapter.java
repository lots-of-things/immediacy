package org.bonkerfield.loft.immediacy.adapters;

import android.content.Context;
import android.location.Location;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import org.bonkerfield.loft.immediacy.R;
import org.bonkerfield.loft.immediacy.models.UserProfile;
import org.bonkerfield.loft.immediacy.utils.DatabaseUtils;
import com.google.firebase.database.DatabaseError;

public class OnlineUsersAdapter extends ArrayAdapter<UserProfile> {

    private final int layoutResource;
    private final List<UserProfile> userProfileList;
    private OnAdapterInteractionListener activity;
    private GeoFire geoFire;
    private String currentUserId;
    private Location startingLocation;

    public OnlineUsersAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserProfile> userProfiles) {
        super(context, resource, userProfiles);

        this.layoutResource = resource;
        this.userProfileList = userProfiles;
        this.geoFire = DatabaseUtils.getNewLocationDatabase();
        this.currentUserId = DatabaseUtils.getCurrentUUID();

        if (context instanceof OnAdapterInteractionListener) {
            activity = (OnAdapterInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdapterInteractionListener");
        }

        geoFire.getLocation(currentUserId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    startingLocation = new Location("");
                    startingLocation.setLatitude(location.latitude);
                    startingLocation.setLongitude(location.longitude);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        final UserProfile user = userProfileList.get(position);


        TextView userName = (TextView) convertView.findViewById(R.id.active_user_name);
        TextView userBio = (TextView) convertView.findViewById(R.id.active_user_bio);
        TextView userDistance = (TextView) convertView.findViewById(R.id.active_user_distance);
        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.active_user_avatar);

        userName.setText(user.getUserName());
        userBio.setText(user.getBio());
        userAvatar.setImageBitmap(user.getAvatar());
//        userDistance.setText(" - (" + 100 +" meters away)");

        if(startingLocation!=null){
            geoFire.getLocation(user.getId(), new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    Location endingLocation = new Location("");
                    endingLocation.setLatitude(location.latitude);
                    endingLocation.setLongitude(location.longitude);
                    float distance = startingLocation.distanceTo(endingLocation);
                    userDistance.setText(" - (" + Math.round(distance) +" meters away)");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        convertView.setOnClickListener(v -> activity.startChatActivity(user));

        return convertView;
    }

    public interface OnAdapterInteractionListener {
        void startChatActivity(UserProfile partnerProfile);
    }
}
