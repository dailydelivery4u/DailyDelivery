package in.dailydelivery.dailydelivery;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import org.json.JSONArray;
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
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.Vacation;
import in.dailydelivery.dailydelivery.DB.WalletTransaction;
import in.dailydelivery.dailydelivery.Fragments.CartDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.OrderDetailsFragment;
import in.dailydelivery.dailydelivery.Fragments.RCOrderDetailsFragment;
import in.dailydelivery.dailydelivery.Fragments.categories.Category;
import in.dailydelivery.dailydelivery.Fragments.categories.CategoryDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.ProductDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.Products;

public class CreateOrderActivity extends AppCompatActivity implements CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener,
        ProductDisplayFragment.ProductDisplayFragmentInteractionListener, CartDisplayFragment.OnCartDisplayFragmentInteractionListener, OrderDetailsFragment.OnOrderDetailsFragmentInteractionListener,
        RCOrderDetailsFragment.RCOrderDetailsFragmentInteractionListener {

    FrameLayout fragmentFrame;
    AHBottomNavigation bottomNavigationView;
    AppDatabase db;
    ProgressDialog progress;
    SharedPreferences sharedPref;
    int walletBal;
    RcOrderDetails rcOrderDetails;
    TextView goToCartBtn, cartQtyTV;
    RelativeLayout bottomRL;
    AppCompatTextView actionBarTitleTV;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        progress = new ProgressDialog(this);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);

        bottomRL = findViewById(R.id.bottomLL);
        goToCartBtn = findViewById(R.id.goToCartBtn);
        cartQtyTV = findViewById(R.id.cartQtyTV);

        goToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartDisplayFragment cartDisplayFragment = new CartDisplayFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, cartDisplayFragment).addToBackStack(null).commit();
                displayBottomLL(false);
            }
        });
        userId = sharedPref.getInt(getString(R.string.sp_tag_user_id), 12705);

        getSupportActionBar().setElevation(0);
        //Intent intent = getIntent();
        //int fragmentNum = intent.getIntExtra("fragment", 0);

        fragmentFrame = findViewById(R.id.fragment_frame);
        /*if (fragmentNum == 3) {
            CartDisplayFragment frag = new CartDisplayFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, frag).commit();
        } else {*/
        CategoryDisplayFragment frag = new CategoryDisplayFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, frag).commit();
        //}

        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Home", R.drawable.ic_home);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("My Repeat Orders", R.drawable.ic_repeat);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Wallet", R.drawable.ic_wallet);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.addItem(item1);
        bottomNavigationView.addItem(item2);
        bottomNavigationView.addItem(item3);

        /*bottomNavigationView.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigationView.setAccentColor(Color.WHITE);
        bottomNavigationView.setInactiveColor(Color.WHITE);
        */
        bottomNavigationView.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        bottomNavigationView.setAccentColor(ContextCompat.getColor(this, R.color.darkBlue));
        bottomNavigationView.setInactiveColor(ContextCompat.getColor(this, R.color.darkBlue));
        bottomNavigationView.setUseElevation(true);
        //bottomNavigationView.setTitleTextSizeInSp(15,13);

        db = AppDatabase.getAppDatabase(this);

        new GetCartQty().execute();
        bottomNavigationView.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        /*Intent userHomeActIntent = new Intent(CreateOrderActivity.this, UserHomeActivity.class);
                        startActivity(userHomeActIntent);*/
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        CategoryDisplayFragment catfrag = new CategoryDisplayFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, catfrag).commit();
                        break;
                    case 1:
                        setActionBarTitle("Daily Delivery");
                        displayBottomLL(false);
                        ReccurringOrdersActivity frag = new ReccurringOrdersActivity();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, frag).commit();
                        break;
                    case 2:
                        setActionBarTitle("My DD Wallet");
                        displayBottomLL(false);
                        WalletFragment cartDisplayFragment = new WalletFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("wallet_bal", walletBal);
                        cartDisplayFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, cartDisplayFragment).commit();
                        break;
                }
                return true;
            }
        });
        //shouldDisplayHomeUp();
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_frame);
                if (!(f instanceof CategoryDisplayFragment) && !(f instanceof ReccurringOrdersActivity) && !(f instanceof WalletFragment)) {
                    shouldDisplayHomeUp();
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher_orange);
        displayWalletBal();
        Intent intent = getIntent();
        handleIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //Toast.makeText(this,query,Toast.LENGTH_LONG).show();

            ProductDisplayFragment productDisplayFragment = new ProductDisplayFragment();
            Bundle bundle = new Bundle();
            bundle.putString("search_query", query);
            productDisplayFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, productDisplayFragment).addToBackStack(null).commit();
            checkActiveFragment();
        }
    }

    private void displayWalletBal() {
        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int userId = sharedPref.getInt(getString(R.string.sp_tag_user_id), 273);
            List<AbstractMap.SimpleEntry> params = new ArrayList<AbstractMap.SimpleEntry>();
            params.add(new AbstractMap.SimpleEntry("user_id", userId));
            try {
                new GetBalance(getQuery(params)).execute(getString(R.string.server_addr_release) + "wallet/getBalance.php");
                //new GetBalance(getQuery(params)).execute(getString(R.string.server_addr_release) + "categories_req.php");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            updateBalance("--");
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        //RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.cartItem).getActionView();
        //cartQtyTV = badgeLayout.findViewById(R.id.actionbar_notifcation_textview);
        //cartImage = badgeLayout.findViewById(R.id.cartImage);
        /*cartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserHomeActivity.this, CreateOrderActivity.class);
                intent.putExtra("fragment", 3);
                startActivity(intent);
            }
        });
        new UpdateCartQty().execute();*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myOrders:
                Intent intent = new Intent(CreateOrderActivity.this, UserHomeActivity.class);
                startActivity(intent);
                break;

            case R.id.profile:
                Intent intent2 = new Intent(CreateOrderActivity.this, ProfileActivity.class);
                startActivity(intent2);
                break;

            case R.id.help:
                Intent intent3 = new Intent(CreateOrderActivity.this, HelpActivity.class);
                startActivity(intent3);
                break;

            case R.id.logout:
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Intent loginIntent = new Intent(CreateOrderActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        db.clearAllTables();
                        SharedPreferences.Editor e = sharedPref.edit();
                        e.clear();
                        e.commit();
                        return null;
                    }
                }.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
        return true;
    }

    private void checkActiveFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_frame);
        if (f instanceof ProductDisplayFragment || f instanceof CategoryDisplayFragment) {
            new GetCartQty().execute();
        }
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void displayBottomLL(boolean display) {
        if (display) {
            goToCartBtn.setEnabled(true);
            bottomRL.setVisibility(View.VISIBLE);
        } else {
            goToCartBtn.setEnabled(false);
            bottomRL.setVisibility(View.GONE);
        }
    }


    private void setCartNum(int num) {
        String qty = num + "Items";
        if (num == 0) {
            goToCartBtn.setEnabled(false);
            bottomRL.setVisibility(View.GONE);
        } else {
            goToCartBtn.setEnabled(true);
            bottomRL.setVisibility(View.VISIBLE);
            cartQtyTV.setText(qty);
        }
        //String bal = getString(R.string.Rs) + num;
        //bottomNavigationView.setNotification(bal, 2);
    }

    @Override
    public void categoryFragmentInteraction(Category item) {
        //Toast.makeText(this,"Item Clicked: "+ item.toString(),Toast.LENGTH_LONG).show();
        //Create new fragment containing prodcuts of the category
        ProductDisplayFragment productDisplayFragment = new ProductDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("cat_id", item.getId());
        bundle.putString("cat_name", item.getCatName());
        bundle.putInt("delivery_slot", item.getDeliverySlot());
        productDisplayFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, productDisplayFragment).addToBackStack(null).commit();
        checkActiveFragment();
    }

    @Override
    public void productDisplayFragmentInteraction(Products.Product item, int qty) {
        //Toast.makeText(this,"Item Clicked: "+ item.toString(),Toast.LENGTH_LONG).show();
        new dbAsyncTask(item).execute(qty);
    }

    @Override
    public void repeatBtnClicked(Products.Product item) {
        displayBottomLL(false);
        RCOrderDetailsFragment frag = new RCOrderDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable("product", item);
        frag.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, frag).addToBackStack(null).commit();
    }

    @Override
    public void onCartDisplayFragmentInteraction(String dateSelected) {
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
                //DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
                obj.put("date", dateSelected);
                //Toast.makeText(CreateOrderActivity.this,dateTime.toString(dtf),Toast.LENGTH_SHORT).show();
                obj.put("delivery_slot", 1);
                for (Cart c : OrderDetailsFragment.cartList) {
                    JSONObject cartDetails = new JSONObject();
                    cartDetails.put("cat_id", c.getCatId());
                    cartDetails.put("p_id", c.getProductId());
                    cartDetails.put("qty", c.getProductqty());
                    jsonArray.put(cartDetails);
                }
                obj.put("cart_details", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //new RegisterActivity.GetHashFromServer(obj).execute(getString(R.string.server_addr) + "add_user.php");
            new PlaceOrder(obj).execute(getString(R.string.server_addr_release) + "add_one_time_order.php");
        } else {
            Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------
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
                obj.put("freq", rcOrderDetails.getFrequency());
                obj.put("day1", rcOrderDetails.getDay1Qty());
                obj.put("day2", rcOrderDetails.getDay2Qty());
                obj.put("date_of_month", rcOrderDetails.getDateOfMonth());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //new RegisterActivity.GetHashFromServer(obj).execute(getString(R.string.server_addr) + "add_user.php");
            new PlaceRcOrder(obj).execute(getString(R.string.server_addr_release) + "add_rc_order.php");
        }
    }

    private void updateBalance(String balance) {
        String bal = getString(R.string.Rs) + balance;
        bottomNavigationView.setNotification(bal, 2);
    }

    @Override
    public void setActionBarTitle(String title) {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_frame);
        if (!(f instanceof CategoryDisplayFragment) && !(f instanceof ReccurringOrdersActivity) && !(f instanceof WalletFragment)) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
            getSupportActionBar().setDisplayShowCustomEnabled(false);
            getSupportActionBar().setTitle(title);
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);

            getSupportActionBar().setCustomView(R.layout.actionbar);
            View v = getSupportActionBar().getCustomView();
            //getSupportActionBar().setTitle("");
            actionBarTitleTV = v.findViewById(R.id.tvTitle);
            actionBarTitleTV.setText(title);
        }


    }

    @Override
    public void showBottom() {
        checkActiveFragment();
    }

    @Override
    public void showBottomLL() {
        checkActiveFragment();
    }

    @Override
    public void onOrderDetailsFragmentInteraction(String dateSelected, int deliverySlot) {

    }

    private void checkForUserUpdates() {
        String query = "userId=" + userId;
        new CheckForUserUpdatesFromServer(query).execute(getString(R.string.server_addr_release) + "check_for_user_updates.php");
    }

    private class PlaceRcOrder extends AsyncTask<String, Void, String> {
        JSONObject orderDetails;

        public PlaceRcOrder(JSONObject orderDetails) {
            this.orderDetails = orderDetails;
        }

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to reach server...";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            if (result.equals("Error")) {
                Toast.makeText(CreateOrderActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            } else if (result.equals("ok")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrderActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Yay!! Order is scheduled")
                        .setMessage("Ensure sufficient wallet balance for daily order confirmations.\n(See Help for more Info).\nYou can Pause " +
                                "repeating orders by placing vacations.");

                builder.setNegativeButton("Recharge Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent walletActivityIntent = new Intent(CreateOrderActivity.this, WalletFragment.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(walletActivityIntent);
                        finish();
                    }
                });
                builder.setPositiveButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        Intent userHomeActivityIntent = new Intent(CreateOrderActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(userHomeActivityIntent);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
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
                String result = readIt(is, len);
                if (result.equals("timeout")) {
                    return "Error";
                } else {
                    JSONObject resultArrayJson = new JSONObject(result);
                    //Check for Result COde
                    //If result is OK, update user Id in editor
                    JSONObject resultJson = resultArrayJson.getJSONObject("result");
                    if (resultJson.getInt("responseCode") == 273) {
                        int orderId = resultJson.getInt("order_id");
                        rcOrderDetails.setOrderId(orderId);
                        db.rcOrderDetailsDao().insertRcOrderDetails(rcOrderDetails);
                        return "ok";

                        //rcOrderDetails.setStatus(1);
                        //new UpdateOrderInDb().execute();
                    } else if (resultJson.getInt("responseCode") == 275) {
                        return "Error";
                    } else {
                        return "Error";
                    }
                }
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (SocketTimeoutException | JSONException e) {
                return "Error";
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
                //Toast.makeText(getActivity(), "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Wallet balance from webserver: " + result);
            try {
                JSONObject resultJson = new JSONObject(result);
                walletBal = resultJson.getInt("balance");
                updateBalance(String.valueOf(walletBal));
                checkForUserUpdates();
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
                return "Unable to Connect to Server";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(CreateOrderActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    int orderStatus = 1;
                    walletBal = resultJson.getInt("wallet");
                    for (Cart c : OrderDetailsFragment.cartList) {
                        c.setUid(resultArrayJson.getInt(String.valueOf(c.getProductId())));
                    }
                    //Toast.makeText(CreateOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                    new UpdateOrderInDb(orderDetails.getString("date"), orderDetails.getInt("delivery_slot"), orderStatus).execute();

                } else if (resultJson.getInt("responseCode") == 275) {
                    Toast.makeText(CreateOrderActivity.this, "Some Error occured! Pls try again", Toast.LENGTH_LONG).show();


                } else {
                    Toast.makeText(CreateOrderActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
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

    private class UpdateOrderInDb extends AsyncTask<Void, Void, Void> {
        List<Cart> cartItems;
        String date;
        int deliverySlot;
        int status;

        public UpdateOrderInDb(String date, int deliverySlot, int status) {
            this.date = date;
            this.deliverySlot = deliverySlot;
            this.status = status;
            cartItems = OrderDetailsFragment.cartList;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (Cart c : cartItems) {
                //Log.d("dd", "Pid - " + String.valueOf(c.getProductId()) + "UId: " + String.valueOf(c.getUid()));
                db.oneTimeOrderDetailsDao().insertOnetimeOrderDetails(new OneTimeOrderDetails(c.getUid(), c.getProductId(), c.getCatId(), c.getProductqty(), c.getProductName(), c.getProductDes(), c.getProductQtyDes(), c.getProductDdprice(), status, date, deliverySlot));
            }
            db.userDao().emptyCart();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Toast.makeText(CreateOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrderActivity.this);
            builder.setCancelable(false);
            builder.setTitle("Yay!! Order is scheduled")
                    .setMessage("Ensure sufficient wallet balance for order confirmation.\n(See Help for more Info)\n" +
                            "Current wallet Balance: Rs." + walletBal);

            builder.setNegativeButton("Recharge Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent walletActivityIntent = new Intent(CreateOrderActivity.this, WalletFragment.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(walletActivityIntent);
                    finish();
                }
            });
            builder.setPositiveButton("Later", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.dismiss();
                    Intent userHomeActivityIntent = new Intent(CreateOrderActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    userHomeActivityIntent.putExtra("orderDate", date);
                    startActivity(userHomeActivityIntent);
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private class dbAsyncTask extends AsyncTask<Integer, Void, Integer> {
        private Products.Product item;

        public dbAsyncTask(Products.Product item) {
            this.item = item;
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            int qty = integers[0];

            if (qty == 1) {
                if (db.userDao().getId(item.getId()) > 0) {
                    //Item already exists, update qty
                    db.userDao().updateQty(item.getId(), item.getCat_id(), qty);
                } else {
                    //new item. insert in db
                    db.userDao().insertCart(new Cart(item.getCat_id(), item.getId(), item.getProductName(), item.getProductDes(), item.getProductQty(), item.getMrp(), item.getDdPrice(), qty, item.getThumbnailUrl(), item.getDeliverySlot()));
                }
            } else if (qty == 0) {
                //delete product from db
                db.userDao().deleteProd(item.getId(), item.getCat_id());
            } else if (qty > 0) {
                //Item quantity updated
                db.userDao().updateQty(item.getId(), item.getCat_id(), qty);
            }
            return db.userDao().count();
        }

        @Override
        protected void onPostExecute(Integer cart_qty) {
            setCartNum(cart_qty);
        }
    }

    private class GetCartQty extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return db.userDao().count();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            setCartNum(integer);
        }
    }

    private class CheckForUserUpdatesFromServer extends AsyncTask<String, Integer, String> {
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
                return "Pls Check internet connection";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(UserHomeActivity.this, result, Toast.LENGTH_SHORT).show();
            if (result.equals("Updating...")) {

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
                //Log.d("DD", "Result from server - user updates: " + result);
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
                    //Log.d("DD", "Row Count: " + updateCnt);
                    if (updateCnt == 0) return "NOUP";
                    JSONArray updateTypes = resultArrayJson.getJSONArray("update_types");
                    for (int i = 0; i < updateCnt; i++) {
                        JSONObject j = resultArrayJson.getJSONObject("result_details" + i);
                        switch (updateTypes.getInt(i)) {
                                /*1:Add OTO, 2: Add RCO, 3: Modify OTO, 4: Modify RCO, 5: Del OTO, 6: Del RCO, 7: Add Vac, 8: Modify Vac,
                                9: Del Vacation, 10: Add Wallet Transaction; 11 - oto status update, 12 - rco status update; 13- rco hold udpdate */
                            case 1:
                                db.oneTimeOrderDetailsDao().insertOnetimeOrderDetails(new OneTimeOrderDetails(j.getInt("id"), j.getInt("product_id"), j.getInt("cat_id"), j.getInt("qty"),
                                        j.getString("name"), j.getString("description"), j.getString("des_qty"), j.getInt("discount_price"), j.getInt("status"), j.getString("order_date"), j.getInt("delivery_slot")));
                                break;
                            case 2:
                                RcOrderDetails r = new RcOrderDetails(j.getInt("p_id"), j.getInt("cat_id"), j.getString("name"), j.getString("description"), j.getString("des_qty"), j.getInt("discount_price"),
                                        j.getInt("status"), j.getInt("delivery_slot"), j.getString("order_date"), j.getInt("mon"), j.getInt("tue"), j.getInt("wed"), j.getInt("thu"),
                                        j.getInt("fri"), j.getInt("sat"), j.getInt("sun"), j.getInt("frequency"), j.getInt("day1_qty"), j.getInt("day2_qty"), j.getInt("date_of_month"));
                                r.setOrderId(j.getInt("id"));
                                db.rcOrderDetailsDao().insertRcOrderDetails(r);
                                break;
                            case 3:
                                db.oneTimeOrderDetailsDao().updateOrder(j.getInt("qty"), j.getInt("id"));
                                break;
                            case 4:
                                db.rcOrderDetailsDao().updateOrder(j.getInt("mon"), j.getInt("tue"), j.getInt("wed"), j.getInt("thu"), j.getInt("fri"), j.getInt("sat"), j.getInt("sun"), j.getInt("id"), j.getInt("day1_qty"), j.getInt("day2_qty"), j.getInt("date_of_month"));
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
                                SharedPreferences.Editor editor = sharedPref.edit();
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
}
