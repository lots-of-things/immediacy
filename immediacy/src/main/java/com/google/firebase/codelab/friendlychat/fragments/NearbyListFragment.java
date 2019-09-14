package com.google.firebase.codelab.friendlychat.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.codelab.friendlychat.R;
import com.google.firebase.codelab.friendlychat.adapters.OnlineUsersAdapter;
import com.google.firebase.codelab.friendlychat.constants.Constant;
import com.google.firebase.codelab.friendlychat.models.Conversation;
import com.google.firebase.codelab.friendlychat.models.UserProfile;
import com.google.firebase.codelab.friendlychat.utils.DatabaseUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.codelab.friendlychat.constants.Constant.LOCATION_SERVICES;

public class NearbyListFragment extends Fragment {

    public static final double RADIUS = 0.50;
    private GeoFire geoFire;
    private String userId;
    private int totalUser;



    private List<UserProfile> conversationProfiles;
    private OnlineUsersAdapter onlineUsersAdapter;
    private ListView conversationsView;
    private ProgressBar mainProgresBar;
    private OnFragmentInteractionListener activity;


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                Log.d(LOCATION_SERVICES, "onLocationResult() called with: locationResult = [" + locationResult + "]" + location.getProvider() + " " + location.getAccuracy());

                GeoLocation myLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                geoFire.setLocation(userId, myLocation);

                updateQuery(myLocation);

            }
        }
    };
    private GeoQuery geoQuery;

    private final GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {

        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
            LatLng latLng = new LatLng(location.latitude, location.longitude);
            UserProfile tempUserProfile = new UserProfile();
            tempUserProfile.setId(key);

            //retrieve the user from the database with an async task

            DatabaseUtils.getUserProfileReferenceById(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

                    if (mainProgresBar != null) mainProgresBar.setVisibility(View.GONE);

                    if (userProfile != null) {
                        if (!userProfile.getId().equals(userId)) {
                            onlineUsersAdapter.add(userProfile);
                            DatabaseUtils.loadProfileImage(userProfile.getId(), bitmap -> {
                                userProfile.setAvatar(bitmap);
                                onlineUsersAdapter.notifyDataSetChanged();

                            }, null);
                            Log.w(Constant.NEARBY_CHAT, "id " + userProfile.getId());
                            Log.w(Constant.NEARBY_CHAT, "userId " + userId);
                        }else{
                            Log.w(Constant.NEARBY_CHAT, "Let's not do that instead");

                        }
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constant.NEARBY_CHAT, "onCancelled() called with: databaseError = [" + databaseError + "]");
                    Log.w(Constant.NEARBY_CHAT, "onCancelled: ", databaseError.toException());
                }
            });


            //update number of people connected
            incTotalUser();
        }


        @Override
        public void onKeyExited(String key) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s is no longer in the search area", key));
            //update number of people connected
            decTotalUser();
        }


        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            Log.d(Constant.NEARBY_CHAT, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            LatLng position = new LatLng(location.latitude, location.longitude);


        }

        @Override
        public void onGeoQueryReady() {
            Log.d(Constant.NEARBY_CHAT, "onGeoQueryReady: All initial data has been loaded and events have been fired!");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Log.w(Constant.NEARBY_CHAT, "onGeoQueryError: There was an error with this query: ", error.toException());
        }
    };
//    private final ValueEventListener getUserProfileListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//
//            UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
//
//            if (mainProgresBar != null) mainProgresBar.setVisibility(View.GONE);
//
//            if (userProfile != null) {
//                activeConversationsAdapter.add(userProfile);
//                DatabaseUtils.loadProfileImage(userProfile.getId(), bitmap -> {
//                    userProfile.setAvatar(bitmap);
//                    activeConversationsAdapter.notifyDataSetChanged();
//
//                }, null);
//                Log.w(Constant.NEARBY_CHAT, "id " + userProfile.getId());
//            }
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Log.w(Constant.NEARBY_CHAT, "Canceled profile request");
//
//        }
//    };
//    private final ChildEventListener userConversationsListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//            Conversation conversation = dataSnapshot.getValue(Conversation.class);
//
//            if (conversation != null) {
//
//                DatabaseUtils.getUserProfileReferenceById(conversation.getPartnerId())
//                        .addListenerForSingleValueEvent(getUserProfileListener);
//            } else {
//                Log.w(Constant.NEARBY_CHAT, "No conversations");
//            }
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Log.w(Constant.NEARBY_CHAT, "loadPost:onCancelled", databaseError.toException());
//        }
//    };

    public NearbyListFragment() {
    }


    public static NearbyListFragment newInstance() {
        return new NearbyListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationProfiles = new ArrayList<>();

        mainProgresBar = (ProgressBar) getActivity().findViewById(R.id.online_spinner);
        if (mainProgresBar != null) {
            mainProgresBar.setVisibility(View.VISIBLE);
        }

        geoFire = DatabaseUtils.getNewLocationDatabase();
        userId = DatabaseUtils.getCurrentUUID();

//        DatabaseUtils.getConversationsReferenceById(DatabaseUtils.getCurrentUUID()).addChildEventListener(userConversationsListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

//        locationCallback
        onlineUsersAdapter = new OnlineUsersAdapter(getActivity(), R.layout.online_users_entry, conversationProfiles);

        conversationsView = (ListView) view.findViewById(R.id.conversation_users_list);
        conversationsView.setAdapter(onlineUsersAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NearbyListFragment.OnFragmentInteractionListener) {
            activity = (NearbyListFragment.OnFragmentInteractionListener) context;
            activity.addLocationCallback(locationCallback);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        clearGeoQuery();
        activity.removeLocationCallback(locationCallback);
        activity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        DatabaseUtils.getConversationsReferenceById(DatabaseUtils.getCurrentUUID())
//                .removeEventListener(userConversationsListener);
        clearGeoQuery();

    }

    private void decTotalUser() {
        if (totalUser > 0) totalUser--;
        updateSubtitle();
    }

    private void updateSubtitle() {
        Log.d(Constant.NEARBY_CHAT, "updateSubtitle: ");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        toolbar.setSubtitle(totalUser + " online user(s)");
    }

    private void incTotalUser() {
        totalUser++;
        updateSubtitle();
    }

    private void updateQuery(GeoLocation myLocation) {
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(myLocation, RADIUS);
            geoQuery.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQuery.setLocation(myLocation, RADIUS);
        }
    }

    private void clearGeoQuery() {

        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery = null;
        }
    }

    public interface OnFragmentInteractionListener {
        void addLocationCallback(LocationCallback locationCallback);

        void removeLocationCallback(LocationCallback locationCallback);
    }
}
