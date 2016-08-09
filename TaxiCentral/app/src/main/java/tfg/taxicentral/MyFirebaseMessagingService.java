package tfg.taxicentral;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Javi on 14/07/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private DeclineTask declineTask = null;
    private Thread thread;
    private TaxiTask mTaxiTask = null;
    private int taxiFlag = 0;
    private String actualState = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        //Calling method to generate notification
        //sendNotification(remoteMessage.getNotification().getBody());
        sendNotification(remoteMessage.getData().get("data"));
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String data) {
        String[] parts = data.split(",");
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra("clientId", Long.valueOf(parts[0].substring(parts[0].indexOf(":")+1)));
        intent.putExtra("country", parts[1].substring(parts[1].indexOf(":")+2, parts[1].length()-1));
        intent.putExtra("region", parts[2].substring(parts[2].indexOf(":")+2, parts[2].length()-1));
        intent.putExtra("city", parts[3].substring(parts[3].indexOf(":")+2, parts[3].length()-1));
        intent.putExtra("address", parts[4].substring(parts[4].indexOf(":")+2, parts[4].length()-2));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TaxiCentral")
                .setContentText("Nuevo viaje: 10 segundos para aceptarlo")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

        thread = new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        wait(10000);
                    }
                } catch(InterruptedException ex){
                }
                mTaxiTask = new TaxiTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
                mTaxiTask.execute((Void) null);
                while (taxiFlag == 0) {
                }
                if ((actualState.compareTo("AVAILABLE")==0)||(actualState.compareTo("INSTAND")==0)) {
                    notificationManager.cancel(0);
                    if (declineTask != null) {
                        return;
                    }
                    declineTask = new DeclineTask(getSharedPreferences("credentials", getApplicationContext().MODE_PRIVATE).getLong("taxiId", 0));
                    declineTask.execute((Void) null);
                }
            }
        };
        thread.start();

    }

    public class DeclineTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

        DeclineTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpPut put = new HttpPut(getString(R.string.ip) + "taxis/" + mTaxiId + "/decline");
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

    public class TaxiTask extends AsyncTask<Void, Void, Boolean> {

        private final Long mTaxiId;

        TaxiTask(Long taxiId) {
            mTaxiId = taxiId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(getString(R.string.ip) + "taxis/" + mTaxiId);
            get.setHeader("content-type", "application/json");
            try {
                HttpResponse resp = new DefaultHttpClient().execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONObject obj = new JSONObject(respStr);
                actualState = obj.getString("actualState");
                taxiFlag = 1;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                return false;
            }
            return true;
        }

    }

}
