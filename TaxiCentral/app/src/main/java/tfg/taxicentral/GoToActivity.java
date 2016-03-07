package tfg.taxicentral;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GoToActivity extends ActionBarActivity {

    private GetCountriesTask mGetCountriesTask = null;
    String[] countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to);

        mGetCountriesTask = new GetCountriesTask();
        mGetCountriesTask.execute((Void) null);
    }

    public void createInstanceArrayAdapter() {
        //Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.select_dialog_item, countries);

        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actv = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCountries);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GetCountriesTask extends AsyncTask<Void, Void, Boolean> {

        GetCountriesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://10.0.2.2:8080/SpringMVCHibernate/country");
            get.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                Log.e("Cero",respStr);
                JSONArray respJSON = new JSONArray(respStr);

                countries = new String[respJSON.length()];

                for(int i=0; i<respJSON.length(); i++)
                {
                    JSONObject obj = respJSON.getJSONObject(i);

                    //Long countryId = new Long(obj.getInt("countryId"));
                    String name = obj.getString("name");
                    //Rellenamos la lista con los resultados
                    countries[i] = name;
                }

            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                return false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            createInstanceArrayAdapter();
        }

    }

}
