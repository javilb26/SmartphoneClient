package tfg.taxicentral;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private SetTokenTask mSetTokenTask = null;

    // UI references.
    private EditText mTaxiIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    boolean resul = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mTaxiIdView = (EditText) findViewById(R.id.taxiId);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mTaxiIdLogInButton = (Button) findViewById(R.id.taxi_log_in_button);
        mTaxiIdLogInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mTaxiIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        Long taxiId = Long.parseLong(mTaxiIdView.getText().toString());
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (taxiId.intValue()==0) {
            mTaxiIdView.setError(getString(R.string.error_field_required));
            focusView = mTaxiIdView;
            cancel = true;
        } else if (!isTaxiIdValid(taxiId)) {
            mTaxiIdView.setError(getString(R.string.error_invalid_taxiId));
            focusView = mTaxiIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(taxiId, password);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isTaxiIdValid(Long taxiId) {
        return (taxiId.intValue() >= 1) && (taxiId.intValue() <= 99999);
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final String mPassword;

        UserLoginTask(Long taxiId, String password) {
            mTaxiId = taxiId;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(getString(R.string.ip)+"login");
            post.setHeader("content-type", "application/json");
            try
            {
                JSONObject object = new JSONObject();
                byte[] data = mPassword.getBytes("UTF-8");
                String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                object.put("taxiId", mTaxiId);
                object.put("password", base64);
                StringEntity entity = new StringEntity(object.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());
                SharedPreferences.Editor editorSharedPreferences = getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).edit();
                editorSharedPreferences.putLong("taxiId", mTaxiId);
                editorSharedPreferences.putString("password", base64);
                editorSharedPreferences.commit();
                if (respStr.substring(0,1).compareTo("{")==0) {
                    resul = true;
                }
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }

            return resul;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (resul) {
                if (getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getInt("refreshedTokenFlag",0)==1) {
                    sendRegistrationToServer(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getString("token",null));
                }
                navigatetoMenuActivity();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public void navigatetoMenuActivity(){
        Intent menuIntent = new Intent(getApplicationContext(),MenuActivity.class);
        startActivity(menuIntent);
    }

    private void sendRegistrationToServer(String token) {
        if (mSetTokenTask != null) {
            return;
        }

        mSetTokenTask = new SetTokenTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0), token);
        mSetTokenTask.execute((Void) null);
    }

    public class SetTokenTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;
        private final String mToken;

        SetTokenTask(Long taxiId, String token) {
            mTaxiId = taxiId;
            mToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/token/" + mToken);
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

