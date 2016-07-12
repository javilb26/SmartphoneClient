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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
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

public class FutureTravelOptionsActivity extends AppCompatActivity {

    private TakeClientToFromFutureTravelTask mTakeClientToFromFutureTravelTask = null;
    private Long travelId = (long) 0;
    private CancelFutureTravelTask mCancelFutureTravelTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future_travel_options);

        TextView item = (TextView) findViewById(R.id.itemTextView);
        item.setText(getIntent().getStringExtra("item"));

        Button mGoToButton = (Button) findViewById(R.id.goToButton);
        mGoToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getIntent().getStringExtra("originAddressStr") + ", " + getIntent().getStringExtra("originCityStr") + ", " + getIntent().getStringExtra("originRegionStr") + ", " + getIntent().getStringExtra("originCountryStr");
                Log.e("GoTo", url);
                //TODO Rectificar los tiempos, se ejecuta el travelId antes que la creacion del travel -> asegurarse
                mTakeClientToFromFutureTravelTask = new TakeClientToFromFutureTravelTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0),
                        getIntent().getLongExtra("originCountryId",0), getIntent().getLongExtra("originRegionId",0), getIntent().getLongExtra("originCityId", 0), getIntent().getLongExtra("originAddressId",0),
                        getIntent().getLongExtra("destinationCountryId",0), getIntent().getLongExtra("destinationRegionId",0), getIntent().getLongExtra("destinationCityId",0), getIntent().getLongExtra("destinationAddressId",0));
                mTakeClientToFromFutureTravelTask.execute((Void) null);

                if (mCancelFutureTravelTask != null) {
                    return;
                }
                mCancelFutureTravelTask = new CancelFutureTravelTask(getIntent().getLongExtra("futureTravelId",0));
                mCancelFutureTravelTask.execute((Void) null);
                Toast.makeText(getApplicationContext(), "FutureTravel deleted", Toast.LENGTH_SHORT).show();

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

        Button mDeleteButton = (Button) findViewById(R.id.deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCancelFutureTravelTask != null) {
                    return;
                }
                mCancelFutureTravelTask = new CancelFutureTravelTask(getIntent().getLongExtra("futureTravelId",0));
                mCancelFutureTravelTask.execute((Void) null);
                Toast.makeText(getApplicationContext(), "FutureTravel cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public class TakeClientToFromFutureTravelTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId, mOriginCountryId, mOriginRegionId, mOriginCityId, mOriginAddressId, mDestinationCountryId, mDestinationRegionId, mDestinationCityId, mDestinationAddressId;

        TakeClientToFromFutureTravelTask(Long taxiId, Long originCountryId, Long originRegionId, Long originCityId, Long originAddressId, Long destinationCountryId, Long destinationRegionId, Long destinationCityId, Long destinationAddressId) {
            mTaxiId = taxiId;
            mOriginCountryId = originCountryId;
            mOriginRegionId = originRegionId;
            mOriginCityId = originCityId;
            mOriginAddressId = originAddressId;
            mDestinationCountryId = destinationCountryId;
            mDestinationRegionId = destinationRegionId;
            mDestinationCityId = destinationCityId;
            mDestinationAddressId = destinationAddressId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/origincountries/" + mOriginCountryId + "/originregions/" + mOriginRegionId
                    + "/origincities/" + mOriginCityId + "/originaddresses/" + mOriginAddressId + "/destinationcountries/" + mDestinationCountryId + "/destinationregions/" + mDestinationRegionId
                    + "/destinationcities/" + mDestinationCityId + "/destinationaddresses/" + mDestinationAddressId);
            put.setHeader("content-type", "application/json");
            try {
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

    public class CancelFutureTravelTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mFutureTravelId;

        CancelFutureTravelTask(Long futureTravelId) {
            mFutureTravelId = futureTravelId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpDelete delete = new HttpDelete(getString(R.string.ip) + "futuretravels/" + mFutureTravelId);
            delete.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(delete);
                String respStr = EntityUtils.toString(resp.getEntity());
                if (!respStr.equals("true"))
                    return false;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
