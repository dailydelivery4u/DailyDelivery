package in.dailydelivery.dailydelivery;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
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

import in.dailydelivery.dailydelivery.Fragments.CategoryDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.categories.Categories;

public class CreateOrderActivity extends AppCompatActivity implements CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener {

    SharedPreferences sharedPref;
    int req_type; // to share whether full list of categories is requested or only update check
    FrameLayout fragmentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        fragmentFrame = findViewById(R.id.fragment_frame);
        //Check if categories are already downloaded
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        if(!sharedPref.getBoolean(getString(R.string.categories_downloaded),false)){
            //Categories not yet downloaded.. GO ahead and download
            getCategoriesFromServer(1);
        } else {
            displayCategories(1);
            getCategoriesFromServer(2);
        }
    }

    private void getCategoriesFromServer(int i) {

        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Create a JSONObject for sending to server
            JSONObject obj = new JSONObject();

            switch(i){
                case 1:
                    //get list of categories
                    req_type = 1;
                    try {
                        obj.put("cat_token",1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case 2:
                    //check if there is an update of categories from server
                    req_type = 2;
                    try {
                        obj.put("cat_token",2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }




            new CreateOrderActivity.PostDataToServer(obj).execute(getString(R.string.server_addr) + "categories_req.php");
        } else {
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------

    }

    public void displayCategories(int i){
        //i = 1 => display, i=2 => update
        switch (i){
            case 1:
                CategoryDisplayFragment frag = new CategoryDisplayFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame,frag).commit();
                break;

            case 2:

                break;
        }
    }

    @Override
    public void categoryFragmentInteraction(Categories.category item) {
        Toast.makeText(this,"Item Clicked: "+ item.toString(),Toast.LENGTH_LONG).show();
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
                Toast.makeText(CreateOrderActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD","Result from webserver in Create Order Activity: "+ result);
            try {
                JSONArray resultArrayJson = new JSONArray(result);
                //Check for Result COde
                //Store categories in sharedpref
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("cat_num",resultArrayJson.length());
                editor.putBoolean(getString(R.string.categories_downloaded),true);
                for(int i=0;i<resultArrayJson.length();i++){
                    JSONObject obj = resultArrayJson.getJSONObject(i);
                    editor.putString("cat_"+String.valueOf(i),obj.getString("name"));
                }
                editor.commit();
                displayCategories(1);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

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
