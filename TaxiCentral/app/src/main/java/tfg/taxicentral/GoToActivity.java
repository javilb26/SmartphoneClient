package tfg.taxicentral;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GoToActivity extends AppCompatActivity {

    private int flag = 25;
    private String[] placesString;
    private String[] placesStrSelected = new String[4];
    private Long[] placesIdSelected = new Long[4];
    private GetPlacesTask mGetPlacesTask = null;
    private TakeClientToTask mTakeClientToTask = null;
    private HashMap<String, Long> countries = new HashMap<>(), regions = new HashMap<>(), cities = new HashMap<>(), addresses = new HashMap<>();
    private Long travelId = (long) 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to);

        //TODO Poner de forma elegante los predefinidos
        AutoCompleteTextView actv;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewCountries);
        placesStrSelected[0]="ESPAÑA";
        placesIdSelected[0]=(long)1;
        actv.setText("ESPAÑA");
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewRegions);
        actv.setText("A Coruña");
        placesStrSelected[1]="A Coruña";
        placesIdSelected[1]=(long)9;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewCities);
        actv.setText("A Coruña");
        placesStrSelected[2]="A Coruña";
        placesIdSelected[2]=(long)6944;
        mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[2], "addressId", addresses, R.id.autoCompleteTextViewAddresses);
        flag = 3;
        mGetPlacesTask.execute((Void) null);

        AutoCompleteTextView actvC = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewCountries);
        actvC.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO hay que darle dos veces para que cargue
                Log.e("HOLAA","HOLAAAAAAA");
                mGetPlacesTask = new GetPlacesTask("countries", (long) 0, "countryId", countries, R.id.autoCompleteTextViewCountries);
                flag = 0;
                mGetPlacesTask.execute((Void) null);
            }
        });

        Button mGoToButton = (Button) findViewById(R.id.goToButton);
        mGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = placesStrSelected[3] + ", " + placesStrSelected[2] + ", " + placesStrSelected[1] + ", " + placesStrSelected[0];
                Log.e("GoTo", url);
                //TODO Rectificar los tiempos, se ejecuta el travelId antes que la creacion del travel -> asegurarse
                mTakeClientToTask = new TakeClientToTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), placesIdSelected[0], placesIdSelected[1], placesIdSelected[2], placesIdSelected[3]);
                mTakeClientToTask.execute((Void) null);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    List<Address> geocodeMatches = new Geocoder(getApplicationContext()).getFromLocationName(url, 1);
                    if (geocodeMatches != null) {
                        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("lat", geocodeMatches.get(0).getLatitude());
                        intent.putExtra("lng", geocodeMatches.get(0).getLongitude());
                        intent.putExtra("travelId", travelId);
                        intent.putExtra("routing", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Destino no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "No se pudo convertir la direccion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class GetPlacesTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final Long mId;
        private final String mTypePlaceId;
        private final HashMap<String, Long> mPlaces;
        private final int mAutoCompleteTextView;

        GetPlacesTask(String url, Long id, String typePlaceId, HashMap<String, Long> places, int autoCompleteTextView) {
            mUrl = url;
            mId = id;
            mTypePlaceId = typePlaceId;
            mPlaces = places;
            mAutoCompleteTextView = autoCompleteTextView;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get;
            if (mId == 0) {
                get = new HttpGet(getString(R.string.ip) + mUrl);
            } else {
                get = new HttpGet(getString(R.string.ip) + mUrl + mId);
            }
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    mPlaces.put(obj.getString("name"), (long) obj.getInt(mTypePlaceId));
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter(mPlaces, mAutoCompleteTextView);
        }

    }

    private String[] iterator(HashMap places) {
        Iterator it = places.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            placesString[i] = e.getKey().toString();
            i += 1;
        }
        return placesString;
    }

    private void selectStrId(AdapterView<?> parent, int position, HashMap<String, Long> places) {
        placesStrSelected[flag] = (String) parent.getItemAtPosition(position);
        placesIdSelected[flag] = places.get(placesStrSelected[flag]);
    }

    public void createInstanceArrayAdapter(final HashMap<String, Long> places, int autoCompleteTextView) {
        ArrayAdapter<String> adapterC = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, iterator(places));
        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvC = (AutoCompleteTextView) findViewById(autoCompleteTextView);
        Log.e("GoToActivity", "createInstanceArrayAdapter");
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        if (flag == 0) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("GoToActivity", "createInstanceArrayAdapter -> Flag: " + flag);
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("countries/", placesIdSelected[flag], "regionId", regions, R.id.autoCompleteTextViewRegions);
                    flag = 1;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 1) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("GoToActivity", "createInstanceArrayAdapter -> Flag: " + flag);
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("regions/", placesIdSelected[flag], "cityId", cities, R.id.autoCompleteTextViewCities);
                    flag = 2;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 2) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("GoToActivity", "createInstanceArrayAdapter -> Flag: " + flag);
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[flag], "addressId", addresses, R.id.autoCompleteTextViewAddresses);
                    flag = 3;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 3) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("GoToActivity", "createInstanceArrayAdapter -> Flag: " + flag);
                    selectStrId(parent, position, places);
                    Log.e("Goto", placesStrSelected[3] + ", " + placesStrSelected[2] + ", " + placesStrSelected[1] + ", " + placesStrSelected[0]);
                }
            });
        }
    }

    public class TakeClientToTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId, mCountryId, mRegionId, mCityId, mAddressId;

        TakeClientToTask(Long taxiId, Long countryId, Long regionId, Long cityId, Long addressId) {
            mTaxiId = taxiId;
            mCountryId = countryId;
            mRegionId = regionId;
            mCityId = cityId;
            mAddressId = addressId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/countries/" + mCountryId + "/regions/" + mRegionId + "/cities/" + mCityId + "/addresses/" + mAddressId);
            put.setHeader("content-type", "application/json");
            try {
                Log.e("GoTo", "taxis/" + mTaxiId + "/countries/" + mCountryId + "/regions/" + mRegionId + "/cities/" + mCityId + "/addresses/" + mAddressId);
                HttpResponse resp = new DefaultHttpClient().execute(put);
                String respStr = EntityUtils.toString(resp.getEntity());
                travelId = Long.valueOf(respStr);
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
