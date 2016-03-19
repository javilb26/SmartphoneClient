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

    private GetCountriesTask mGetCountriesTask = null;
    String[] countriesString;
    HashMap<String, Long> countries = new HashMap<>();
    String countrySelected;

    private GetRegionsTask mGetRegionsTask = null;
    String[] regionsString = null;
    HashMap<String, Long> regions = new HashMap<>();
    String regionSelected;

    private GetCitiesTask mGetCitiesTask = null;
    String[] citiesString;
    HashMap<String, Long> cities = new HashMap<>();
    String citySelected;

    private GetAddressesTask mGetAddressesTask = null;
    String[] addresses;
    String addressSelected;

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
                    List<Address> geocodeMatches = new Geocoder(getApplicationContext()).getFromLocationName(url, 1);
                    if ((geocodeMatches==null)||(!geocodeMatches.isEmpty())) {
                        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("lat", geocodeMatches.get(0).getLatitude());
                        intent.putExtra("lng", geocodeMatches.get(0).getLongitude());
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

    public void createInstanceArrayAdapterCountries() {

        Iterator it = countries.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            countriesString[i] = e.getKey().toString();
            i+=1;
        }

        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterC = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, countriesString);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvC = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCountries);
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                countrySelected = (String) parent.getItemAtPosition(position);
                Log.e("countryId: ",countries.get(countrySelected).toString());
                mGetRegionsTask = new GetRegionsTask(countries.get(countrySelected));
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
        ArrayAdapter<String> adapterR = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, regionsString);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvR = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRegions);
        actvR.setThreshold(1);//will start working from first character
        actvR.setAdapter(adapterR);//setting the adapter data into the AutoCompleteTextView
        actvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                regionSelected = (String) parent.getItemAtPosition(position);
                Log.e("regionId: ",regions.get(regionSelected).toString());
                mGetCitiesTask = new GetCitiesTask(regions.get(regionSelected));
                mGetCitiesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterCities() {

        Iterator it = cities.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            citiesString[i] = e.getKey().toString();
            i+=1;
        }

        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterCi = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, citiesString);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvCi = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCities);
        actvCi.setThreshold(1);//will start working from first character
        actvCi.setAdapter(adapterCi);//setting the adapter data into the AutoCompleteTextView
        actvCi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                citySelected = (String) parent.getItemAtPosition(position);
                Log.e("cityId: ",cities.get(citySelected).toString());
                mGetAddressesTask = new GetAddressesTask(cities.get(citySelected));
                mGetAddressesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterAddresses() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterA = new ArrayAdapter<>
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
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/country");
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                countriesString = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    Long countryId = (long) obj.getInt("countryId");
                    String name = obj.getString("name");
                    countries.put(name, countryId);
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
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

        private final Long mCountryId;

        GetRegionsTask(Long countryId) {
            mCountryId = countryId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
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
                    Long regionId = (long) obj.getInt("regionId");
                    String name = obj.getString("name");
                    regions.put(name, regionId);
                    //regions[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
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

        private final Long mRegionId;

        GetCitiesTask(Long regionId) {
            mRegionId = regionId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.34:8080/SpringMVCHibernate/region/" + mRegionId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                citiesString = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    Long cityId = (long) obj.getInt("cityId");
                    String name = obj.getString("name");
                    cities.put(name, cityId);
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
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

        private final Long mCityId;

        GetAddressesTask(Long cityId) {
            mCityId = cityId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
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
                    String name = obj.getString("name");
                    addresses[i] = name;
                }
            }
            catch(Exception ex) {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapterAddresses();
        }

    }

}
