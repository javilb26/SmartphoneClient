package tfg.taxicentral;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class HistoryActivity extends ListActivity {

    private HistoryTask mHistoryTask = null;
    //private NumTaxisStandTask mNumTaxisStandTask = null;
    String[] history;
    String[] paths;
    String numTaxis;
    int historyTaskFlag = 0, numTaxisStandTaskFlag = 0;
    int notEmptyHistoryFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHistoryTask != null) {
            return;
        }
        mHistoryTask = new HistoryTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
        mHistoryTask.execute((Void) null);
        //TODO resolver sin recurrir a sleeps
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        while (historyTaskFlag == 0) {

        }
        if (notEmptyHistoryFlag == 0) {
            Toast.makeText(getApplicationContext(), "No travels", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, history);
            setListAdapter(adapter);
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.e("HistoryActivity", "item pulsado");
        String item = (String) getListAdapter().getItem(position);
        Log.e("HistoryActivity", item);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.putExtra("path", paths[position]);
        Log.e("path", paths[position]);
        intent.putExtra("historyPath", true);
        startActivity(intent);
    }

    public class HistoryTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

        HistoryTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "taxis/" + mTaxiId + "/travels");
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                history = new String[respJSON.length()];
                paths = new String[999999];
                for (int i = 0; i < respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);
                    //Log.e("HistoryActivity",obj.toString());
                    //TODO mirar si lo ideal seria que teniendo un array de ids (manteniendo la misma i que para stand apareciera ya en view taxi stands el numero de taxis en parada
                    JSONObject originCountry = obj.getJSONObject("originCountry");
                    JSONObject originRegion = obj.getJSONObject("originRegion");
                    JSONObject originCity = obj.getJSONObject("originCity");
                    JSONObject originAddress = obj.getJSONObject("originAddress");
                    JSONObject destinationCountry = obj.getJSONObject("destinationCountry");
                    JSONObject destinationRegion = obj.getJSONObject("destinationRegion");
                    JSONObject destinationCity = obj.getJSONObject("destinationCity");
                    JSONObject destinationAddress = obj.getJSONObject("destinationAddress");
                    JSONObject originPoint = obj.getJSONObject("originPoint");
                    JSONObject destinationPoint = obj.getJSONObject("destinationPoint");
                    JSONObject path = obj.getJSONObject("path");
                    history[i] = obj.getLong("travelId") + " - '" + obj.getString("date") + "'" + " - '" + originCountry.getString("name") + "'" + " - '" + originRegion.getString("name") + "'" + " - '" + originCity.getString("name") + "'" + " - '" + originAddress.getString("name") + "'" + " - '" + destinationCountry.getString("name") + "'" + " - '" + destinationRegion.getString("name") + "'" + " - '" + destinationCity.getString("name") + "'" + " - '" + destinationAddress.getString("name") + "'" + " - '" + obj.getLong("distance") + "'" + " - '" + originPoint.getString("coordinates") + "'" + " - '" + destinationPoint.getString("coordinates") + "'" + " - '" + obj.getString("path") + "'";
                    paths[i]= path.getString("coordinates");
                    notEmptyHistoryFlag = 1;
                    //Log.e("NearestStands","Stand: " + stand[i].toString());
                }
                historyTaskFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }
/*
    public class NumTaxisStandTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mStandId;

        NumTaxisStandTask(Long standId) {
            mStandId = standId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "stands/" + mStandId + "/numtaxis");
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                numTaxis = EntityUtils.toString(resp.getEntity());
                numTaxisStandTaskFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }
*/
}
