package com.gbtrip.org.travel_share;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.gbtrip.org.travel_share.Main_project.get_inst;
import static com.gbtrip.org.travel_share.Main_project.load_data;

public class raccept extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        ResultCallback<Status> {

    String trainingfile=String.valueOf(R.raw.train_data);

    public static void main(String[] args) {
        int LValue = Integer.parseInt(args[0]);
        int KValue = Integer.parseInt(args[1]);
        String trainingfile=String.valueOf(R.raw.train_data);
       // String trainingFileName = args[2];
        String testfile=String.valueOf(R.raw.test_data);

//        String testFileName = args[3];
      //  String validationFileName = args[4];
        String validfile=String.valueOf(R.raw.validation_data);

        String Print = args[5];
        Decision_tree tree = new Decision_tree();
        Tree_inst file_data =load_data(trainingfile);
        Tree_inst file_rows = get_inst(file_data, 0, 0, file_data.getRowCount(), file_data.getColCount() - 1);
        Tree_inst resultRows =get_inst(file_data, 0, file_data.getColCount() - 1, file_data.getRowCount(), 1);
        Tree_nodes root1 = tree.makeTree(file_rows, resultRows, 1);
        Tree_nodes root2 = tree.makeTree(file_rows, resultRows, 2);

        if(Print.equals("yes") || Print.equals("Yes")){
            try {
                System.out.println("Decision Tree - Information Gain heuristics");
                tree.printTree(root1);
                System.out.println("Decision Tree - Variance Impurity heuristics");
                tree.printTree(root2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Tree_inst test_data = load_data(testfile);
        Tree_inst testDataRows = get_inst(test_data, 0, 0, test_data.getRowCount(), test_data.getColCount() - 1);
        Tree_inst testResultRows = get_inst(test_data, 0, test_data.getColCount() - 1,  test_data.getRowCount(), 1);
        Decision_tree_accuaracy Tree_1_accuracy = new Decision_tree_accuaracy();
        Tree_1_accuracy.parseInstances(testDataRows, testResultRows, root1);
        Decision_tree_accuaracy treeAcc2 = new Decision_tree_accuaracy();
        treeAcc2.parseInstances(testDataRows, testResultRows, root2);
        System.out.println("Decision Tree - 1 : Accuracy");
        Tree_1_accuracy.Accuracy();
        System.out.println("Decision Tree - 2 : Accuracy");
        treeAcc2.Accuracy();
        Tree_inst validation_data = load_data(validfile);
        Tree_inst valDataRows = get_inst(validation_data, 0, 0, validation_data.getRowCount(), validation_data.getColCount() - 1);
        Tree_inst valResultRows = get_inst(validation_data, 0, validation_data.getColCount() - 1,  validation_data.getRowCount(), 1);
        Decision_tree_accuaracy treePrunAccb1 = new Decision_tree_accuaracy();
        treePrunAccb1.parseInstances(testDataRows, testResultRows, root1);
        double acc1prunb = treePrunAccb1.getAcc();
        if(Print.equals("yes") || Print.equals("Yes")){
            try {
                System.out.println("Pruned - Decision Tree - Information Gain heuristics");
                tree.printTree(root1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Tree_pruning treePrune1 = new Tree_pruning();
        System.out.println("Pruning Decision Tree1");
        Tree_nodes root1prun =treePrune1.pruning(valDataRows,valResultRows, LValue, KValue, root1);
        Decision_tree_accuaracy treePrunAcca1 = new Decision_tree_accuaracy();
        treePrunAcca1.parseInstances(testDataRows, testResultRows, root1prun);
        System.out.println("Accuracy of Decision Tree1 after pruning");
        if(acc1prunb > treePrunAcca1.getAcc()) {
            treePrunAccb1.Accuracy();
        }
        else {
            treePrunAccb1.Accuracy();
        }

        Decision_tree_accuaracy treePrunAccb2 = new Decision_tree_accuaracy();
        treePrunAccb2.parseInstances(testDataRows, testResultRows, root2);
        double acc2prunb = treePrunAccb2.getAcc();

        if(Print.equals("yes") || Print.equals("Yes")){
            try {
                System.out.println("Pruned - Decision Tree - Variance Impurity heuristics");
                tree.printTree(root2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Tree_pruning treePrune2 = new Tree_pruning();
        System.out.println("Pruning Decision Tree2");
        Tree_nodes root2prun =treePrune2.pruning(valDataRows,valResultRows, LValue, KValue, root2);
        Decision_tree_accuaracy Tree_2_accuracy = new Decision_tree_accuaracy();
        Tree_2_accuracy.parseInstances(testDataRows, testResultRows, root2prun);
        System.out.println("Accuracy - Pruned - Decision Tree 1");
        if(acc2prunb > Tree_2_accuracy.getAcc()) {
            treePrunAccb2.Accuracy();
        }
        else {
            treePrunAccb2.Accuracy();
        }

    }

        private GoogleMap mMap;
        private GoogleApiClient client;
        private Location lastLocation;
        private LocationRequest locationRequest;
        private Marker currentLocationMarker;
        public LatLng latLng, latLng1;
        private Marker pickMarker;
        Double checklat, checklon;
        private FirebaseAuth mAuth;
        private LatLng picklat;
        Float FinDist,distance;
        Float agecomp,ageset;
        String FinGender,FinAge;
        String Finacceptage,FinacceptGender;
        private FirebaseDatabase display,mdisplay;


    Marker mCurrLocationMarker;
        Button btn;

        Intent intent;

        public static final int REQUEST_LOCATION_CODE = 99;
        private static final int[] COLORS = new int[]{R.color.design_default_color_primary};


        @Override
        public View onCreateView (LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            View view = inflater.inflate(R.layout.activity_raccept, null);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            intent = new Intent(getActivity(), condition.class);
            mAuth = FirebaseAuth.getInstance();

            btn = (Button) view.findViewById(R.id.acceptbtn);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Decision_tree decision = new Decision_tree();
                    Tree_pruning treeprun = new Tree_pruning();

                    String userrid = mAuth.getCurrentUser().getUid();
                    DatabaseReference reff = FirebaseDatabase.getInstance().getReference("accept");
                    GeoFire geoFire = new GeoFire(reff);
                    geoFire.setLocation(userrid, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Toast.makeText(getContext(), "There was an error saving the location to GeoFire: " + error, Toast.LENGTH_LONG).show();
                            } else {

                            }
                        }
                    });
                    picklat = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(picklat).title("pick"));
                    btn.setText("Getting your ride....");
                    Toast.makeText(getContext(), "geting", Toast.LENGTH_SHORT).show();
                    getClosestDriver();
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }
            Places.initialize(getContext(), "AIzaSyBrZt9COUshx059Lb2Q4hiPWMK8KCG6O_c");

// Create a new Places client instance.
            PlacesClient placesClient = Places.createClient(getContext());
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // TODO: Get info about the selected place.
                    if (Geocoder.isPresent()) {
                        try {
                            String location = place.getName();
                            Geocoder gc = new Geocoder(getContext());
                            List<Address> addresses = gc.getFromLocationName(location, 5); // get the found Address Object
                            LatLng ll;
                            // List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                            for (Address a : addresses) {
                                if (a.hasLatitude() && a.hasLongitude()) {
                                    ll = new LatLng(a.getLatitude(), a.getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(ll);
                                    markerOptions.title("sathya");
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    pickMarker = mMap.addMarker(markerOptions);
                                    String url = getUrl(ll, latLng1);
                                    Log.d("onMapClick", url.toString());
                                    raccept.FetchUrl FetchUrl = new raccept.FetchUrl();

                                    // Start downloading json data from Google Directions API
                                    FetchUrl.execute(url);
                                    Log.i("Place and location", "Place: " + place.getName() + ", " + ll);
                                    //getLastKnownLocation();
                                    //getRouteToMarker(ll);
                                }
                            }
                        } catch (IOException e) {
                            // handle the exception
                        }
                    }
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("Error", "An error occurred: " + status);
                }
            });


            return view;
        }
        private int radius = 1;
        private Boolean driverfound =false ;
        private String driverFoundID;
        private void getClosestDriver () {
            DatabaseReference shareloc = FirebaseDatabase.getInstance().getReference().child("Available");
            GeoFire geoFire = new GeoFire(shareloc);

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(picklat.latitude, picklat.longitude), radius);
            Toast.makeText(getContext(), "query", Toast.LENGTH_SHORT).show();

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override

                public void onKeyEntered(String key, GeoLocation location) {
                    Toast.makeText(getContext(), "loop", Toast.LENGTH_SHORT).show();
                    if (!driverfound) {
                        driverfound = true;
                        driverFoundID = key;
                        DatabaseReference driveref = FirebaseDatabase.getInstance().getReference().child("Available").child(driverFoundID);
                        String customerId = mAuth.getCurrentUser().getUid();
                        HashMap map = new HashMap();
                        map.put("customerId is", customerId);
                        driveref.updateChildren(map);
                        Toast.makeText(getContext(), "hmmm", Toast.LENGTH_SHORT).show();
                        getDriverLocation();
                        btn.setText("Looking for Rider Location...");
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    Toast.makeText(getContext(), "NO driver found", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                    if (!driverfound) {
                        radius++;
                        getClosestDriver();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }

        private Marker Ridermarker;
        private void getDriverLocation () {
            Toast.makeText(getContext(), "this toast", Toast.LENGTH_SHORT).show();
            DatabaseReference driverlocref = FirebaseDatabase.getInstance().getReference().child("Available").child(driverFoundID).child("l");
            driverlocref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(getContext(), "driverrr", Toast.LENGTH_SHORT).show();
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        double locationlat = 0;
                        double locationlong = 0;
                        btn.setText("Rider Found....");
                        if (map.get(0) != null) {
                            locationlat = Double.parseDouble(map.get(0).toString());
                        }

                        if (map.get(1) != null) {
                            locationlong = Double.parseDouble(map.get(1).toString());
                        }
                        LatLng driverlatlng = new LatLng(locationlat, locationlong);

                        if (Ridermarker != null) {
                            Ridermarker.remove();
                        }
                        Location loc1 = new Location("");
                        loc1.setLatitude(picklat.latitude);
                        loc1.setLongitude(picklat.longitude);


                        Location loc2 = new Location("");
                        loc2.setLatitude(driverlatlng.latitude);
                        loc2.setLongitude(driverlatlng.longitude);

                        distance = loc1.distanceTo(loc2);
                        btn.setText("Rider Found : " + String.valueOf(distance));
                        String url = getUrl(picklat, driverlatlng);
                        Log.d("onMapClick", url.toString());
                        raccept.FetchUrl FetchUrl = new raccept.FetchUrl();

                        // Start downloading json data from Google Directions API
                        FetchUrl.execute(url);

                        Ridermarker = mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Position"));
                     /*  display=FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference=display.getReference().child("acceptcondition").child(mAuth.getUid());
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                conditondata data =dataSnapshot.getValue(conditondata.class);
                                FinAge.equals(data.getUserage());
                                FinGender.equals(data.getUsergender());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        mdisplay=FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference1=mdisplay.getReference().child("acceptcondition").child(driverFoundID);
                        databaseReference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                conditondata data =dataSnapshot.getValue(conditondata.class);
                                Finacceptage.equals(data.getUserage());
                                FinacceptGender.equals(data.getUsergender());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        if(FinGender.equals(FinacceptGender)) {
                            Toast.makeText(getContext(), "Gender Constraints matched", Toast.LENGTH_SHORT).show();
                              /*  if (agecomp > 10 && ageset > 10) {
                                    Toast.makeText(getContext(), "Age constraints matched", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), "Constraints matched successfully", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), "RIDE ACCEPTED", Toast.LENGTH_LONG).show();
                                } else if (agecomp < 10 && ageset < 10) {
                                    Toast.makeText(getContext(), "Age constraints matched", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), "Constraints matched successfully", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getContext(), "RIDE ACCEPTED", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Age factor does not matches,Ride cancelled", Toast.LENGTH_SHORT).show();
                                    Fragment fragment = new acceptcondition();
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.content_frame, fragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                }
                            }
                        }
                            else
                            {
                                Toast.makeText(getContext(), "Gender does not matches,Ride cancelled", Toast.LENGTH_SHORT).show();
                                Fragment fragment = new acceptcondition();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.content_frame, fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }

                        /*
                      //  agecomp=Float.parseFloat(FinAge);
                       // ageset=Float.parseFloat(Finacceptage);
                        if(distance < 5000.00 ){
                            Toast.makeText(getContext(), "Distance is less than 5 km...Checking other constraints", Toast.LENGTH_LONG).show();
                            }
                        else
                        {
                            Toast.makeText(getContext(), "Ride sharer is far away!!!!,Ride cancelled", Toast.LENGTH_SHORT).show();
                            Fragment fragment = new acceptcondition();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }*/
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            switch (requestCode) {
                case REQUEST_LOCATION_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (client == null) {
                                buildGoogleApiClient();
                            }
                            mMap.setMyLocationEnabled(true);
                        }
                    } else {
                        Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_LONG).show();
                    }
                    return;
            }
        }

        @Override
        public void onMapReady (GoogleMap googleMap){
            mMap = googleMap;
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }

        private String getUrl (LatLng origin, LatLng dest){

            // Origin of route
            String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

            // Destination of route
            String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyBrZt9COUshx059Lb2Q4hiPWMK8KCG6O_c";


            return url;
        }


        /**
         * A method to download json data from url
         */
        private String downloadUrl (String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

        // Fetches data from url passed
        private class FetchUrl extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... url) {

                // For storing data from web service
                String data = "";

                try {
                    // Fetching the data from web service
                    data = downloadUrl(url[0]);
                    Log.d("Background Task data", data.toString());
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                }
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                raccept.ParserTask parserTask = new raccept.ParserTask();

                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);

            }
        }

        /**
         * A class to parse the Google Places in JSON format
         */
        private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

            // Parsing the data in non-ui thread
            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;

                try {
                    jObject = new JSONObject(jsonData[0]);
                    Log.d("ParserTask", jsonData[0].toString());
                    DataParsers parser = new DataParsers();
                    Log.d("ParserTask", parser.toString());

                    // Starts parsing data
                    routes = parser.parse(jObject);
                    Log.d("ParserTask", "Executing routes");
                    Log.d("ParserTask", routes.toString());

                } catch (Exception e) {
                    Log.d("ParserTask", e.toString());
                    e.printStackTrace();
                }
                return routes;
            }

            // Executes in UI thread, after the parsing process
            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points;
                PolylineOptions lineOptions = null;

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(20);
                    lineOptions.color(Color.BLUE);

                    Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                }

                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {
                    mMap.addPolyline(lineOptions);
                } else {
                    Log.d("onPostExecute", "without Polylines drawn");
                }
            }
        }


        protected synchronized void buildGoogleApiClient () {

            client = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            client.connect();
        }


        @Override
        public void onConnected (@Nullable Bundle bundle){
            locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
            }

        }

        public boolean checkLocationPermission () {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                }
                return false;
            } else {
                return false;
            }
        }


        @Override
        public void onConnectionSuspended ( int i){

        }

        @Override
        public void onConnectionFailed (@NonNull ConnectionResult connectionResult){

        }

        @Override
        public void onLocationChanged (Location location){
            lastLocation = location;
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            checklat = latLng.latitude;
            checklon = latLng.longitude;
            latLng1 = new LatLng(checklat, checklon);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng1);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        }

        @Override
        public boolean onMarkerClick (Marker marker){
            return false;
        }

        @Override
        public void onMarkerDragStart (Marker marker){

        }

        @Override
        public void onMarkerDrag (Marker marker){

        }

        @Override
        public void onMarkerDragEnd (Marker marker){

        }

        @Override
        public void onResult (@NonNull Status status){

        }

        @Override
        public void onStop () {
            super.onStop();

        }
    }
