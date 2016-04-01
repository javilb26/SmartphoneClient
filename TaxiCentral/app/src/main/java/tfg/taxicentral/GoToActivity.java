package tfg.taxicentral;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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

public class GoToActivity extends ActionBarActivity {

    private int flag = 0;
    private String[] placesString;
    private GetCountriesTask mGetCountriesTask = null;
    private GetRegionsTask mGetRegionsTask = null;
    private GetCitiesTask mGetCitiesTask = null;
    private GetAddressesTask mGetAddressesTask = null;
    private TakeClientToTask mTakeClientToTask = null;
    private HashMap<String, Long> countries = new HashMap<>(), regions = new HashMap<>(), cities = new HashMap<>(), addresses = new HashMap<>();
    private String countryStrSelected, regionStrSelected, cityStrSelected, addressStrSelected;
    private Long countryIdSelected, regionIdSelected, cityIdSelected, addressIdSelected;
    private Long clientId = (long) 0;
    private Long travelId = (long) 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to);

        mGetCountriesTask = new GetCountriesTask();
        mGetCountriesTask.execute((Void) null);

        Button mGoToButton = (Button) findViewById(R.id.goToButton);
        mGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = addressStrSelected + ", " + cityStrSelected + ", " + regionStrSelected + ", " + countryStrSelected;
                //TODO Cuidado con los tiempos, puede que se ejecute el travelId antes que la creacion del travel -> asegurarse
                mTakeClientToTask = new TakeClientToTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), clientId, countryIdSelected, regionIdSelected, cityIdSelected, addressIdSelected);
                mTakeClientToTask.execute((Void) null);
                try {
                    List<Address> geocodeMatches = new Geocoder(getApplicationContext()).getFromLocationName(url, 1);
                    if ((geocodeMatches == null) || (!geocodeMatches.isEmpty())) {
                        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("lat", geocodeMatches.get(0).getLatitude());
                        intent.putExtra("lng", geocodeMatches.get(0).getLongitude());
                        intent.putExtra("travelId", travelId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Destino no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "No se pudo convertir la direccion", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public void createInstanceArrayAdapter(final HashMap<String, Long> places, int autoCompleteTextView) {
        ArrayAdapter<String> adapterC = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, iterator(places));

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvC = (AutoCompleteTextView) findViewById(autoCompleteTextView);
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        if (flag==1) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    countryStrSelected = (String) parent.getItemAtPosition(position);
                    countryIdSelected = places.get(countryStrSelected);
                    Log.e("countryId: ", places.get(countryStrSelected).toString());
                    mGetRegionsTask = new GetRegionsTask(places.get(countryStrSelected));
                    mGetRegionsTask.execute((Void) null);
                }
            });
        }
        if (flag==2) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    regionStrSelected = (String) parent.getItemAtPosition(position);
                    regionIdSelected = places.get(regionStrSelected);
                    Log.e("regionId: ", places.get(regionStrSelected).toString());
                    mGetCitiesTask = new GetCitiesTask(places.get(regionStrSelected));
                    mGetCitiesTask.execute((Void) null);
                }
            });
        }
        if (flag==3) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    cityStrSelected = (String) parent.getItemAtPosition(position);
                    cityIdSelected = places.get(cityStrSelected);
                    Log.e("cityId: ", places.get(cityStrSelected).toString());
                    mGetAddressesTask = new GetAddressesTask(places.get(cityStrSelected));
                    mGetAddressesTask.execute((Void) null);
                }
            });
        }
        if (flag==4) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    addressStrSelected = (String) parent.getItemAtPosition(position);
                    addressIdSelected = places.get(addressStrSelected);
                }
            });
        }
    }

    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
            flag=1;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "country");
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    countries.put(obj.getString("name"), (long) obj.getInt("countryId"));
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter(countries, R.id.autoCompleteTextViewCountries);
        }

    }

    public class GetRegionsTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mCountryId;

        GetRegionsTask(Long countryId) {
            flag=2;
            mCountryId = countryId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "country/" + mCountryId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    regions.put(obj.getString("name"), (long) obj.getInt("regionId"));
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter(regions, R.id.autoCompleteTextViewRegions);
        }

    }

    public class GetCitiesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mRegionId;

        GetCitiesTask(Long regionId) {
            flag=3;
            mRegionId = regionId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "region/" + mRegionId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    cities.put(obj.getString("name"), (long) obj.getInt("cityId"));
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter(cities, R.id.autoCompleteTextViewCities);
        }

    }

    public class GetAddressesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mCityId;

        GetAddressesTask(Long cityId) {
            flag=4;
            mCityId = cityId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "city/" + mCityId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    addresses.put(obj.getString("name"), (long) obj.getInt("addressId"));
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter(addresses, R.id.autoCompleteTextViewAddresses);
        }

    }

    public class TakeClientToTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final Long mClientId;
        private final Long mCountryId;
        private final Long mRegionId;
        private final Long mCityId;
        private final Long mAddressId;

        TakeClientToTask(Long taxiId, Long clientId, Long countryId, Long regionId, Long cityId, Long addressId) {
            mTaxiId = taxiId;
            mClientId = clientId;
            mCountryId = countryId;
            mRegionId = regionId;
            mCityId = cityId;
            mAddressId = addressId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxi/" + mTaxiId + "/client/" + mClientId + "/country/" + mCountryId + "/region/" + mRegionId + "/city/" + mCityId + "/address/" + mAddressId);
            put.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(put);
                String respStr = EntityUtils.toString(resp.getEntity());
                if (!respStr.equals("true"))
                    return false;
                travelId = Long.valueOf(respStr);
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
