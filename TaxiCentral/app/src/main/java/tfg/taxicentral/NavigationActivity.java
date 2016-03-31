package tfg.taxicentral;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, RoutingListener {

    protected GoogleMap mMap;
    protected LatLng start;
    protected LatLng end;
    protected Location location = null;
    ArrayList<Polyline> polylines = new ArrayList<>();
    TextView infoRouteTextView = null;
    Button futureStateButton = null;
    private DestinationReachedTask mDestinationReachedTask = null;
    Button destinationReachedButton = null;
    String infoRouteWithRoutes = null;
    String infoRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        infoRouteTextView = (TextView) findViewById(R.id.infoRouteTextView);
        futureStateButton = (Button) findViewById(R.id.futureStateButton);
        futureStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("App","futurestate");
                Intent intent = new Intent(getApplicationContext(), FutureStateActivity.class);
                startActivity(intent);

            }
        });
        destinationReachedButton = (Button) findViewById(R.id.arrivalButton);
        destinationReachedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Recuperar bien los valores
                mDestinationReachedTask = new DestinationReachedTask(Long.valueOf(1), 300.0, 43.3415225, -8.4477031, 43.3415225, -8.4477031, "ds");
                mDestinationReachedTask.execute((Void) null);
                //TODO Volver con elegancia xD
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
                finish();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String url = getIntent().getStringExtra("url");
        double lat = getIntent().getDoubleExtra("lat", 43.3415225);
        double lng = getIntent().getDoubleExtra("lng", -8.4477031);
        end = new LatLng(lat, lng);
        //mMap.addMarker(new MarkerOptions().position(latLng).title("Destination: " + url));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getApplicationContext(), "Debe garantizar los permisos de localizacion para funcionar", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location From GPS

        while (location==null) {
            locationManager.requestLocationUpdates(provider, 5000, 0, this);
            location = locationManager.getLastKnownLocation(provider);
        }

        if(location!=null){
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 5000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        start = new LatLng(location.getLatitude(), location.getLongitude());
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .build();
        routing.execute();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        Log.e("NavigationActivity", "onLocationChanged");
        if (infoRoute != null) {
            infoRouteTextView.setText(infoRoute);
        }
        //TODO Contar la distancia para settear despues
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        if (results[0]<500) {
            destinationReachedButton.performClick();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //RoutingListener
    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        //progressDialog.dismiss();
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorAccent, R.color.common_plus_signin_btn_text_light, R.color.common_action_bar_splitter};
        //progressDialog.dismiss();
        //CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));

        //map.moveCamera(center);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <1; i++) {
        //for (int i = 0; i <route.size(); i++) {

            //In case of more than X alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(20 - i * 4);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            infoRouteWithRoutes = "Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue();
            infoRoute = "distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue();
            //Toast.makeText(getApplicationContext(), infoRoute,Toast.LENGTH_SHORT).show();
        }

        // Start marker

        MarkerOptions options = new MarkerOptions();
        /*
        options.position(start);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        mMap.addMarker(options);
        */
        // End marker
        options = new MarkerOptions();
        options.position(end);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {
        Log.i("", "Routing was cancelled.");
    }

    public class DestinationReachedTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mFutureTravelId;
        private final Double mDistance;
        private final Double mOX;
        private final Double mOY;
        private final Double mDX;
        private final Double mDY;
        private final String mPath;

        DestinationReachedTask(Long futureTravelId, Double distance, Double oX, Double oY, Double dX, Double dY, String path) {
            mFutureTravelId = futureTravelId;
            mDistance = distance;
            mOX = oX;
            mOY = oY;
            mDX = dX;
            mDY = dY;
            mPath = path;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPut put = new HttpPut(getString(R.string.ip)+"arrival/" + mFutureTravelId + "/distance/" + mDistance + "/originpoint/" + mOX + "/" + mOY + "/destinationpoint/" + mDX + "/" + mDY + "/path/" + mPath);
            put.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(put);
                String respStr = EntityUtils.toString(resp.getEntity());
                if(!respStr.equals("true"))
                    resul = false;
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }
            return resul;
        }

    }
}
