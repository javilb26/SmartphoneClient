package tfg.taxicentral;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by Javi on 14/07/2016.
 */
public class AlertActivity extends AppCompatActivity {

    private AcceptTask acceptTask = null;
    private DeclineTask declineTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        /*
        Content-Type: application/json
        Authorization: key=AIzaSyDuJHUIiXZGWgul-eH_28qugSELqErrsOc

        {
            "to" : "cGGRuSk22Aw:APA91bHCAOUv2H9i5fs4gdjjiLriPrWYabAbRQgnJFBO2lQtOenx-zlBmnSZvo9yEI8C-f6dNl5lpowNBA5e1sBIjBoSK9cHbG3ELjwmwlHfcraif17BWfKQOh32Lr1S1VfrxGa9obZl",
            "data" : {
	            "clientId": 1
	            "country": "ESPAÑA"
	            "region": "A Coruña"
	            "city": "A Coruña"
	            "address": "CALLE ALCALDE ABAD CONDE"
            }
        }
        */
        TextView alertTextView = (TextView) findViewById(R.id.alertTextView);
        alertTextView.setText("Country: " + getIntent().getStringExtra("country") + "\n" + "Region: " + getIntent().getStringExtra("region") + "\n" + "City: " + getIntent().getStringExtra("city") + "\n" + "Address: " + getIntent().getStringExtra("address") + "\n");

        Button mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (acceptTask != null) {
                    return;
                }
                acceptTask = new AcceptTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), getIntent().getLongExtra("clientId",0));
                acceptTask.execute((Void) null);
                finish();
            }
        });

        Button mDeclineButton = (Button) findViewById(R.id.declineButton);
        mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (declineTask != null) {
                    return;
                }
                declineTask = new DeclineTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
                declineTask.execute((Void) null);
                finish();
            }
        });

    }

    public class AcceptTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final Long mClientId;

        AcceptTask(Long taxiId, Long clientId) {
            mTaxiId = taxiId;
            mClientId = clientId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/clients/" + mClientId + "/accept");
            put.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(put);
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

    public class DeclineTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

        DeclineTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/decline");
            put.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(put);
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
