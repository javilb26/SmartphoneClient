package tfg.taxicentral;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Javi on 14/07/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

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
        intent.putExtra("clientId", Long.valueOf(parts[0].substring(parts[0].indexOf(":") + 1)));
        intent.putExtra("country", parts[1].substring(parts[1].indexOf(":") + 1));
        intent.putExtra("region", parts[2].substring(parts[2].indexOf(":") + 1));
        intent.putExtra("city", parts[3].substring(parts[3].indexOf(":") + 1));
        intent.putExtra("address", parts[4].substring(parts[4].indexOf(":") + 1));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TaxiCentral")
                .setContentText("Nuevo viaje: 5 segundos para aceptarlo")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
