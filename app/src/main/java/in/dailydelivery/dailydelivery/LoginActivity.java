package in.dailydelivery.dailydelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    EditText phnoInput, pinInput;
    ProgressDialog progress;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        if (sharedPref.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else {
            setContentView(R.layout.activity_login);

            phnoInput = findViewById(R.id.phnoInput);
            pinInput = findViewById(R.id.pinInput);
            progress = new ProgressDialog(this);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+919788528444"));
                startActivity(intent);
            }
        });
    }

    public void onLoginBtnClicked(View view){

        //TODO: Ensure fields are filled before contacting server


        //Toast.makeText(this,"Login",Toast.LENGTH_LONG).show();
        progress.setMessage("Hang on!! Logging You in...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(30);
        progress.setCanceledOnTouchOutside(false);
//----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            progress.show();
            //Create a JSONObject for sending to server
            JSONObject obj = new JSONObject();

            try {
                obj.put("ph", phnoInput.getText().toString());
                obj.put("pin", pinInput.getText().toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            new PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "login.php");
        } else {
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------
    }

    public void onRegisterBrnClicked(View view){
        //Proceed to register Activity
        Intent RegisterActivityIntent = new Intent(this, RegisterActivity.class);
        startActivity(RegisterActivityIntent);
    }



    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class PostDataToServer extends AsyncTask<String, Void, String> {
        JSONObject userDetails;

        public PostDataToServer(JSONObject obj) {
            this.userDetails = obj;
        }

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if(result.equals("timeout")){
                Toast.makeText(LoginActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD","Result from webserver: "+ result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    int userId = resultJson.getInt("userId");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.sp_tag_user_id),userId);
                    editor.putBoolean("logged_in", true);
                    editor.commit();

                    Intent userHomeActivityIntent = new Intent(LoginActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(userHomeActivityIntent);
                    finish();

                } else if (resultJson.getInt("responseCode") == 275) {
                    //Toast.makeText(LoginActivity.this,"Phone No. and Pin does not Match!",Toast.LENGTH_LONG).show();

                    //show the user status with an alert dailogue
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Unsuccessful Login")
                            .setMessage("If you are new here, please proceed to Register!");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //Proceed to Register Activity
                            Intent RegisterActivityIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                            startActivity(RegisterActivityIntent);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                progress.dismiss();
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 1500;

            try {
                URL url = new URL(myurl);
                //Using httpurlconnection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//* milliseconds *//*);
                conn.setConnectTimeout(15000); //* milliseconds *//*);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                // Starts the query
                conn.connect();
                String query = "json=" + userDetails.toString();
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
                out.flush();
                out.close();

                //int response = conn.getResponseCode();
                //Log.d("NetworkDebugTag", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                //Log.d("NetworkDebugTag", "The text is: " + contentAsString);
                return readIt(is, len);

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (SocketTimeoutException e){
                return "timeout";
            }finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException {
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }
}