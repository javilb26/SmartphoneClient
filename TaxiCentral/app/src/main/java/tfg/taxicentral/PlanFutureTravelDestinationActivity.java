package tfg.taxicentral;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlanFutureTravelDestinationActivity extends AppCompatActivity implements
        View.OnClickListener {

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
    private PlanFutureTravelTask mPlanFutureTravelTask = null;

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_future_travel_destination);

        actvCountries = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationCountries);
        actvRegions = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationRegions);
        actvCities = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationCities);
        actvAddresses = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationAddresses);

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

        Button mPlanFutureTravelButton = (Button) findViewById(R.id.planFutureTravelButton);
        mPlanFutureTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date = mDay + "-" + mMonth + "-" + mYear + " " + mHour + ":" + mMinute;
                mPlanFutureTravelTask = new PlanFutureTravelTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0),
                        getIntent().getLongExtra("placesIdSelectedOrigin0",0), getIntent().getLongExtra("placesIdSelectedOrigin1",0), getIntent().getLongExtra("placesIdSelectedOrigin2",0),
                        getIntent().getLongExtra("placesIdSelectedOrigin3",0), countryIdSelected, regionIdSelected, cityIdSelected, addressIdSelected, date);
                mPlanFutureTravelTask.execute((Void) null);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Future travel created", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        txtDate=(EditText)findViewById(R.id.in_date);
        txtTime=(EditText)findViewById(R.id.in_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            txtTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
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

    public class PlanFutureTravelTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId, mOriginCountryId, mOriginRegionId, mOriginCityId, mOriginAddressId, mDestinationCountryId, mDestinationRegionId, mDestinationCityId, mDestinationAddressId;
        private final String mDate;

        PlanFutureTravelTask(Long taxiId, Long originCountryId, Long originRegionId, Long originCityId, Long originAddressId, Long destinationCountryId, Long destinationRegionId, Long destinationCityId, Long destinationAddressId, String date) {
            mTaxiId = taxiId;
            mOriginCountryId = originCountryId;
            mOriginRegionId = originRegionId;
            mOriginCityId = originCityId;
            mOriginAddressId = originAddressId;
            mDestinationCountryId = destinationCountryId;
            mDestinationRegionId = destinationRegionId;
            mDestinationCityId = destinationCityId;
            mDestinationAddressId = destinationAddressId;
            mDate = date;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(getString(R.string.ip) + "futuretravels");
            post.setHeader("content-type", "application/json");
            try {
                JSONObject object = new JSONObject();
                object.put("taxiId", mTaxiId);
                object.put("originCountryId", mOriginCountryId);
                object.put("originRegionId", mOriginRegionId);
                object.put("originCityId", mOriginCityId);
                object.put("originAddressId", mOriginAddressId);
                object.put("destinationCountryId", mDestinationCountryId);
                object.put("destinationRegionId", mDestinationRegionId);
                object.put("destinationCityId", mDestinationCityId);
                object.put("destinationAddressId", mDestinationAddressId);
                object.put("date", mDate);
                StringEntity entity = new StringEntity(object.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());
                if(!respStr.equals("true"))
                    return false;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
