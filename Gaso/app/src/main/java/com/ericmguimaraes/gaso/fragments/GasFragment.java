/*
 *     Gaso
 *
 *     Copyright (C) 2016  Eric Guimarães
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ericmguimaraes.gaso.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ericmguimaraes.gaso.R;
import com.ericmguimaraes.gaso.activities.LoginActivity;
import com.ericmguimaraes.gaso.activities.PlainTextActivity;
import com.ericmguimaraes.gaso.activities.registers.ExpensesRegisterActivity;
import com.ericmguimaraes.gaso.config.Constants;
import com.ericmguimaraes.gaso.maps.GooglePlaces;
import com.ericmguimaraes.gaso.maps.LocationHelper;
import com.ericmguimaraes.gaso.maps.PlacesHelper;
import com.ericmguimaraes.gaso.model.Location;
import com.ericmguimaraes.gaso.model.Station;
import com.ericmguimaraes.gaso.util.ConnectionDetector;
import com.ericmguimaraes.gaso.util.GsonManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GasFragment extends Fragment {

    private static int LOCATION_REFRESH_TIME = 1000; //millis
    private static int STATIONS_REFRESH_DISTANCE = 10000; //m
    private int REQUEST_PLACE_REFRESH_DISTANCE = 1000;

    Menu menu;

    boolean isMapAttached = false;

    LocationHelper locationHelper;

    PlacesHelper placesHelper;

    GooglePlaces googlePlaces;

    Location location;

    private List<Station> stationsList = null;

    private boolean isSearching = true;

    private Handler locationHandler;

    MapGasoFragment mapGasoFragment;

    StationFragment stationFragment;

    Location lastLocation;

    Station lastStation;

    boolean blockAlert = false;

    public GasFragment() {
    }

    public static GasFragment newInstance() {
        GasFragment fragment = new GasFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        locationHelper = LocationHelper.getINSTANCE(getActivity());
        googlePlaces = new GooglePlaces();

        placesHelper = new PlacesHelper(getActivity());

        locationHandler = new Handler();

        locationHandler = new Handler();

        locationChecker.run();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gas, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stationFragment = StationFragment.newInstance(stationsList);
        mapGasoFragment = MapGasoFragment.newInstance();
        FragmentTransaction ft;
        ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.content, stationFragment);
        ft.commit();
        isMapAttached=false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_gas, menu);
        this.menu = menu;
        if(isMapAttached){
            menu.findItem(R.id.map_menu_item).setVisible(false);
            menu.findItem(R.id.stations_list_menu_item).setVisible(true);
        } else {
            menu.findItem(R.id.map_menu_item).setVisible(true);
            menu.findItem(R.id.stations_list_menu_item).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        FragmentTransaction ft;
        switch (id) {
            case R.id.map_menu_item:
                if(!isSearching && locationHelper!=null && locationHelper.isConnected()) {
                    ft = getChildFragmentManager().beginTransaction();
                    ft.replace(R.id.content, mapGasoFragment);
                    ft.commit();
                    menu.findItem(R.id.map_menu_item).setVisible(false);
                    menu.findItem(R.id.stations_list_menu_item).setVisible(true);
                    isMapAttached = true;
                }
                return true;
            case R.id.action_help:
                Intent intentHelp = new Intent(getActivity(), PlainTextActivity.class);
                intentHelp.putExtra(PlainTextActivity.EXTRA_TITLE, R.string.help_title);
                intentHelp.putExtra(PlainTextActivity.EXTRA_TEXT, R.string.help_text);
                startActivity(intentHelp);
                return true;
            case R.id.action_disclaimer:
                Intent intentDisclaimer = new Intent(getActivity(), PlainTextActivity.class);
                intentDisclaimer.putExtra(PlainTextActivity.EXTRA_TITLE, R.string.disclaimer_title);
                intentDisclaimer.putExtra(PlainTextActivity.EXTRA_TEXT, R.string.disclaimer_text);
                startActivity(intentDisclaimer);
                return true;
            case R.id.stations_list_menu_item:
                isMapAttached = false;
                ft = getChildFragmentManager().beginTransaction();
                ft.replace(R.id.content, stationFragment);
                ft.commit();
                menu.findItem(R.id.map_menu_item).setVisible(true);
                menu.findItem(R.id.stations_list_menu_item).setVisible(false);
                return true;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                forgetLoggedUser();
                intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isSearching() {
        return isSearching;
    }

    Runnable locationChecker = new Runnable() {
        @Override
        public void run() {
            if (locationHelper.isConnected()) {
                location = locationHelper.getLastKnownLocation();
                if (location != null) {
                    double distance = -1;
                    boolean firstTime = lastLocation == null;
                    if (!firstTime)
                        distance = LocationHelper.distance(location, lastLocation);
                    if (firstTime || distance > STATIONS_REFRESH_DISTANCE) {
                        lastLocation = location;
                        StationSearch task = new StationSearch(location.getLat(), location.getLng());
                        AsyncTaskCompat.executeParallel( task );
                    }
                    if (firstTime || distance > REQUEST_PLACE_REFRESH_DISTANCE) {
                        placesHelper.isAtGasStationAsync(new PlacesHelper.CurrentPlaceListener() {
                            @Override
                            public void OnIsAtGasStationResult(Station station) {
                                showSpentRequestDialog(station);
                            }
                        });
                    }
                }
            }
            locationHandler.postDelayed(locationChecker, LOCATION_REFRESH_TIME);
        }
    };

    private void showSpentRequestDialog(final Station station) {
        if (lastStation == null || lastStation.getId() != station.getId() || !blockAlert) {
            lastStation = station;
            blockAlert = false;
            new AlertDialog.Builder(getContext())
                .setTitle("Abastecendo?")
                .setMessage("Você está num posto? Deseja cadastrar um gasto?")
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String gsonStation = GsonManager.getGsonInstance().toJson(station);
                        Log.e("station_gson", gsonStation);
                        Intent intent = new Intent(getActivity(), ExpensesRegisterActivity.class);
                        intent.putExtra("station_id",station.getId());
                        intent.putExtra("station_name", station.getName());
                        intent.putExtra("station_gson", gsonStation);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        blockAlert = true;
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

        }
    }


    private void updateData() {
        if(stationFragment!=null)
            stationFragment.setStationList(stationsList);
        if(mapGasoFragment!=null)
            mapGasoFragment.setStationList(stationsList);
    }

    public class StationSearch extends AsyncTask<Void,Void,Void> {

        double lat;
        double lgn;

        public StationSearch(double lat, double lgn) {
            this.lat = lat;
            this.lgn = lgn;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isSearching = true;
            stationsList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Location l = new Location();
            l.setLat(lat);
            l.setLng(lgn);
            if(getContext()!=null && new ConnectionDetector(getContext()).isConnectingToInternet())
                stationsList.addAll(googlePlaces.getStationsList(l, null));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateData();
            isSearching = false;
        }

    }

    private void forgetLoggedUser() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_LOGGED_TAG, "");
        editor.apply();
    }

}
