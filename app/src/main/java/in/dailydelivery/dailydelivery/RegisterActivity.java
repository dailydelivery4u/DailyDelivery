package in.dailydelivery.dailydelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class RegisterActivity extends AppCompatActivity {
    EditText nameInput, addInput;
    SharedPreferences sharedPref;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        nameInput = findViewById(R.id.nameInput);
        addInput = findViewById(R.id.addInput);
        progress = new ProgressDialog(this);
        getSupportActionBar().setTitle("Welcome");

    }

    public void registerBtnOnClick(View view) {
        progress.setMessage("Hang on!! Registering Our Beloved Customer...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(30);
        progress.setCanceledOnTouchOutside(false);

        if (nameInput.getText().equals("") || addInput.getText().equals("")) {
            Toast.makeText(this, "Please fill your name and address", Toast.LENGTH_LONG).show();
        } else {

//----------------------------------Connect to Server
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                progress.show();
                //Create a JSONObject for sending to server
                JSONObject obj = new JSONObject();

                try {
                    obj.put("name", nameInput.getText().toString());
                    obj.put("id", sharedPref.getInt(getString(R.string.sp_tag_user_id), 0));
                    obj.put("add", addInput.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new RegisterActivity.PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "add_user.php");
            } else {
                Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
            }
        }
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
                return "Unable to contact server... check net connection";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(RegisterActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    //Registered succesfully
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("logged_in", true);
                    editor.putString(getString(R.string.sp_tag_user_name), resultJson.getString("name"));
                    editor.putString(getString(R.string.sp_tag_user_phone), resultJson.getString("phone"));
                    editor.putString(getString(R.string.sp_tag_user_add), resultJson.getString("add"));
                    editor.commit();
                    Intent userHomeActivityIntent = new Intent(RegisterActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(userHomeActivityIntent);
                    finish();
                } else if (resultJson.getInt("responseCode") == 275) {
                    //Regsitration failed
                    Toast.makeText(RegisterActivity.this, "Something went wrong.. try again later", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
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
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
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
            } catch (SocketTimeoutException e) {
                return "timeout";
            } finally {
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
