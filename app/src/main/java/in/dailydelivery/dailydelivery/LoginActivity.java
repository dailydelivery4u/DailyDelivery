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
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.Vacation;
import in.dailydelivery.dailydelivery.DB.WalletTransaction;

//import android.util.Log;

public class LoginActivity extends AppCompatActivity {
    EditText phInput, pinInput;
    RelativeLayout rl1, rl2;
    ProgressDialog progress;
    SharedPreferences sharedPref;
    AppDatabase db;
    String userPh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent createOrderActivityIntent = new Intent(this, RegisterActivity.class);
        startActivity(createOrderActivityIntent);
        /*Intent RegisterActivityIntent = new Intent(this, RegisterActivity.class);
        startActivity(RegisterActivityIntent);*/

        /*sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        if (sharedPref.getBoolean("logged_in", false)) {
            startActivity(new Intent(this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else {
            setContentView(R.layout.activity_login);

            phInput = findViewById(R.id.phInput);
            pinInput = findViewById(R.id.otpInputET);
            progress = new ProgressDialog(this);
            rl1 = findViewById(R.id.phInputRL);
            rl2 = findViewById(R.id.otpInputRL);
        }
        db = AppDatabase.getAppDatabase(this);*/
    }

    public void onLoginBtnClicked(View view) {

        if (pinInput.getText().length() == 4) {
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
                    obj.put("ph", userPh);
                    obj.put("pin", pinInput.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "login.php");
            } else {
                Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please Enter correct OTP", Toast.LENGTH_LONG).show();

        }
    }

    private boolean validPhoneNumber(String s) {
        String regexStr = "^[0-9]{10}$";
        return s.matches(regexStr);
    }

    public void onGetOtpClicked(View view) {

        if (validPhoneNumber(phInput.getText().toString())) {
            progress.setMessage("Sending OTP...");
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
                userPh = phInput.getText().toString();
                try {
                    obj.put("ph", userPh);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new GetOtpFromServer(obj).execute(getString(R.string.server_addr_release) + "req_otp.php");
            } else {
                Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please enter valid phone number to proceed.", Toast.LENGTH_LONG).show();
        }

/*        rl1.setVisibility(View.GONE);
        rl2.setVisibility(View.VISIBLE);
        pinInput.requestFocus();*/
    }

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
            progress.dismiss();
            if (result.equals("OK")) {
                Intent userHomeActivityIntent = new Intent(LoginActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(userHomeActivityIntent);
                finish();
            } else if (result.equals("NOLOGIN")) {
                //show the user status with an alert dailogue
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(false);
                builder.setTitle("OTP Mismatch");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else if (result.equals("TIMEOUT")) {
                Toast.makeText(LoginActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
            } else if (result.equals("NEWUSER")) {
                //Proceed to Register Activity
                Intent RegisterActivityIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(RegisterActivityIntent);
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 3000;

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
                String result = readIt(is, len);
                SharedPreferences.Editor editor = sharedPref.edit();
                if (result.equals("timeout")) {
                    return "TIMEOUT";
                }
                //Log.d("DD", "Result from webserver: " + result);
                try {
                    JSONObject resultArrayJson = new JSONObject(result);
                    //Check for Result COde
                    //If result is OK, update user Id in editor
                    JSONObject resultJson = resultArrayJson.getJSONObject("result");
                    if (resultJson.getInt("responseCode") == 273) {
                        int userId = resultJson.getInt("userId");
                        editor.putInt(getString(R.string.sp_tag_user_id), userId);
                        editor.commit();
                        if (resultJson.getInt("newUser") == 0) {
                            editor.putBoolean("logged_in", true);
                            editor.putString(getString(R.string.sp_tag_user_name), resultJson.getString("name"));
                            editor.putString(getString(R.string.sp_tag_user_phone), resultJson.getString("phone"));
                            editor.putString(getString(R.string.sp_tag_user_add), resultJson.getString("add"));
                            editor.commit();
                            JSONArray otoJson = resultArrayJson.getJSONArray("oto");
                            //Log.d("DD",otoJson.toString());
                            for (int i = 0; i < otoJson.length(); i++) {
                                JSONObject j = (JSONObject) otoJson.get(i);
                                db.oneTimeOrderDetailsDao().insertOnetimeOrderDetails(new OneTimeOrderDetails(j.getInt("id"), j.getInt("product_id"), j.getInt("cat_id"), j.getInt("qty"),
                                        j.getString("name"), j.getString("description"), j.getInt("discount_price"), j.getInt("status"), j.getString("order_date"), j.getInt("delivery_slot")));
                            }
                            JSONArray rcoJson = resultArrayJson.getJSONArray("rco");
                            //Log.d("DD",rcoJson.toString());
                            for (int i = 0; i < rcoJson.length(); i++) {
                                JSONObject j = (JSONObject) rcoJson.get(i);
                                RcOrderDetails r = new RcOrderDetails(j.getInt("p_id"), j.getInt("cat_id"), j.getString("name"), j.getString("description"), j.getInt("discount_price"),
                                        j.getInt("status"), j.getInt("delivery_slot"), j.getString("order_date"), j.getInt("mon"), j.getInt("tue"), j.getInt("wed"), j.getInt("thu"),
                                        j.getInt("fri"), j.getInt("sat"), j.getInt("sun"), j.getInt("frequency"), j.getInt("day1_qty"), j.getInt("day2_qty"), j.getInt("date_of_month"));
                                r.setOrderId(j.getInt("id"));
                                db.rcOrderDetailsDao().insertRcOrderDetails(r);
                            }


                            JSONArray vacJson = resultArrayJson.getJSONArray("vac");
                            //Log.d("DD",vacJson.toString());
                            for (int i = 0; i < vacJson.length(); i++) {
                                JSONObject j = (JSONObject) vacJson.get(i);
                                db.vacationDao().addVacation(new Vacation(j.getInt("id"), j.getString("start_f"), j.getString("end_f")));
                            }

                            JSONArray walletJson = resultArrayJson.getJSONArray("wallet");
                            //Log.d("DD",walletJson.toString());
                            for (int i = 0; i < walletJson.length(); i++) {
                                JSONObject j = (JSONObject) walletJson.get(i);
                                db.walletTransactionDao().insertWalletTransaction(new WalletTransaction(j.getInt("id"), j.getInt("transaction_type"), j.getString("description"), j.getInt("transaction_amount"), j.getString("date")));
                            }

                            JSONArray rcoStatusJson = resultArrayJson.getJSONArray("rco_status");
                            //Log.d("DD",rcoStatusJson.toString());
                            for (int i = 0; i < rcoStatusJson.length(); i++) {
                                JSONObject j = (JSONObject) rcoStatusJson.get(i);
                                String key = j.getString("date") + j.getInt("rco_id");
                                //Log.d("DD", "Key: " + key);
                                editor.putInt(key, j.getInt("status"));
                                editor.commit();
                            }

                            return "OK";
                        } else {

                            return "NEWUSER";
                        }
                    } else if (resultJson.getInt("responseCode") == 275) {
                        return "NOLOGIN";
                    } else {
                        return "TIMEOUT";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {

                }

                return result;
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

    private class GetOtpFromServer extends AsyncTask<String, Void, String> {
        JSONObject userDetails;

        public GetOtpFromServer(JSONObject obj) {
            this.userDetails = obj;
        }

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to reach server..." + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            //Log.d("DD", "Result from server for sending otp: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    rl1.setVisibility(View.GONE);
                    rl2.setVisibility(View.VISIBLE);
                    pinInput.requestFocus();
                }
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
            int len = 3000;

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
                String result = readIt(is, len);
                return result;
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

}
