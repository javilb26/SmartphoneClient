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

    private GetCountriesTask mGetCountriesTask = null;
    private String[] placesString;
    private HashMap<String, Long> countries = new HashMap<>();
    private String countryStrSelected;
    private Long countryIdSelected;

    private GetRegionsTask mGetRegionsTask = null;
    private String[] regionsString = null;
    private HashMap<String, Long> regions = new HashMap<>();
    private String regionStrSelected;
    private Long regionIdSelected;

    private GetCitiesTask mGetCitiesTask = null;
    private String[] citiesString;
    private HashMap<String, Long> cities = new HashMap<>();
    private String cityStrSelected;
    private Long cityIdSelected;

    private GetAddressesTask mGetAddressesTask = null;
    private String[] addressesString;
    private HashMap<String, Long> addresses = new HashMap<>();
    private String addressStrSelected;
    private Long addressIdSelected;

    private TakeClientToTask mTakeClientToTask = null;
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
                mTakeClientToTask = new TakeClientToTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), (long) clientId, countryIdSelected, regionIdSelected, cityIdSelected, addressIdSelected);
                mTakeClientToTask.execute((Void) null);
                try {
                    List<Address> geocodeMatches = new Geocoder(getApplicationContext()).getFromLocationName(url, 1);
                    if ((geocodeMatches==null)||(!geocodeMatches.isEmpty())) {
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

    private String[] iterator (HashMap places) {
        Iterator it = places.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            placesString[i] = e.getKey().toString();
            i+=1;
        }
        return placesString;
    }

    public void createInstanceArrayAdapterCountries() {
        ArrayAdapter<String> adapterC = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, iterator(countries));

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvC = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCountries);
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                countryStrSelected = (String) parent.getItemAtPosition(position);
                countryIdSelected = countries.get(countryStrSelected);
                Log.e("countryId: ",countries.get(countryStrSelected).toString());
                mGetRegionsTask = new GetRegionsTask(countries.get(countryStrSelected));
                mGetRegionsTask.execute((Void) null);
            }
        });

    }

    public void createInstanceArrayAdapterRegions() {
        ArrayAdapter<String> adapterR = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, iterator(regions));

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvR = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRegions);
        actvR.setThreshold(1);//will start working from first character
        actvR.setAdapter(adapterR);//setting the adapter data into the AutoCompleteTextView
        actvR.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                regionStrSelected = (String) parent.getItemAtPosition(position);
                regionIdSelected = regions.get(regionStrSelected);
                Log.e("regionId: ",regions.get(regionStrSelected).toString());
                mGetCitiesTask = new GetCitiesTask(regions.get(regionStrSelected));
                mGetCitiesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterCities() {
        ArrayAdapter<String> adapterCi = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, iterator(cities));

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvCi = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCities);
        actvCi.setThreshold(1);//will start working from first character
        actvCi.setAdapter(adapterCi);//setting the adapter data into the AutoCompleteTextView
        actvCi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityStrSelected = (String) parent.getItemAtPosition(position);
                cityIdSelected = cities.get(cityStrSelected);
                Log.e("cityId: ",cities.get(cityStrSelected).toString());
                mGetAddressesTask = new GetAddressesTask(cities.get(cityStrSelected));
                mGetAddressesTask.execute((Void) null);
            }
        });
    }

    public void createInstanceArrayAdapterAddresses() {
        ArrayAdapter<String> adapterA = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item, iterator(addresses));

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvA = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewAddresses);
        actvA.setThreshold(1);//will start working from first character
        actvA.setAdapter(adapterA);//setting the adapter data into the AutoCompleteTextView
        actvA.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addressStrSelected = (String) parent.getItemAtPosition(position);
                addressIdSelected = addresses.get(addressStrSelected);
            }
        });
    }

    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(getString(R.string.ip)+"country");
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
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
            HttpGet get = new HttpGet(getString(R.string.ip)+"country/" + mCountryId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
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
            HttpGet get = new HttpGet(getString(R.string.ip)+"region/" + mRegionId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
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
            HttpGet get = new HttpGet(getString(R.string.ip)+"city/" + mCityId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                placesString = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    Long addressId = (long) obj.getInt("addressId");
                    String name = obj.getString("name");
                    addresses.put(name, addressId);
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
            HttpClient httpClient = new DefaultHttpClient();
            HttpPut put = new HttpPut(getString(R.string.ip)+"taxi/" + mTaxiId + "/client/" + mClientId + "/country/" + mCountryId + "/region/" + mRegionId + "/city/" + mCityId + "/address/" + mAddressId);
            put.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(put);
                String respStr = EntityUtils.toString(resp.getEntity());
                if(!respStr.equals("true"))
                    return false;
                travelId = Long.valueOf(respStr);
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return true;
        }

    }

}
