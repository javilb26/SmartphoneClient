package tfg.taxicentral;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FutureStateActivity extends ListActivity {

    private FutureStateTask mFutureStateTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new String[]{"Off", "Available"});
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        if (mFutureStateTask != null) {
            return;
        }
        mFutureStateTask = new FutureStateTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), item);
        mFutureStateTask.execute((Void) null);
        super.finish();
    }

    public class FutureStateTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final String mState;

        FutureStateTask(Long taxiId, String state) {
            mTaxiId = taxiId;
            mState = state;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/futurestate/" + mState);
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
