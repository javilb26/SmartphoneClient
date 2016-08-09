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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HistoryActivity extends ListActivity {

    private HistoryTask mHistoryTask = null;
    //private NumTaxisStandTask mNumTaxisStandTask = null;
    String[] history;
    String[] paths;
    String numTaxis;
    int historyTaskFlag = 0, numTaxisStandTaskFlag = 0;
    int notEmptyHistoryFlag = 0;
    String[] history2;

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
            history2 = new String[history.length];
            String historyO2;
            int i = 0;
            for (String historyO: history) {
                String[] historyOaux = historyO.split("-");
                historyO2 = "ID: " + historyOaux[0] + "\n" + "Date: " + historyOaux[1]+"-"+historyOaux[2]+"-"+historyOaux[3] + "\n" + "Origin -> Country: " + historyOaux[4] + "\n" + "Origin -> Region: " + historyOaux[5] + "\n" + "Origin -> City: " + historyOaux[6] + "\n" + "Origin -> Address: " + historyOaux[7] + "\n" + "Destination -> Country: " + historyOaux[8] + "\n" + "Destination -> Region: " + historyOaux[9] + "\n" + "Destination -> City: " + historyOaux[10] + "\n" + "Destination -> Address: " + historyOaux[11] + "\n";
                history2[i] = historyO2;
                i++;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, history2);
            setListAdapter(adapter);
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.putExtra("path", paths[position]);
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
                    Calendar dateAsCalendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    dateAsCalendar.setTimeInMillis(Long.parseLong(obj.getString("date")));
                    history[i] = obj.getLong("travelId") + " - '" + sdf.format(dateAsCalendar.getTime()) + "'" + " - '" + originCountry.getString("name") + "'" + " - '" + originRegion.getString("name") + "'" + " - '" + originCity.getString("name") + "'" + " - '" + originAddress.getString("name") + "'" + " - '" + destinationCountry.getString("name") + "'" + " - '" + destinationRegion.getString("name") + "'" + " - '" + destinationCity.getString("name") + "'" + " - '" + destinationAddress.getString("name") + "'" + " - '" + obj.getLong("distance") + "'"/* + " - '" + originPoint.getString("coordinates") + "'" + " - '" + destinationPoint.getString("coordinates") + "'" + " - '" + obj.getString("path") + "'"*/;
                    paths[i]= path.getString("coordinates");
                    notEmptyHistoryFlag = 1;
                }
                historyTaskFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
