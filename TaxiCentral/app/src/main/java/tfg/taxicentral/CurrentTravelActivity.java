package tfg.taxicentral;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by Javi on 14/07/2016.
 */
public class CurrentTravelActivity extends AppCompatActivity {

    private TaxiTask mTaxiTask = null;
    private int taxiFlag = 0;
    private String actualState = "";
    private String futureState = "";
    private String originCountryClient = "";
    private String originRegionClient = "";
    private String originCityClient = "";
    private String originAddressClient = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_travel);

        mTaxiTask = new TaxiTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
        mTaxiTask.execute((Void) null);
        while (taxiFlag == 0) {
        }
        TextView infoTextView = (TextView) findViewById(R.id.infoTextView);
        if (originCountryClient.compareTo("")!=0) {
            infoTextView.setText("Actual state: " + actualState + "\n" + "Future state: " + futureState + "\n" + "Country: " + originCountryClient + "\n" + "Region: " + originRegionClient + "\n" + "City: " + originCityClient + "\n" + "Address: " + originAddressClient + "\n");
        } else {
            infoTextView.setText("Actual state: " + actualState + "\n" + "Future state: " + futureState + "\n");
        }
    }

    public class TaxiTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

        TaxiTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "taxis/" + mTaxiId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONObject obj = new JSONObject(respStr);
                actualState = obj.getString("actualState");
                futureState = obj.getString("futureState");
                if (!obj.isNull("client")) {
                    JSONObject client = obj.getJSONObject("client");
                    JSONObject originCountry = client.getJSONObject("originCountry");
                    JSONObject originRegion = client.getJSONObject("originRegion");
                    JSONObject originCity = client.getJSONObject("originCity");
                    JSONObject originAddress = client.getJSONObject("originAddress");
                    originCountryClient = originCountry.getString("name");
                    originRegionClient = originRegion.getString("name");
                    originCityClient = originCity.getString("name");
                    originAddressClient = originAddress.getString("name");
                }
                taxiFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
