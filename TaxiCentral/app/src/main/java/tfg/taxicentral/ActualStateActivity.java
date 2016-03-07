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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ActualStateActivity extends ListActivity {

    private ActualStateTask mActualStateTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_menu);

        String[] values = new String[] { "Available", "Busy", "Off" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        if (mActualStateTask != null) {
            return;
        }
        Long l1 = new Long(1);
        mActualStateTask = new ActualStateTask(l1, item);
        mActualStateTask.execute((Void) null);
        super.finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ActualStateTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final String mState;

        ActualStateTask(Long taxiId, String state) {
            mTaxiId = taxiId;
            mState = state;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resul = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPut put = new HttpPut("http://10.0.2.2:8080/SpringMVCHibernate/taxi/" + mTaxiId + "/actualstate/" + mState);
            put.setHeader("content-type", "application/json");
            try
            {
                HttpResponse resp = httpClient.execute(put);
                String respStr = EntityUtils.toString(resp.getEntity());
                if(!respStr.equals("true"))
                    resul = false;
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }
            return resul;
        }

    }

}
