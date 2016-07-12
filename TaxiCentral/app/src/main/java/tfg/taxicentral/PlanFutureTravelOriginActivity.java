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

    private int flag = 25;
    private String[] placesString;
    private String[] placesStrSelected = new String[8];
    private Long[] placesIdSelected = new Long[8];
    private GetPlacesTask mGetPlacesTask = null;
    private HashMap<String, Long> countries = new HashMap<>(), regions = new HashMap<>(), cities = new HashMap<>(), addresses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_future_travel_origin);

        //TODO Poner de forma elegante los predefinidos
        AutoCompleteTextView actv;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginCountries);
        placesStrSelected[0]="ESPAÑA";
        placesIdSelected[0]=(long)1;
        actv.setText("ESPAÑA");
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginRegions);
        actv.setText("A Coruña");
        placesStrSelected[1]="A Coruña";
        placesIdSelected[1]=(long)9;
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginCities);
        actv.setText("A Coruña");
        placesStrSelected[2]="A Coruña";
        placesIdSelected[2]=(long)6944;
        mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[2], "addressId", addresses, R.id.autoCompleteTextViewOriginAddresses);
        flag = 3;
        mGetPlacesTask.execute((Void) null);

        AutoCompleteTextView actvC = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewOriginCountries);
        actvC.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO hay que darle dos veces para que cargue
                Log.e("HOLAA", "HOLAAAAAAA");
                mGetPlacesTask = new GetPlacesTask("countries", (long) 0, "countryId", countries, R.id.autoCompleteTextViewOriginCountries);
                flag = 0;
                mGetPlacesTask.execute((Void) null);
            }
        });

        Button mPlanFutureTravelOriginButton = (Button) findViewById(R.id.planFutureTravelOriginButton);
        mPlanFutureTravelOriginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlanFutureTravelDestinationActivity.class);
                intent.putExtra("placesIdSelectedOrigin0", placesIdSelected[0]);
                intent.putExtra("placesIdSelectedOrigin1", placesIdSelected[1]);
                intent.putExtra("placesIdSelectedOrigin2", placesIdSelected[2]);
                intent.putExtra("placesIdSelectedOrigin3", placesIdSelected[3]);
                startActivity(intent);
                finish();
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
                    mGetPlacesTask = new GetPlacesTask("countries/", placesIdSelected[flag], "regionId", regions, R.id.autoCompleteTextViewOriginRegions);
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
                    mGetPlacesTask = new GetPlacesTask("regions/", placesIdSelected[flag], "cityId", cities, R.id.autoCompleteTextViewOriginCities);
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
                    mGetPlacesTask = new GetPlacesTask("cities/", placesIdSelected[flag], "addressId", addresses, R.id.autoCompleteTextViewOriginAddresses);
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

}
