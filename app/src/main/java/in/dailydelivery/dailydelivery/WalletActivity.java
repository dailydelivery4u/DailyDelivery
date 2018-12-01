package in.dailydelivery.dailydelivery;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class WalletActivity extends AppCompatActivity {

    private String checksumHash;
    private ProgressDialog progress;
    int userId;
    SharedPreferences sharedPref;
    TextView balanceDisplayTV;
    EditText amountET;
    private String orderId;
    String amountToRecharge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        balanceDisplayTV = findViewById(R.id.balanceDisplayTV);
        amountET = findViewById(R.id.amountET);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCanceledOnTouchOutside(false);

        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
            userId = sharedPref.getInt(getString(R.string.sp_tag_user_id), 273);
            List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
            params.add(new AbstractMap.SimpleEntry("user_id", userId));
            try {
                new GetBalance(getQuery(params)).execute(getString(R.string.server_addr_release) + "wallet/getBalance.php");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Unable to update Balance!\nNo Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------
    }

    public void onPayBtnClicked(View view) {
        progress.show();
        amountToRecharge = amountET.getText().toString() + ".00";
        orderId = generateOrderId(9);
        //getHashFromServer
//----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            //TODO: Update params beforer release
            List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
            params.add(new AbstractMap.SimpleEntry("MID", getString(R.string.paytm_mid)));
            params.add(new AbstractMap.SimpleEntry("ORDER_ID", orderId));
            params.add(new AbstractMap.SimpleEntry("CUST_ID", userId));
            params.add(new AbstractMap.SimpleEntry("INDUSTRY_TYPE_ID", "Retail"));
            params.add(new AbstractMap.SimpleEntry("CHANNEL_ID", "WAP"));
            params.add(new AbstractMap.SimpleEntry("TXN_AMOUNT", amountToRecharge));
            params.add(new AbstractMap.SimpleEntry("WEBSITE", "WEBSTAGING"));
            params.add(new AbstractMap.SimpleEntry("CALLBACK_URL", "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId));
            try {
                new GetHashFromServer(getQuery(params)).execute(getString(R.string.server_addr_release) + "wallet/generateChecksum.php");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            progress.dismiss();
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
    }

    public String generateOrderId(int count) {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        StringBuilder builder = new StringBuilder();
        //builder.append("ORDER");

        while (count-- != 0) {

            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());

            builder.append(ALPHA_NUMERIC_STRING.charAt(character));

        }

        return builder.toString();

    }

    private void connectToPaytm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        //TODO: Update Paytm Release function and params before release
        PaytmPGService Service = PaytmPGService.getStagingService();

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("MID", getString(R.string.paytm_mid));
// Key in your staging and production MID available in your dashboard
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", String.valueOf(userId));
        //paramMap.put( "MOBILE_NO" , "7777777777");
        //paramMap.put( "EMAIL" , "username@emailprovider.com");
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("TXN_AMOUNT", amountToRecharge);
        paramMap.put("WEBSITE", "WEBSTAGING");
// This is the staging value. Production value is available in your dashboard
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
// This is the staging value. Production value is available in your dashboard
        paramMap.put("CALLBACK_URL", "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId);
        paramMap.put("CHECKSUMHASH", checksumHash);
        PaytmOrder Order = new PaytmOrder(paramMap);
        progress.dismiss();
        Service.initialize(Order, null);

        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            /*Call Backs*/
            public void someUIErrorOccurred(String inErrorMessage) {
            }

            public void onTransactionResponse(Bundle inResponse) {
                //String jsonString = inResponse.toString().substring(7,inResponse.toString().length()-1);
                JSONObject json = new JSONObject();
                Set<String> keys = inResponse.keySet();
                for (String key : keys) {
                    try {
                        // json.put(key, bundle.get(key)); see edit below
                        json.put(key, inResponse.get(key));
                    } catch (JSONException e) {
                        //Handle exception here
                    }
                }
                Log.d("dd", "Payment Transaction response " + json.toString());
                verifyChecksumFromServer(json);
            }

            public void networkNotAvailable() {
            }

            public void clientAuthenticationFailed(String inErrorMessage) {
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
            }

            public void onBackPressedCancelTransaction() {
            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
            }
        });

    }

    private void verifyChecksumFromServer(JSONObject json) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < json.names().length(); i++) {
            if (first) first = false;
            else result.append("&");
            try {
                result.append(json.names().getString(i));
                result.append("=");
                result.append(json.get(json.names().getString(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        result.append("&USER_ID=" + userId);

        new VerifyChecksumFromServer(result.toString()).execute(getString(R.string.server_addr_release) + "wallet/verifyChecksum.php");
    }

    private String getQuery(List<AbstractMap.SimpleEntry> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey().toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue().toString(), "UTF-8"));
        }

        return result.toString();
    }

    private class GetHashFromServer extends AsyncTask<String, Void, String> {
        String dataToServer;

        public GetHashFromServer(String dataToServer) {
            this.dataToServer = dataToServer;
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
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Hash from webserver: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                checksumHash = resultJson.getString("CHECKSUMHASH");
                connectToPaytm();
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
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(dataToServer);
                writer.flush();
                writer.close();
                out.flush();
                out.close();
                is = conn.getInputStream();
                return readIt(is, len);
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

    private class VerifyChecksumFromServer extends AsyncTask<String, Void, String> {
        String checksumRcvd;

        public VerifyChecksumFromServer(String checksumRcvd) {
            this.checksumRcvd = checksumRcvd;
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
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Result from webserver After server validation: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                evaluateResult(resultJson.getString("STATUS"));

            } catch (JSONException e) {
                e.printStackTrace();
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
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(checksumRcvd);
                writer.flush();
                writer.close();
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

    private void evaluateResult(String status) {
        if (status.equals("AISH")) {
            Toast.makeText(this, "Recharge Success", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Recharge Failed", Toast.LENGTH_LONG).show();
        }
        this.recreate();
    }

    private class GetBalance extends AsyncTask<String, Void, String> {
        String dataToServer;

        public GetBalance(String dataToServer) {
            this.dataToServer = dataToServer;
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
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Wallet balance from webserver: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                updateBalance(resultJson.getInt("balance"));
            } catch (JSONException e) {
                e.printStackTrace();
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
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(dataToServer);
                writer.flush();
                writer.close();
                out.flush();
                out.close();
                is = conn.getInputStream();
                return readIt(is, len);
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

    private void updateBalance(int balance) {
        if (balance != 12345678) {
            balanceDisplayTV.setText("Rs. " + String.valueOf(balance));
        }
    }
}
