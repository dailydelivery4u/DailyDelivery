package in.dailydelivery.dailydelivery;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import java.util.ArrayList;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.Vacation;
import in.dailydelivery.dailydelivery.DB.WalletTransaction;


public class UserHomeActivity extends AppCompatActivity implements DatePickerListener, OrdersDisplayRecylcerViewAdapter.DeleteOto {
    AppDatabase db;
    RecyclerView ordersforthedayRV, rcOrdersforthedayRV;
    OrdersDisplayRecylcerViewAdapter recylcerViewAdapter;
    RcOrdersDisplayRecyclerviewAdapter rcOrdersDisplayRecyclerviewAdapter;
    TextView noOrdersTV;
    boolean isLoadingFirstTime;
    TextView cartQtyTV;
    ImageView cartImage;
    List<RcOrderDetails> rcOrders;
    List<Vacation> vacations;
    DateTime dateSelected;
    DateTimeFormatter dtf;
    boolean ordersPresent;
    boolean oneTimeUpdate, displayingRcOrderFirstTime;
    SharedPreferences sharedPreferences;
    int userId, orderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        db = AppDatabase.getAppDatabase(this);
        // find the picker
        HorizontalPicker picker = (HorizontalPicker) findViewById(R.id.datePicker);

        // initialize it and attach a listener
        picker
                .setListener(this)
                .init();
        //picker.setBackgroundColor(Color.LTGRAY);
        isLoadingFirstTime = true;
        DateTime today = new DateTime();
        picker.setDate(today);
        noOrdersTV = findViewById(R.id.noOrdersTV);
        ordersforthedayRV = findViewById(R.id.ordersforthedayRV);
        rcOrdersforthedayRV = findViewById(R.id.rcOrdersforthedayRV);
        ordersforthedayRV.setLayoutManager(new LinearLayoutManager(this));
        rcOrdersforthedayRV.setLayoutManager(new LinearLayoutManager(this));
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        oneTimeUpdate = true;
        displayingRcOrderFirstTime = true;

