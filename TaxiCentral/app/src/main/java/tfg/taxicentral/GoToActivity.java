package tfg.taxicentral;

import android.content.Intent;
import android.graphics.Color;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GoToActivity extends ActionBarActivity {

    private GetCountriesTask mGetCountriesTask = null;
    String[] countries;
    private GetRegionsTask mGetRegionsTask = null;
    String[] regions;
    private GetCitiesTask mGetCitiesTask = null;
    String[] cities;
    private GetAddressesTask mGetAddressesTask = null;
    String[] addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to);

        mGetCountriesTask = new GetCountriesTask();
        mGetCountriesTask.execute((Void) null);

        if (mGetCountriesTask!=null){
            mGetRegionsTask = new GetRegionsTask(new Long(1));
            mGetRegionsTask.execute((Void) null);
        }

        //TODO Completar con los task city y address

        Button mGoToButton = (Button) findViewById(R.id.goToButton);
        mGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO (Mirar si hacer antes la conversion o despues) Recuperar datos goto y enviar a NavigationActivity
                Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                startActivity(intent);
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
    }

    public void createInstanceArrayAdapterRegions() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterR = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, regions);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvR = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRegions);
        actvR.setThreshold(1);//will start working from first character
        actvR.setAdapter(adapterR);//setting the adapter data into the AutoCompleteTextView
    }

    public void createInstanceArrayAdapterCities() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterCi = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, cities);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvCi = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCities);
        actvCi.setThreshold(1);//will start working from first character
        actvCi.setAdapter(adapterCi);//setting the adapter data into the AutoCompleteTextView
    }

    public void createInstanceArrayAdapterAddresses() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapterA = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, addresses);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actvA = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewAddresses);
        actvA.setThreshold(1);//will start working from first character
        actvA.setAdapter(adapterA);//setting the adapter data into the AutoCompleteTextView
    }

    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://10.0.2.2:8080/SpringMVCHibernate/country");
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                Log.e("Error",respStr);
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
            HttpGet get = new HttpGet("http://10.0.2.2:8080/SpringMVCHibernate/country/" + mCountryId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                Log.e("Error Background", respStr);
                JSONArray respJSON = new JSONArray(respStr);
                regions = new String[respJSON.length()];
                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    //Long countryId = new Long(obj.getInt("countryId"));
                    String name = obj.getString("name");
                    regions[i] = name;
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
            HttpGet get = new HttpGet("http://10.0.2.2:8080/SpringMVCHibernate/region/" + mRegionId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                Log.e("Error Background", respStr);
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
            HttpGet get = new HttpGet("http://10.0.2.2:8080/SpringMVCHibernate/city/" + mCityId);
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                Log.e("Error Background", respStr);
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
