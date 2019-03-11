package in.dailydelivery.dailydelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.WalletTransaction;

public class WalletActivity extends AppCompatActivity {

    int userId;
    SharedPreferences sharedPref;
    TextView balanceDisplayTV, monthlyTV, weeklyTV;
    EditText amountET;
    String amountToRecharge;
    AppDatabase db;
    int rcWeeklyProjection = 0, otoWeeklyProjection = 0, otoMonthlyProjection = 0;
    private String checksumHash;
    private ProgressDialog progress;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        balanceDisplayTV = findViewById(R.id.balanceDisplayTV);
        amountET = findViewById(R.id.amountET);
        monthlyTV = findViewById(R.id.monthlyCostTV);
        weeklyTV = findViewById(R.id.weeklyCostTV);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCanceledOnTouchOutside(false);

        db = AppDatabase.getAppDatabase(this);

        getSupportActionBar().setTitle("My Wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        new GetAmountProjections().execute();

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
                //new GetBalance(getQuery(params)).execute(getString(R.string.server_addr_release) + "categories_req.php");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Unable to update Balance!\nNo Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------
    }

    public void onPayBtnClicked(View view) {
        startTransaction();
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        } else {
            startTransaction();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                startTransaction();
            }
        }
    }

    public void startTransaction() {
        progress.show();
        amountToRecharge = amountET.getText().toString() + ".00";
        orderId = generateOrderId(9);
        //getHashFromServer
        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
            params.add(new AbstractMap.SimpleEntry("MID", getString(R.string.paytm_mid)));
            params.add(new AbstractMap.SimpleEntry("ORDER_ID", orderId));
            params.add(new AbstractMap.SimpleEntry("MOBILE_NO", sharedPref.getString(getString(R.string.sp_tag_user_phone), "2703")));
            params.add(new AbstractMap.SimpleEntry("CUST_ID", userId));
            params.add(new AbstractMap.SimpleEntry("INDUSTRY_TYPE_ID", "Retail"));
            params.add(new AbstractMap.SimpleEntry("CHANNEL_ID", "WAP"));
            params.add(new AbstractMap.SimpleEntry("TXN_AMOUNT", amountToRecharge));
            params.add(new AbstractMap.SimpleEntry("WEBSITE", "DEFAULT"));
            params.add(new AbstractMap.SimpleEntry("CALLBACK_URL", "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId));
            try {
                new GetHashFromServer(getQuery(params)).execute(getString(R.string.server_addr_release) + "wallet/generateChecksum.php");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                progress.dismiss();
            }
        } else {
            progress.dismiss();
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent userHomeActivityIntent = new Intent(WalletActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent userHomeActivityIntent = new Intent(WalletActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        finish();
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
        PaytmPGService Service = PaytmPGService.getProductionService();

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("MID", getString(R.string.paytm_mid));
// Key in your staging and production MID available in your dashboard
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", String.valueOf(userId));
        paramMap.put("MOBILE_NO", sharedPref.getString(getString(R.string.sp_tag_user_phone), "2703"));
        //paramMap.put( "EMAIL" , "username@emailprovider.com");
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("TXN_AMOUNT", amountToRecharge);
        paramMap.put("WEBSITE", "DEFAULT");
// This is the staging value. Production value is available in your dashboard
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
// This is the staging value. Production value is available in your dashboard
        paramMap.put("CALLBACK_URL", "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId);
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
                progress.show();
                //Log.d("dd", "Payment Transaction response " + json.toString());
                verifyChecksumFromServer(json);
            }

            public void networkNotAvailable() {
            }

            public void clientAuthenticationFailed(String inErrorMessage) {
                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
            }

            public void onBackPressedCancelTransaction() {
                Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();
            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                Toast.makeText(getApplicationContext(), "Transaction Cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
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

    public void onRequestCashColClicked(View view) {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            progress.show();
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", sharedPref.getInt(getString(R.string.sp_tag_user_id), 273));
                new ReqCashCol(obj).execute(getString(R.string.server_addr_release) + "wallet/req_cc.php");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
    }

    public void onViewWalTxClicked(View view) {
        Intent intent2 = new Intent(this, AccountHistoryActivity.class);
        startActivity(intent2);
    }

    private void evaluateResult(String status) {
        progress.dismiss();
        String message;
        if (status.equals("AISH")) {
            //Toast.makeText(this, "Recharge Success", Toast.LENGTH_LONG).show();
            message = "Recharge Success!!";
        } else {
            //Toast.makeText(this, "Recharge Failed.\n Contact Customer Care for any queries", Toast.LENGTH_LONG).show();
            message = "Recharge Failed!! Pls contact customer care";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent userHomeActivityIntent = new Intent(WalletActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(userHomeActivityIntent);
                finish();
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

    }

    private void updateBalance(int balance) {
        if (balance != 12345678) {
            balanceDisplayTV.setText("Rs. " + String.valueOf(balance));
        }
    }

    private class GetAmountProjections extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            int monthlyProjection = rcWeeklyProjection * 4 + otoMonthlyProjection;
            int weeklyProjection = rcWeeklyProjection + otoWeeklyProjection;
            monthlyTV.setText("Monthly Orders Value: Rs." + monthlyProjection);
            weeklyTV.setText("Weekly Orders Value: Rs." + weeklyProjection);
            amountET.setText(String.valueOf(monthlyProjection));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<RcOrderDetails> rcOrders = db.rcOrderDetailsDao().getRcOrders();
            List<OneTimeOrderDetails> oto = db.oneTimeOrderDetailsDao().getAllOrders();
            for (RcOrderDetails rc : rcOrders) {
                rcWeeklyProjection += rc.getPrice() * (rc.getMon() + rc.getTue() + rc.getWed() + rc.getThu() + rc.getFri() + rc.getSat() + rc.getSun());
            }
            DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
            DateTime today = new DateTime();
            for (OneTimeOrderDetails oneTimeOrderDetails : oto) {
                DateTime orderDate = dtf.parseDateTime(oneTimeOrderDetails.getDate());
                if (orderDate.isBefore(today.plusDays(8)) && orderDate.isAfterNow()) {
                    otoWeeklyProjection += oneTimeOrderDetails.getPrice() * oneTimeOrderDetails.getQty();
                }
                if (orderDate.isBefore(today.plusDays(31)) && orderDate.isAfterNow()) {
                    otoMonthlyProjection += oneTimeOrderDetails.getPrice() * oneTimeOrderDetails.getQty();
                }
            }
            return null;
        }
    }

    private class ReqCashCol extends AsyncTask<String, Void, String> {
        JSONObject orderDetails;

        public ReqCashCol(JSONObject orderDetails) {
            this.orderDetails = orderDetails;
        }

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "timeout";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WalletActivity.this);
                    builder.setCancelable(false);
                    builder.setMessage("Your request has been recorded.\nOur executives will get in touch with you soon.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog mDialog = builder.create();
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                } else if (resultJson.getInt("responseCode") == 275) {
                    Toast.makeText(WalletActivity.this, "Some Error occured!\nPls Contact Customer Care", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(WalletActivity.this, "Error in connection with Server.. \nPlease try again later.", Toast.LENGTH_LONG).show();
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
            int count;
            InputStreamReader reader;

            reader = new InputStreamReader(stream, "UTF-8");
            String str = new String();
            char[] buffer = new char[len];
            while ((count = reader.read(buffer, 0, len)) > 0) {
                str += new String(buffer, 0, count);
            }
            return str;
        }

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
                return "timeout";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
                progress.dismiss();
            }
            //Log.d("DD", "Hash from webserver: " + result);
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
                /*OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(dataToServer);
                writer.flush();
                writer.close();
                out.flush();
                out.close();*/
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                out.write(dataToServer.getBytes());
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
            int count;
            InputStreamReader reader;

            reader = new InputStreamReader(stream, "UTF-8");
            String str = new String();
            char[] buffer = new char[len];
            while ((count = reader.read(buffer, 0, len)) > 0) {
                str += new String(buffer, 0, count);
            }
            return str;
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
                return "timeout";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                progress.dismiss();
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            } else {
                evaluateResult(result);
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
                /*OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(checksumRcvd);
                writer.flush();
                writer.close();
                out.flush();
                out.close();*/
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                out.write(checksumRcvd.getBytes());
                out.flush();
                out.close();

                //int response = conn.getResponseCode();
                //Log.d("NetworkDebugTag", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                //Log.d("NetworkDebugTag", "The text is: " + contentAsString);
                String result = readIt(is, len);
                //Log.d("DD", "Result from webserver After server validation: " + result);
                JSONObject resultJson = new JSONObject(result);
                int response = resultJson.getInt("responseCode");
                if (response == 273) {
                    //Transaction succesfull
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
                    DateTime d = new DateTime();
                    db.walletTransactionDao().insertWalletTransaction(new WalletTransaction(resultJson.getInt("wallettx_id"), 1, "Online Recharge", resultJson.getInt("amount"), d.toString(dtf)));
                    return "AISH";
                } else {
                    return "FAIL";
                }


                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (SocketTimeoutException e) {
                return "timeout";
            } catch (JSONException e) {
                e.printStackTrace();
                return "timeout";
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException {
            int count;
            InputStreamReader reader;

            reader = new InputStreamReader(stream, "UTF-8");
            String str = new String();
            char[] buffer = new char[len];
            while ((count = reader.read(buffer, 0, len)) > 0) {
                str += new String(buffer, 0, count);
            }
            return str;
        }
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
                //Log.d("DD","Caught - " + e.getMessage());
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Log.d("DD","Result - " + result);
            if (result.equals("timeout")) {
                Toast.makeText(WalletActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Wallet balance from webserver: " + result);
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
                /*OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(dataToServer);
                writer.flush();
                writer.close();
                out.flush();
                out.close();*/
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                out.write(dataToServer.getBytes());
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
            int count;
            InputStreamReader reader;

            reader = new InputStreamReader(stream, "UTF-8");
            String str = new String();
            char[] buffer = new char[len];
            while ((count = reader.read(buffer, 0, len)) > 0) {
                str += new String(buffer, 0, count);
            }
            return str;
        }
    }
}
