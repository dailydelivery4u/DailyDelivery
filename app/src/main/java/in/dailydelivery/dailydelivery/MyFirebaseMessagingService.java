package in.dailydelivery.dailydelivery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    SharedPreferences sharedPref;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
        }

    }
    @Override
    public void onNewToken(String token) {
        //Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
// [END on_new_token]

    /**
     * /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        //Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        int userId = sharedPref.getInt(getString(R.string.sp_tag_user_id), 12705);
        if (userId != 12705) {
            //Post the server Key along with requestor Id to update the GCM server key in server database
            String myurl = getString(R.string.server_addr_release) + "insert_gcm_server_key.php";
            InputStream is = null;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                // Starts the query
                conn.connect();

                String query = "userId=" + userId + "&serverKey=" + token;
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                out.write(query.getBytes());
                out.flush();
                out.close();

                //int response = conn.getResponseCode();
                //Log.d("serverComm", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String resultFromServer = readIt(is, 500);
                //Log.d("serverComm", "The result is: " + resultFromServer);
                if (!resultFromServer.isEmpty()) {
                    JSONObject resultJson = new JSONObject(resultFromServer);
                    if (resultJson.getInt("result") == 273) {
                        //Log.d(TAG, "Token Logged at server");
                        //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getString(R.string.gcm_token_saved_at_server_file_variable), true);
                        editor.apply();
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (is != null) try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.gcm_token_saved_at_server_file_variable), false);
            editor.apply();
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, UserHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 273, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //String channelId = getString(R.string.default_notification_channel_id);
        String channelId = "Sandeep123";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground_new)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}


