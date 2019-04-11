package in.dailydelivery.dailydelivery;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.Vacation;


public class UserHomeActivity extends AppCompatActivity implements DatePickerListener, OrdersDisplayRecylcerViewAdapter.DeleteOto {
    AppDatabase db;
    RecyclerView ordersforthedayRV, rcOrdersforthedayRV;
    OrdersDisplayRecylcerViewAdapter recylcerViewAdapter;
    RcOrdersDisplayRecyclerviewAdapter rcOrdersDisplayRecyclerviewAdapter;
    TextView noOrdersTV;
    boolean isLoadingFirstTime;
    List<RcOrderDetails> rcOrders;
    List<Vacation> vacations;
    DateTime dateSelected;
    DateTimeFormatter dtf;
    boolean ordersPresent;
    boolean oneTimeUpdate, displayingRcOrderFirstTime;
    SharedPreferences sharedPreferences;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        db = AppDatabase.getAppDatabase(this);
        // find the picker
        HorizontalPicker picker = findViewById(R.id.datePicker);

        // initialize it and attach a listener
        picker
                .setListener(this)
                .init();
        //picker.setBackgroundColor(Color.LTGRAY);
        isLoadingFirstTime = true;
        DateTime today = new DateTime();
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");

        if (getIntent().hasExtra("orderDate")) {
            today = dtf.parseDateTime(getIntent().getStringExtra("orderDate"));
        }
        picker.setDate(today);
        noOrdersTV = findViewById(R.id.noOrdersTV);
        ordersforthedayRV = findViewById(R.id.ordersforthedayRV);
        rcOrdersforthedayRV = findViewById(R.id.rcOrdersforthedayRV);

        LinearLayoutManager l1 = new LinearLayoutManager(this);
        LinearLayoutManager l2 = new LinearLayoutManager(this);
        ordersforthedayRV.setLayoutManager(l1);
        rcOrdersforthedayRV.setLayoutManager(l2);
        DividerItemDecoration dividerItemDecoration1 = new DividerItemDecoration(ordersforthedayRV.getContext(),
                l1.getOrientation());
        ordersforthedayRV.addItemDecoration(dividerItemDecoration1);

        DividerItemDecoration dividerItemDecoration2 = new DividerItemDecoration(rcOrdersforthedayRV.getContext(),
                l2.getOrientation());
        rcOrdersforthedayRV.addItemDecoration(dividerItemDecoration2);

        oneTimeUpdate = true;
        displayingRcOrderFirstTime = true;

        sharedPreferences = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        userId = sharedPreferences.getInt(getString(R.string.sp_tag_user_id), 12705);

