package tfg.taxicentral;

import android.app.ListActivity;
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

public class HistoryActivity extends ListActivity {

    private HistoryTask mHistoryTask = null;
    //private NumTaxisStandTask mNumTaxisStandTask = null;
    String[] history;
    String numTaxis;
    int historyTaskFlag = 0, numTaxisStandTaskFlag = 0;

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, history);
        setListAdapter(adapter);
    }
/*
    //TODO Al clickar en un travel -> mostrar en Navigation la ruta hecha
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        numTaxisStandTaskFlag = 0;
        Log.e("NearestStandsActivity", "item pulsado");
        String item = (String) getListAdapter().getItem(position);
        Log.e("NearestStandsActivity", item.substring(0,1));
        mNumTaxisStandTask = new NumTaxisStandTask(new Long(item.substring(0,1)));
        mNumTaxisStandTask.execute((Void) null);
        //TODO Arreglar actualizacion
        while (numTaxisStandTaskFlag == 0) {

        }
        if (numTaxis.compareTo("")==0) {
            numTaxis = "0";
        }
        Toast.makeText(getApplicationContext(), numTaxis, Toast.LENGTH_SHORT).show();
        //super.finish();
    }
*/
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
                    history[i] = obj.getLong("travelId") + " - '" + obj.getString("date") + "'" + " - '" + originCountry.getString("name") + "'" + " - '" + originRegion.getString("name") + "'" + " - '" + originCity.getString("name") + "'" + " - '" + originAddress.getString("name") + "'" + " - '" + destinationCountry.getString("name") + "'" + " - '" + destinationRegion.getString("name") + "'" + " - '" + destinationCity.getString("name") + "'" + " - '" + destinationAddress.getString("name") + "'" + " - '" + obj.getLong("distance") + "'" + " - '" + originPoint.getString("coordinates") + "'" + " - '" + destinationPoint.getString("coordinates") + "'" + " - '" + obj.getString("path") + "'";
                    historyTaskFlag = 1;
                    //Log.e("NearestStands","Stand: " + stand[i].toString());
                }

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