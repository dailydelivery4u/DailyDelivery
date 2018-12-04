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

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.Fragments.RCOrderDetailsFragment;
import in.dailydelivery.dailydelivery.Fragments.categories.Categories;
import in.dailydelivery.dailydelivery.Fragments.categories.CategoryDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.ProductDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.Products;

public class CreateRecurringOrderActivity extends AppCompatActivity implements CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener, ProductDisplayFragment.ProductDisplayFragmentInteractionListener, RCOrderDetailsFragment.RCOrderDetailsFragmentInteractionListener {
    FrameLayout fragmentFrameLayout;
    ProgressDialog progress;
    SharedPreferences sharedPref;
    AppDatabase db;
    RcOrderDetails rcOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recurring_order);

        fragmentFrameLayout = findViewById(R.id.fragment_frame);
        progress = new ProgressDialog(this);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);

        CategoryDisplayFragment frag = new CategoryDisplayFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, frag).commit();
        db = AppDatabase.getAppDatabase(this);
    }

    @Override
    public void categoryFragmentInteraction(Categories.category item) {
        ProductDisplayFragment productDisplayFragment = new ProductDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("cat_id", item.getId());
        bundle.putInt("order_type", 2);// 1 for 1 time order ; 2 for recurring order
        productDisplayFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, productDisplayFragment).addToBackStack(null).commit();
        getSupportActionBar().setTitle("Products");
    }

    @Override
    public void productDisplayFragmentInteraction(Products.Product item, int qty) {
        RCOrderDetailsFragment frag = new RCOrderDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable("product", item);
        frag.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, frag).addToBackStack(null).commit();
    }

    @Override
    public void rcOrderDetailsFragmentInteraction(RcOrderDetails rcOrderDetails) {
        this.rcOrderDetails = rcOrderDetails;
        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setProgress(30);
            progress.setCanceledOnTouchOutside(false);
            progress.setMessage("Placing your Order...");
            progress.show();
            //Create a JSONArray for sending to server
            JSONArray jsonArray = new JSONArray();
            JSONObject obj = new JSONObject();
            try {
                obj.put("user_id", sharedPref.getInt(getString(R.string.sp_tag_user_id), 273));
                obj.put("p_id", rcOrderDetails.getProductId());
                obj.put("cat_id", rcOrderDetails.getCatId());
                obj.put("delivery_slot", rcOrderDetails.getDeliverySlot());
                obj.put("start_date", rcOrderDetails.getStartDate());
                obj.put("mon", rcOrderDetails.getMon());
                obj.put("tue", rcOrderDetails.getTue());
                obj.put("wed", rcOrderDetails.getWed());
                obj.put("thu", rcOrderDetails.getThu());
                obj.put("fri", rcOrderDetails.getFri());
                obj.put("sat", rcOrderDetails.getSat());
                obj.put("sun", rcOrderDetails.getSun());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //new RegisterActivity.GetHashFromServer(obj).execute(getString(R.string.server_addr) + "add_user.php");
            new PlaceOrder(obj).execute(getString(R.string.server_addr_release) + "add_rc_order.php");
        }
    }
    private class PlaceOrder extends AsyncTask<String, Void, String> {
        JSONObject orderDetails;

        public PlaceOrder(JSONObject orderDetails) {
            this.orderDetails = orderDetails;
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
            if (result.equals("timeout")) {
                Toast.makeText(CreateRecurringOrderActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Result from webserver for RC Order: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    int orderId = resultJson.getInt("order_id");
                    rcOrderDetails.setUid(orderId);
                    //rcOrderDetails.setStatus(1);
                    new UpdateOrderInDb().execute();

                } else if (resultJson.getInt("responseCode") == 275) {
                    Toast.makeText(CreateRecurringOrderActivity.this, "Some Error occured! Pls try again", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateRecurringOrderActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
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
                String query = "json=" + orderDetails.toString();
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

    private class UpdateOrderInDb extends AsyncTask<Void, Void, Void> {

        public UpdateOrderInDb() {
        }

        @Override
        protected Void doInBackground(Void... integers) {
            db.rcOrderDetailsDao().insertRcOrderDetails(rcOrderDetails);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(CreateRecurringOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
            Intent userHomeActivityIntent = new Intent(CreateRecurringOrderActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(userHomeActivityIntent);
            finish();
        }
    }
}
