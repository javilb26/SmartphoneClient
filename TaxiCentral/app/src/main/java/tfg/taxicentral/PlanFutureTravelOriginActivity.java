package tfg.taxicentral;

import android.content.Intent;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlanFutureTravelOriginActivity extends AppCompatActivity {

    private String countryStrSelected, regionStrSelected, cityStrSelected, addressStrSelected;
    private Long countryIdSelected, regionIdSelected, cityIdSelected, addressIdSelected;
    private String[] countriesString, regionsString, citiesString, addressesString;
    private HashMap<String, Long> countries = new HashMap<>(), regions = new HashMap<>(), cities = new HashMap<>(), addresses = new HashMap<>();
    private Long travelId = (long) 0;
    private GetCountriesTask mGetCountriesTask = null;
    private GetRegionsTask mGetRegionsTask = null;
    private GetCitiesTask mGetCitiesTask = null;
    private GetAddressesTask mGetAddressesTask = null;
    AutoCompleteTextView actvCountries;
    AutoCompleteTextView actvRegions;
    AutoCompleteTextView actvCities;
    AutoCompleteTextView actvAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_future_travel_origin);

        actvCountries = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginCountries);
        actvRegions = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginRegions);
        actvCities = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginCities);
        actvAddresses = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginAddresses);

        mGetCountriesTask = new GetCountriesTask();
        mGetCountriesTask.execute((Void) null);
        countryStrSelected=getString(R.string.spain);
        countryIdSelected=(long)1;
        actvCountries.setText(getString(R.string.spain));

        mGetRegionsTask = new GetRegionsTask(countryIdSelected);
        mGetRegionsTask.execute((Void) null);
        actvRegions.setText(getString(R.string.corunna));
        regionStrSelected=getString(R.string.corunna);
        regionIdSelected=(long)9;

        mGetCitiesTask = new GetCitiesTask(regionIdSelected);
        mGetCitiesTask.execute((Void) null);
        actvCities.setText(getString(R.string.corunna));
        cityStrSelected=getString(R.string.corunna);
        cityIdSelected=(long)6944;

        mGetAddressesTask = new GetAddressesTask(cityIdSelected);
        mGetAddressesTask.execute((Void) null);

        Button mPlanFutureTravelOriginButton = (Button) findViewById(R.id.planFutureTravelOriginButton);
        mPlanFutureTravelOriginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlanFutureTravelDestinationActivity.class);
                intent.putExtra("placesIdSelectedOrigin0", countryIdSelected);
                intent.putExtra("placesIdSelectedOrigin1", regionIdSelected);
                intent.putExtra("placesIdSelectedOrigin2", cityIdSelected);
                intent.putExtra("placesIdSelectedOrigin3", addressIdSelected);
                startActivity(intent);
                finish();
            }
        });
    }

    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "countries");
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                countriesString = null;
                countriesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    countries.put(obj.getString("name"), (long) obj.getInt("countryId"));
                    countriesString[i] = obj.getString("name");
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterCountries();
        }

    }

    public class GetRegionsTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mId;

        GetRegionsTask(Long id) {
            mId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "countries/" + mId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                regionsString = null;
                regionsString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    regions.put(obj.getString("name"), (long) obj.getInt("regionId"));
                    regionsString[i] = obj.getString("name");
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterRegions();
        }

    }

    public class GetCitiesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mId;

        GetCitiesTask(Long id) {
            mId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "regions/" + mId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                citiesString = null;
                citiesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    cities.put(obj.getString("name"), (long) obj.getInt("cityId"));
                    citiesString[i] = obj.getString("name");
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterCities();
        }

    }

    public class GetAddressesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mId;

        GetAddressesTask(Long id) {
            mId = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "cities/" + mId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                addressesString = null;
                addressesString = new String[respJSON.length()];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    addresses.put(obj.getString("name"), (long) obj.getInt("addressId"));
                    addressesString[i] = obj.getString("name");
                }
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterAddresses();
        }

    }

    public void createInstanceArrayAdapterCountries() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, countriesString);
        actvCountries.setThreshold(1);//will start working from first character
        actvCountries.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

        actvCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvRegions.setText("");
                actvCities.setText("");
                actvAddresses.setText("");
                countryStrSelected = (String) parent.getItemAtPosition(position);
                countryIdSelected = countries.get(countryStrSelected);
                mGetRegionsTask = new GetRegionsTask(countryIdSelected);
                mGetRegionsTask.execute((Void) null);
            }
        });

    }

    public void createInstanceArrayAdapterRegions() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, regionsString);
        actvRegions.setThreshold(1);//will start working from first character
        actvRegions.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

        actvRegions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvCities.setText("");
                actvAddresses.setText("");
                regionStrSelected = (String) parent.getItemAtPosition(position);
                regionIdSelected = regions.get(regionStrSelected);
                mGetCitiesTask = new GetCitiesTask(regionIdSelected);
                mGetCitiesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterCities() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, citiesString);
        actvCities.setThreshold(1);//will start working from first character
        actvCities.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView

        actvCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvAddresses.setText("");
                cityStrSelected = (String) parent.getItemAtPosition(position);
                cityIdSelected = cities.get(cityStrSelected);
                mGetAddressesTask = new GetAddressesTask(cityIdSelected);
                mGetAddressesTask.execute((Void) null);
            }
        });

    }

    public void createInstanceArrayAdapterAddresses() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, addressesString);
        actvAddresses.setThreshold(1);//will start working from first character
        actvAddresses.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actvAddresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addressStrSelected = (String) parent.getItemAtPosition(position);
                addressIdSelected = addresses.get(addressStrSelected);
            }
        });
    }

}
