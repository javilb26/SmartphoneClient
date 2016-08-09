package tfg.taxicentral;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.Set;

/**
 * Created by Javi on 14/07/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private int refreshedTokenFlag = 0;

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        refreshedTokenFlag = 1;
        SharedPreferences.Editor editorSharedPreferences = getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).edit();

        editorSharedPreferences.putInt("refreshedTokenFlag", refreshedTokenFlag);
        editorSharedPreferences.putString("token", refreshedToken);
        editorSharedPreferences.commit();

    }

}