        if (getIntent().getStringExtra("title") != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getIntent().getStringExtra("title"))
                    .setMessage(getIntent().getStringExtra("message"));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        createNotificationChannel();
        getSupportActionBar().setTitle("My Orders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onDateSelected(DateTime dateSelected) {
        this.dateSelected = dateSelected;
        new GetOrders(dateSelected.toString(dtf)).execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent userHomeActivityIntent = new Intent(this, CreateOrderActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent userHomeActivityIntent = new Intent(this, CreateOrderActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        finish();
    }

    @Override
    public void deleteOto(final int otoId) {
        //Toast.makeText(this,"Oto Id: " + otoId,Toast.LENGTH_LONG).show();
        if (checkForOrderDeletion()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            // Set the dialog title
            builder.setTitle("Confirm Order Delete");
            builder.setMessage("Are you sure you want to delete the order?");

            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String query = "orderId=" + otoId;
                    new DeleteOrder(query, otoId).execute(getString(R.string.server_addr_release) + "del_one_time_order.php");
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
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }
    }

    private boolean checkForOrderDeletion() {
        if (dateSelected.isBefore(new DateTime())) {
            Toast.makeText(this, "Sorry! Previous orders cannot be Deleted", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
            for (RcOrderDetails rcO : rcOrders) {
                RcOrderDetails rcOrderDetails = new RcOrderDetails(rcO.getProductId(), rcO.getCatId(), rcO.getName(), rcO.getDes(), rcO.getQtyDes(), rcO.getPrice(), rcO.getStatus(), rcO.getDeliverySlot(), rcO.getStartDate(), rcO.getMon(), rcO.getTue(), rcO.getWed(), rcO.getThu(), rcO.getFri(), rcO.getSat(), rcO.getSun(), rcO.getFrequency(), rcO.getDay1Qty(), rcO.getDay2Qty(), rcO.getDateOfMonth());
                rcOrderDetails.setOrderId(rcO.getOrderId());
                startDate = dtf.parseDateTime(rcOrderDetails.getStartDate());
                if (dateSelected.isAfter(startDate.minusDays(1))) {
                    int qty = 0;
                    //Log.d("dd", "Rc Orders frequency: " + rcOrderDetails.getFrequency() + rcOrderDetails.getDay1Qty() + rcOrderDetails.getDay2Qty() + rcOrderDetails.getDateOfMonth());
                    switch (rcOrderDetails.getFrequency()) {
                        case 1:
                            //Daily Order
                            int dayOfWeek = dateSelected.getDayOfWeek();
                            switch (dayOfWeek) {
                                case 1:
                                    qty = rcOrderDetails.getMon();
                                    break;
                                case 2:
                                    qty = rcOrderDetails.getTue();
                                    break;
                                case 3:
                                    qty = rcOrderDetails.getWed();
                                    break;
                                case 4:
                                    qty = rcOrderDetails.getThu();
                                    break;
                                case 5:
                                    qty = rcOrderDetails.getFri();
                                    break;
                                case 6:
                                    qty = rcOrderDetails.getSat();
                                    break;
                                case 7:
                                    qty = rcOrderDetails.getSun();
                                    break;
                            }
                            break;

                        case 2:
                            //Alternate Days order
                            long diffInMillis = dateSelected.getMillis() - startDate.getMillis();
                            long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                            if (diff % 2 == 0) {
                                qty = rcOrderDetails.getDay1Qty();
                            } else {
                                qty = rcOrderDetails.getDay2Qty();
                            }
                            break;
                        case 3:
                            //Monthly Order
                            //Log.d("DD", "Date selected: " + dateSelected.getDayOfMonth());
                            if (dateSelected.getDayOfMonth() == rcOrderDetails.getDateOfMonth()) {
                                qty = rcOrderDetails.getDay1Qty();
                            }
                            break;
                    }

                    if (qty == 0) {
                        continue;
                    }
                    String key = dateSelected.toString(dtf) + rcOrderDetails.getOrderId();
                    //Log.d("DD","Key: "+key);
                    int s = sharedPreferences.getInt(key, 273);
                    if (s != 273) {
                        if (s == 1) rcOrderDetails.setStatus(4);
                        else if (s == 2) rcOrderDetails.setStatus(7);
                        else if (s == 3) rcOrderDetails.setStatus(5);
                        else if (s == 4) rcOrderDetails.setStatus(6);
                    } else if (rcOrderDetails.getStatus() == 2) {
                        continue;
                    } else {
                        for (Vacation v : vacations) {
                            vStartDate = dtf.parseDateTime(v.getStartDate());
                            vEndDate = dtf.parseDateTime(v.getEndDate());
                            //Log.d("DD", "Start Date: " + v.getStartDate());
                            //Log.d("DD", "End Date: " + v.getEndDate());
                            if (dateSelected.isAfter(vStartDate.minusDays(1)) && dateSelected.isBefore(vEndDate.plusDays(1))) {
                                rcOrderDetails.setStatus(8);
                            }
                        }
                    }
                    rcOrdersForTheDay.add(rcOrderDetails);
                }

            }

            if (rcOrdersForTheDay.size() > 0) {
                ordersPresent = true;
                if (displayingRcOrderFirstTime) {
                    //Log.d("dd", "Setting Adapter");
                    rcOrdersDisplayRecyclerviewAdapter = new RcOrdersDisplayRecyclerviewAdapter(rcOrdersForTheDay, dateSelected.getDayOfWeek(), dateSelected);
                    rcOrdersforthedayRV.setAdapter(rcOrdersDisplayRecyclerviewAdapter);
                    displayingRcOrderFirstTime = false;
                } else {
                    //Log.d("dd", "Updating Rc Orders for the Day size: " + rcOrdersForTheDay.size() + ",Date selected " + dateSelected.toString(dtf));
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
                            //Log.d("MyFirebaseMsgService", "Token " + token);
                            String query = "userId=" + userId + "&serverKey=" + token;
                            new UpdateGCMToken(query).execute(getString(R.string.server_addr_release) + "insert_gcm_server_key.php");
                        }
                    });
        }
    }

    private void refreshActivity() {
        //this.recreate();
        /*Intent self = new Intent(this, UserHomeActivity.class);
        startActivity(self);
        finish();*/
        oneTimeUpdate = true;
        new GetOrders(dateSelected.toString(dtf)).execute();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Sandeep123", name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
                //Log.d("dd", "Rc Orders First time size: " + rcOrders.size());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateRecyclerView(orders);
        }
    }

    /*private class UpdateCartQty extends AsyncTask<Void, Void, Void> {
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
*/
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
                return "Check internet connection";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                //JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("result") == 273) {
                    //Log.d("MyFirebaseMsgService", "Token Logged at server");
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
                return "Check internet connection!";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Log.d("dd", "refreshing " + dateSelected.toString(dtf));
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

                //Log.d("DD", "Result from webserver: " + result);
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
