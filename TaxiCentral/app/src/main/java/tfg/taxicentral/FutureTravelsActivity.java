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

public class FutureTravelsActivity extends ListActivity {

    private FutureTravelsTask mFutureTravelsTask = null;
    //private NumTaxisStandTask mNumTaxisStandTask = null;
    String[] futureTravels;
    String numTaxis;
    int futureTravelsTaskFlag = 0, numTaxisStandTaskFlag = 0;
    long[] futureTravelId, originCountryId, originRegionId, originCityId, originAddressId, destinationCountryId, destinationRegionId, destinationCityId, destinationAddressId;
    String[] originCountryStr, originRegionStr, originCityStr, originAddressStr, destinationCountryStr, destinationRegionStr, destinationCityStr, destinationAddressStr;
    int notEmptyHistoryFlag = 0;
    String[] futureTravels2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mFutureTravelsTask != null) {
            return;
        }
        mFutureTravelsTask = new FutureTravelsTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
        mFutureTravelsTask.execute((Void) null);
        //TODO resolver sin recurrir a sleeps
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        while (futureTravelsTaskFlag == 0) {

        }
        if (notEmptyHistoryFlag == 0) {
            Toast.makeText(getApplicationContext(), "No futuretravels", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            futureTravels2 = new String[futureTravels.length];
            String futureTravel2;
            int i = 0;
            for (String futureTravel: futureTravels) {
                String[] futureTravelaux = futureTravel.split("-");
                futureTravel2 = "ID: " + futureTravelaux[0] + "\n" + "Date: " + futureTravelaux[1]+"-"+futureTravelaux[2]+"-"+futureTravelaux[3] + "\n" + "Origin -> Country: " + futureTravelaux[4] + "\n" + "Origin -> Region: " + futureTravelaux[5] + "\n" + "Origin -> City: " + futureTravelaux[6] + "\n" + "Origin -> Address: " + futureTravelaux[7] + "\n" + "Destination -> Country: " + futureTravelaux[8] + "\n" + "Destination -> Region: " + futureTravelaux[9] + "\n" + "Destination -> City: " + futureTravelaux[10] + "\n" + "Destination -> Address: " + futureTravelaux[11] + "\n";
                futureTravels2[i] = futureTravel2;
                i++;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, futureTravels2);
            setListAdapter(adapter);
        }
    }

    //Ir a
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        //Esta clase tendra arriva del toodo un textview con la info y dos botones, uno para ir y otro para borrar
        Intent intent = new Intent(getApplicationContext(), FutureTravelOptionsActivity.class);
            intent.putExtra("item", item);
            intent.putExtra("futureTravelId", futureTravelId[position]);
            intent.putExtra("originCountryStr", originCountryStr[position]);
            intent.putExtra("originRegionStr", originRegionStr[position]);
            intent.putExtra("originCityStr", originCityStr[position]);
            intent.putExtra("originAddressStr", originAddressStr[position]);
            intent.putExtra("destinationCountryStr", destinationCountryStr[position]);
            intent.putExtra("destinationRegionStr", destinationRegionStr[position]);
            intent.putExtra("destinationCityStr", destinationCityStr[position]);
            intent.putExtra("destinationAddressStr", destinationAddressStr[position]);
        intent.putExtra("originCountryId", originCountryId[position]);
        intent.putExtra("originRegionId", originRegionId[position]);
        intent.putExtra("originCityId", originCityId[position]);
        intent.putExtra("originAddressId", originAddressId[position]);
        intent.putExtra("destinationCountryId", destinationCountryId[position]);
        intent.putExtra("destinationRegionId", destinationRegionId[position]);
        intent.putExtra("destinationCityId", destinationCityId[position]);
        intent.putExtra("destinationAddressId", destinationAddressId[position]);
        startActivity(intent);
        finish();
    }

    public class FutureTravelsTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

    FutureTravelsTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "taxis/" + mTaxiId + "/futuretravels");
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray respJSON = new JSONArray(respStr);
                futureTravels = new String[respJSON.length()];
                futureTravelId = new long[99999];
                originCountryStr = new String[99999];
                originRegionStr = new String[99999];
                originCityStr = new String[99999];
                originAddressStr = new String[99999];
                destinationCountryStr = new String[99999];
                destinationRegionStr = new String[99999];
                destinationCityStr = new String[99999];
                destinationAddressStr = new String[99999];
                originCountryId = new long[99999];
                originRegionId = new long[99999];
                originCityId = new long[99999];
                originAddressId = new long[99999];
                destinationCountryId = new long[99999];
                destinationRegionId = new long[99999];
                destinationCityId = new long[99999];
                destinationAddressId = new long[99999];
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
                    Calendar dateAsCalendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    dateAsCalendar.setTimeInMillis(Long.parseLong(obj.getString("date")));
                    futureTravels[i] = obj.getLong("futureTravelId") + " - '" + sdf.format(dateAsCalendar.getTime()) + "'" + " - '" + originCountry.getString("name") + "'" + " - '" + originRegion.getString("name") + "'" + " - '" + originCity.getString("name") + "'" + " - '" + originAddress.getString("name") + "'" + " - '" + destinationCountry.getString("name") + "'" + " - '" + destinationRegion.getString("name") + "'" + " - '" + destinationCity.getString("name") + "'" + " - '" + destinationAddress.getString("name") + "'";

                    futureTravelId[i] = obj.getLong("futureTravelId");
                    originCountryStr[i] = originCountry.getString("name");
                    originRegionStr[i] = originRegion.getString("name");
                    originCityStr[i] = originCity.getString("name");
                    originAddressStr[i] = originAddress.getString("name");
                    destinationCountryStr[i] = destinationCountry.getString("name");
                    destinationRegionStr[i] = destinationRegion.getString("name");
                    destinationCityStr[i] = destinationCity.getString("name");
                    destinationAddressStr[i] = destinationAddress.getString("name");

                    originCountryId[i] = originCountry.getLong("countryId");
                    originRegionId[i] = originRegion.getLong("regionId");
                    originCityId[i] = originCity.getLong("cityId");
                    originAddressId[i] = originAddress.getLong("addressId");
                    destinationCountryId[i] = destinationCountry.getLong("countryId");
                    destinationRegionId[i] = destinationRegion.getLong("regionId");
                    destinationCityId[i] = destinationCity.getLong("cityId");
                    destinationAddressId[i] = destinationAddress.getLong("addressId");
                    notEmptyHistoryFlag = 1;
                }
                futureTravelsTaskFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
