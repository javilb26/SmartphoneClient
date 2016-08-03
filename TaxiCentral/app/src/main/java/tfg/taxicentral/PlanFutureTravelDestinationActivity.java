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

    private int flag = 25;
    private String[] placesString;
    private String[] placesStrSelected = new String[8];
    private Long[] placesIdSelected = new Long[8];
    private GetPlacesTask mGetPlacesTask = null;
    private PlanFutureTravelTask mPlanFutureTravelTask = null;
    private HashMap<String, Long> countries = new HashMap<>(), regions = new HashMap<>(), cities = new HashMap<>(), addresses = new HashMap<>();

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_future_travel_destination);

        //TODO Poner de forma elegante los predefinidos
        AutoCompleteTextView actv;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationCountries);
        placesStrSelected[0]="ESPAÑA";
        placesIdSelected[0]=(long)1;
        actv.setText("ESPAÑA");
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationRegions);
        actv.setText("A Coruña");
        placesStrSelected[1]="A Coruña";
        placesIdSelected[1]=(long)9;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationCities);
        actv.setText("A Coruña");
        placesStrSelected[2]="A Coruña";
        placesIdSelected[2]=(long)6944;
        mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[2], "addressId", addresses, R.id.autoCompleteTextViewDestinationAddresses);
        flag = 3;
        mGetPlacesTask.execute((Void) null);

        AutoCompleteTextView actvC = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestinationCountries);
        actvC.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO hay que darle dos veces para que cargue
                Log.e("HOLAA", "HOLAAAAAAA");
                mGetPlacesTask = new GetPlacesTask("countries", (long) 0, "countryId", countries, R.id.autoCompleteTextViewDestinationCountries);
                flag = 0;
                mGetPlacesTask.execute((Void) null);
            }
        });

        Button mPlanFutureTravelButton = (Button) findViewById(R.id.planFutureTravelButton);
        mPlanFutureTravelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String url = placesStrSelected[3] + ", " + placesStrSelected[2] + ", " + placesStrSelected[1] + ", " + placesStrSelected[0];
                date = mDay + "-" + mMonth + "-" + mYear + " " + mHour + ":" + mMinute;
                //TODO Rectificar los tiempos, se ejecuta el travelId antes que la creacion del travel -> asegurarse
                mPlanFutureTravelTask = new PlanFutureTravelTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0),
                        getIntent().getLongExtra("placesIdSelectedOrigin0",0), getIntent().getLongExtra("placesIdSelectedOrigin1",0), getIntent().getLongExtra("placesIdSelectedOrigin2",0),
                        getIntent().getLongExtra("placesIdSelectedOrigin3",0), placesIdSelected[0], placesIdSelected[1], placesIdSelected[2], placesIdSelected[3], date);
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
            if ((mId == 0)) {
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
        actvC.setThreshold(1);//will start working from first character
        actvC.setAdapter(adapterC);//setting the adapter data into the AutoCompleteTextView
        if (flag == 0) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("countries/", placesIdSelected[flag], "regionId", regions, R.id.autoCompleteTextViewDestinationRegions);
                    flag = 1;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 1) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("regions/", placesIdSelected[flag], "cityId", cities, R.id.autoCompleteTextViewDestinationCities);
                    flag = 2;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 2) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectStrId(parent, position, places);
                    mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[flag], "addressId", addresses, R.id.autoCompleteTextViewDestinationAddresses);
                    flag = 3;
                    mGetPlacesTask.execute((Void) null);
                }
            });
        }
        if (flag == 3) {
            actvC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectStrId(parent, position, places);
                }
            });
        }

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
                Log.e("PlanFutureTravel", "oaid: " + mOriginAddressId.toString());
                object.put("destinationCountryId", mDestinationCountryId);
                object.put("destinationRegionId", mDestinationRegionId);
                object.put("destinationCityId", mDestinationCityId);
                object.put("destinationAddressId", mDestinationAddressId);
                object.put("date", mDate);
                StringEntity entity = new StringEntity(object.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                Log.e("PlanFutureTravel", object.toString());
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
