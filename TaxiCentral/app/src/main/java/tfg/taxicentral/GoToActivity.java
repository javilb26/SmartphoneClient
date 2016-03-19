package tfg.taxicentral;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GoToActivity extends ActionBarActivity {

    private GetCountriesTask mGetCountriesTask = null;
    String[] countries;
    String countrySelected;
    private GetRegionsTask mGetRegionsTask = null;
    String[] regionsString = null;
    HashMap<String, Long> regions = new HashMap<String, Long>();
    String regionSelected;
    private GetCitiesTask mGetCitiesTask = null;
    String[] cities;
    String citySelected;
    private GetAddressesTask mGetAddressesTask = null;
    String[] addresses;
    String addressSelected;
    List<Address> geocodeMatches = null;
    Double lat, lng;


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
                String url = addressSelected + ", " + citySelected + ", " + regionSelected + ", " + countrySelected;
                try {
                    geocodeMatches =
                            new Geocoder(getApplicationContext()).getFromLocationName(
                                    url, 1);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (!geocodeMatches.isEmpty()) {
                    lat = geocodeMatches.get(0).getLatitude();
                    lng = geocodeMatches.get(0).getLongitude();
                    Log.e("lat", lat.toString());
                    Log.e("lng", lng.toString());
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    startActivity(intent);
                }
                Toast.makeText(getApplicationContext(), "Destino no encontrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createInstanceArrayAdapterCountries() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterC = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, countries);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvC = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCountries);
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                countrySelected = (String) parent.getItemAtPosition(position);
                mGetRegionsTask = new GetRegionsTask(new Long(1));
                mGetRegionsTask.execute((Void) null);
            }
        });

    }

    public void createInstanceArrayAdapterRegions() {

        Iterator it = regions.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            regionsString[i] = e.getKey().toString();
            i+=1;
        }

        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterR = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, regionsString);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvR = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRegions);
        actvR.setThreshold(1);//will start working from first character
        actvR.setAdapter(adapterR);//setting the adapter data into the AutoCompleteTextView
        actvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                regionSelected = (String) parent.getItemAtPosition(position);
                Log.e("Clave 9 == ", regions.get(regionSelected).toString());
                mGetCitiesTask = new GetCitiesTask(regions.get(regionSelected));
                mGetCitiesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterCities() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterCi = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, cities);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvCi = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCities);
        actvCi.setThreshold(1);//will start working from first character
        actvCi.setAdapter(adapterCi);//setting the adapter data into the AutoCompleteTextView
        actvCi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                citySelected = (String) parent.getItemAtPosition(position);
                mGetAddressesTask = new GetAddressesTask(new Long(6944));
                mGetAddressesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterAddresses() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterA = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, addresses);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvA = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewAddresses);
        actvA.setThreshold(1);//will start working from first character
        actvA.setAdapter(adapterA);//setting the adapter data into the AutoCompleteTextView
        actvA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addressSelected = (String) parent.getItemAtPosition(position);
            }
        });
    }

    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/country");
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                countries = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    //Long countryId = new Long(obj.getInt("countryId"));
                    String name = obj.getString("name");
                    countries[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterCountries();
        }

    }

    public class GetRegionsTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mCountryId;

        GetRegionsTask(Long countryId) {
            mCountryId = countryId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/country/" + mCountryId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                regionsString = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    Long regionId = new Long(obj.getInt("regionId"));
                    String name = obj.getString("name");
                    regions.put(name, regionId);
                    //regions[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterRegions();
        }

    }

    public class GetCitiesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mRegionId;

        GetCitiesTask(Long regionId) {
            mRegionId = regionId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/region/" + mRegionId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                cities = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    //Long countryId = new Long(obj.getInt("countryId"));
                    String name = obj.getString("name");
                    cities[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterCities();
        }

    }

    public class GetAddressesTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mCityId;

        GetAddressesTask(Long cityId) {
            mCityId = cityId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/city/" + mCityId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                addresses = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    //Long countryId = new Long(obj.getInt("countryId"));
                    String name = obj.getString("name");
                    addresses[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterAddresses();
        }

    }

}