        sharedPreferences = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        userId = sharedPreferences.getInt(getString(R.string.sp_tag_user_id), 12705);
        if (!sharedPreferences.getBoolean(getString(R.string.gcm_token_saved_at_server_file_variable), false)) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("MyFirebaseMsgService", "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            Log.d("MyFirebaseMsgService", "Token " + token);
                            String query = "userId=" + userId + "&serverKey=" + token;
                            new UpdateGCMToken(query).execute(getString(R.string.server_addr_release) + "insert_gcm_server_key.php");
                            /*Uri.Builder uri = new Uri.Builder();
                            uri.scheme("http")
                                    .authority("www.scoollife.com")
                                    .appendPath("dd")
                                    .appendPath("insert_gcm_server_key.php");
                            String url = uri.build().toString();
                            */
                            //new UpdateGCMToken(query).execute(url);
                        }
                    });
        }

        checkForUserUpdates();
    }

    private void checkForUserUpdates() {
        String query = "userId=" + userId;
        new CheckForUserUpdatesFromServer(query).execute(getString(R.string.server_addr_release) + "check_for_user_updates.php");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.cartItem).getActionView();
        cartQtyTV = badgeLayout.findViewById(R.id.actionbar_notifcation_textview);
        cartImage = badgeLayout.findViewById(R.id.cartImage);
        cartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserHomeActivity.this, CreateOrderActivity.class);
                intent.putExtra("fragment", 3);
                startActivity(intent);
            }
        });
        new UpdateCartQty().execute();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wallet:
                Intent intent = new Intent(UserHomeActivity.this, WalletActivity.class);
                startActivity(intent);
                break;

            case R.id.vacation:
                Intent intent1 = new Intent(UserHomeActivity.this, VacationActivity.class);
                startActivity(intent1);
                break;

            case R.id.accountHistory:
                Intent intent2 = new Intent(UserHomeActivity.this, AccountHistoryActivity.class);
                startActivity(intent2);
                break;

            case R.id.logout:
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Intent loginIntent = new Intent(UserHomeActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        db.clearAllTables();
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putBoolean("logged_in", false);
                        e.commit();
                        return null;
                    }
                }.execute();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSelected(DateTime dateSelected) {
        this.dateSelected = dateSelected;
        new GetOrders(dateSelected.toString(dtf)).execute();
    }

    public void createOrderBtnOnClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Choose Order Type");
        orderType = 0;
        builder.setSingleChoiceItems(R.array.order_type_array, orderType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderType = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (orderType == 0) {
                    //One time order selected
                    Intent createOrderActivityIntent = new Intent(UserHomeActivity.this, CreateOrderActivity.class);
                    startActivity(createOrderActivityIntent);
                } else {
                    //Reccurring order selected
                    Intent createOrderActivityIntent = new Intent(UserHomeActivity.this, CreateRecurringOrderActivity.class);
                    startActivity(createOrderActivityIntent);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = builder.create();
        mDialog.show();
    }


    @Override
    public void deleteOto(int otoId) {
        //Toast.makeText(this,"Oto Id: " + otoId,Toast.LENGTH_LONG).show();
        if (checkForOrderDeletion()) {
            String query = "orderId=" + otoId;
            new DeleteOrder(query, otoId).execute(getString(R.string.server_addr_release) + "del_one_time_order.php");
        }
    }

    private boolean checkForOrderDeletion() {
        if (dateSelected.isBefore(new DateTime())) {
            Toast.makeText(this, "Sorry! Previous orders cannot be Deleted", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private class GetOrders extends AsyncTask<Void, Void, Void> {
        String date;
        List<OneTimeOrderDetails> orders;

        public GetOrders(String date) {
            this.date = date;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orders = db.oneTimeOrderDetailsDao().getOrdersForTheDay(date);
            if (oneTimeUpdate) {
                rcOrders = db.rcOrderDetailsDao().getRcOrders();
                vacations = db.vacationDao().getAll();
                oneTimeUpdate = false;
                Log.d("dd", "Rc Orders First time size: " + rcOrders.size());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateRecyclerView(orders);
        }
    }

    private void updateRecyclerView(List<OneTimeOrderDetails> orders) {
        if (isLoadingFirstTime) {
            if (orders.size() > 0) {
                ordersPresent = true;
                //ordersforthedayRV.setVisibility(View.VISIBLE);
                //noOrdersTV.setVisibility(View.GONE);
                recylcerViewAdapter = new OrdersDisplayRecylcerViewAdapter(orders, this);
                ordersforthedayRV.setAdapter(recylcerViewAdapter);
                isLoadingFirstTime = false;
            } else ordersPresent = false;
        } else {
            if (orders.size() > 0) {
                ordersPresent = true;
                ordersforthedayRV.setVisibility(View.VISIBLE);
                noOrdersTV.setVisibility(View.GONE);
                recylcerViewAdapter.updateData(orders);
            } else {
                ordersforthedayRV.setVisibility(View.GONE);
                noOrdersTV.setVisibility(View.VISIBLE);
                ordersPresent = false;
            }
        }
        displayRcOrders();
    }

    private void displayRcOrders() {
        //rcOrdersForTheDay.clear();
        //Log.d("dd","Rc Orders size: " + rcOrders.size());
        if (rcOrders.size() > 0) {
            List<RcOrderDetails> rcOrdersForTheDay = new ArrayList<>();
            DateTime startDate, vStartDate, vEndDate;
            for (RcOrderDetails rcOrderDetails : rcOrders) {
                //Log.d("DD","Start Date: " + rcOrderDetails.getStartDate());
                startDate = dtf.parseDateTime(rcOrderDetails.getStartDate());
                if (dateSelected.isAfter(startDate.minusDays(1))) {
                    String key = rcOrderDetails.getStartDate() + rcOrderDetails.getOrderId();
                    int s = sharedPreferences.getInt(key, 273);
                    if (s != 273) {
                        if (s == 1) rcOrderDetails.setStatus(4);
                        else if (s == 2) rcOrderDetails.setStatus(7);
                        else if (s == 3) rcOrderDetails.setStatus(5);
                        else if (s == 4) rcOrderDetails.setStatus(6);
                    } else {
                        for (Vacation v : vacations) {
                            vStartDate = dtf.parseDateTime(v.getStartDate());
                            vEndDate = dtf.parseDateTime(v.getEndDate());
                            if (dateSelected.isAfter(vStartDate.minusDays(1)) && dateSelected.isBefore(vEndDate.plusDays(1))) {
                                rcOrderDetails.setStatus(2);
                            }
                        }
                    }
                    rcOrdersForTheDay.add(rcOrderDetails);
                    //Log.d("dd","Rc Orders for the Day size: " + rcOrdersForTheDay.size() + ",Date selected " + dateSelected.toString(dtf) + ", StartDate: "+ startDate.toString(dtf));
                }
            }
            //Log.d("dd","Rc Orders for the Day size: " + rcOrdersForTheDay.size() + ",Date selected " + dateSelected.toString(dtf));

            if (rcOrdersForTheDay.size() > 0) {
                ordersPresent = true;
                if (displayingRcOrderFirstTime) {
                    Log.d("dd", "Setting Adapter");
                    rcOrdersDisplayRecyclerviewAdapter = new RcOrdersDisplayRecyclerviewAdapter(rcOrdersForTheDay, dateSelected.getDayOfWeek());
                    rcOrdersforthedayRV.setAdapter(rcOrdersDisplayRecyclerviewAdapter);
                    displayingRcOrderFirstTime = false;
                } else {
                    Log.d("dd", "Updating Rc Orders for the Day size: " + rcOrdersForTheDay.size() + ",Date selected " + dateSelected.toString(dtf));
                    rcOrdersDisplayRecyclerviewAdapter.updateData(rcOrdersForTheDay, dateSelected.getDayOfWeek());
                }
            } else {
                if (!displayingRcOrderFirstTime) {
                    rcOrdersDisplayRecyclerviewAdapter.clearData();
                }
            }
        }

        if (!ordersPresent) {
            noOrdersTV.setVisibility(View.VISIBLE);
        } else {
            noOrdersTV.setVisibility(View.GONE);
        }
    }

    private class UpdateCartQty extends AsyncTask<Void, Void, Void> {
        int qty;

        @Override
        protected Void doInBackground(Void... voids) {
            qty = db.userDao().count();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            cartQtyTV.setText(String.valueOf(qty));
        }
    }

    private class UpdateGCMToken extends AsyncTask<String, Void, String> {
        String query;

        public UpdateGCMToken(String query) {
            this.query = query;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                //JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("result") == 273) {
                    Log.d("MyFirebaseMsgService", "Token Logged at server");
                    //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.gcm_token_saved_at_server_file_variable), true);
                    editor.apply();
                }
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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//* milliseconds *//*);
                conn.setConnectTimeout(15000); //* milliseconds *//*);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                ;
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
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

    private class CheckForUserUpdatesFromServer extends AsyncTask<String, Void, String> {
        String query;

        public CheckForUserUpdatesFromServer(String query) {
            this.query = query;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(UserHomeActivity.this, result, Toast.LENGTH_SHORT).show();
            if (result.equals("Updating...")) {
                refreshActivity();
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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//* milliseconds *//*);
                conn.setConnectTimeout(15000); //* milliseconds *//*);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                ;
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
                out.flush();
                out.close();
                is = conn.getInputStream();

                String result = readIt(is, len);
                Log.d("DD", "Result from server - user updates: " + result);
                if (is != null) {
                    is.close();
                }
                if (result.equals("timeout")) {
                    return "TIMEOUT";
                }
                JSONObject resultArrayJson = new JSONObject(result);
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    int updateCnt = resultJson.getInt("row_cnt");
                    Log.d("DD", "Row Count: " + updateCnt);
                    if (updateCnt == 0) return "NOUP";
                    JSONArray updateTypes = resultArrayJson.getJSONArray("update_types");
                    for (int i = 0; i < updateCnt; i++) {
                        JSONObject j = resultArrayJson.getJSONObject("result_details" + i);
                        switch (updateTypes.getInt(i)) {
                                /*1:Add OTO, 2: Add RCO, 3: Modify OTO, 4: Modify RCO, 5: Del OTO, 6: Del RCO, 7: Add Vac, 8: Modify Vac,
                                9: Del Vacation, 10: Add Wallet Transaction; 11 - oto status update, 12 - rco status update; 13- rco hold udpdate */
                            case 1:
                                db.oneTimeOrderDetailsDao().insertOnetimeOrderDetails(new OneTimeOrderDetails(j.getInt("id"), j.getInt("product_id"), j.getInt("cat_id"), j.getInt("qty"),
                                        j.getString("name"), j.getString("description"), j.getInt("discount_price"), j.getInt("status"), j.getString("order_date"), j.getInt("delivery_slot")));
                                break;
                            case 2:
                                RcOrderDetails r = new RcOrderDetails(j.getInt("p_id"), j.getInt("cat_id"), j.getString("name"), j.getString("description"), j.getInt("discount_price"),
                                        j.getInt("status"), j.getInt("delivery_slot"), j.getString("order_date"), j.getInt("mon"), j.getInt("tue"), j.getInt("wed"), j.getInt("thu"),
                                        j.getInt("fri"), j.getInt("sat"), j.getInt("sun"));
                                r.setOrderId(j.getInt("id"));
                                db.rcOrderDetailsDao().insertRcOrderDetails(r);
                                break;
                            case 3:
                                db.oneTimeOrderDetailsDao().updateOrder(j.getInt("qty"), j.getInt("id"));
                                break;
                            case 4:
                                db.rcOrderDetailsDao().updateOrder(j.getInt("mon"), j.getInt("tue"), j.getInt("wed"), j.getInt("thu"), j.getInt("fri"), j.getInt("sat"), j.getInt("sun"), j.getInt("id"));
                                break;
                            case 5:
                                db.oneTimeOrderDetailsDao().deleteByOrderId(j.getInt("uniqueId"));
                                break;
                            case 6:
                                db.rcOrderDetailsDao().deleteByOrderId(j.getInt("uniqueId"));
                                break;
                            case 7:
                                db.vacationDao().addVacation(new Vacation(j.getInt("id"), j.getString("start_f"), j.getString("end_f")));
                                break;
                            case 8:
                                db.vacationDao().updateVac(j.getString("start_f"), j.getString("end_f"), j.getInt("id"));
                                break;
                            case 9:
                                db.vacationDao().deleteVac(j.getInt("uniqueId"));
                                break;
                            case 10:
                                db.walletTransactionDao().insertWalletTransaction(new WalletTransaction(j.getInt("id"), j.getInt("transaction_type"), j.getString("description"), j.getInt("transaction_amount"), j.getString("date")));
                                break;
                            case 11:
                                db.oneTimeOrderDetailsDao().updateStatus(j.getInt("status"), j.getInt("id"));
                                break;
                            case 12:
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                String key = j.getString("date") + j.getInt("rco_id");
                                editor.putInt(key, j.getInt("status"));
                                editor.commit();
                                break;
                            case 13:
                                db.rcOrderDetailsDao().updateStatus(j.getInt("status"), j.getInt("id"));
                                break;
                        }
                    }
                    return "Updating...";
                } else {
                    return "";
                }
            } catch (SocketTimeoutException e) {
                return "timeout";
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
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

    private void refreshActivity() {
        this.recreate();
    }

    private class DeleteOrder extends AsyncTask<String, Void, String> {
        String query;
        int orderId;

        public DeleteOrder(String query, int orderId) {
            this.query = query;
            this.orderId = orderId;
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("dd", "refreshing " + dateSelected.toString(dtf));
            new GetOrders(dateSelected.toString(dtf)).execute();
            //refreshActivity();
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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//* milliseconds *//*);
                conn.setConnectTimeout(15000); //* milliseconds *//*);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                ;
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
                out.flush();
                out.close();
                is = conn.getInputStream();

                String result = readIt(is, len);

                Log.d("DD", "Result from webserver: " + result);
                try {
                    JSONObject resultJson = new JSONObject(result);
                    //Log.d("DD","Result:" + resultJson.getInt("result") + orderId);
                    if (resultJson.getInt("result") == 273) {
                        db.oneTimeOrderDetailsDao().deleteByOrderId(orderId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (SocketTimeoutException e) {
                return "timeout";
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return "ok";
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
