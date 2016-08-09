package tfg.taxicentral;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ChangePasswordActivity extends AppCompatActivity {

    private ChangePasswordTask mChangePasswordTask = null;
    private TextView mTaxiIdView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mTaxiIdView = (TextView) findViewById(R.id.taxiIdP);
        mPasswordView = (EditText) findViewById(R.id.passwordP);

        mTaxiIdView.setText("Id: " + getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));

        Button mChangePasswordButton = (Button) findViewById(R.id.change_password_button);
        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChangePasswordTask = new ChangePasswordTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), mPasswordView.getText().toString());
                mChangePasswordTask.execute((Void) null);
            }
        });
    }

    public class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final String mPassword;

        ChangePasswordTask(Long taxiId, String password) {
            mTaxiId = taxiId;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] data = new byte[0];
            try {
                data = mPassword.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(getString(R.string.ip)+"changepassword");
            post.setHeader("content-type", "application/json");
            try {
                JSONObject object = new JSONObject();
                object.put("taxiId", mTaxiId);
                object.put("password", base64);
                StringEntity entity = new StringEntity(object.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());
                if (!respStr.equals("true"))
                    return false;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(getApplicationContext(), "Password changed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Error changing password", Toast.LENGTH_SHORT).show();
            }
            finish();
        }

    }

}
