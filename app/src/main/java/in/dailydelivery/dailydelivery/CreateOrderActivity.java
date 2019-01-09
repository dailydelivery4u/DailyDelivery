package in.dailydelivery.dailydelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
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
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.Cart;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.Fragments.CartDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.OrderDetailsFragment;
import in.dailydelivery.dailydelivery.Fragments.categories.Categories;
import in.dailydelivery.dailydelivery.Fragments.categories.CategoryDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.ProductDisplayFragment;
import in.dailydelivery.dailydelivery.Fragments.products.Products;

public class CreateOrderActivity extends AppCompatActivity implements CategoryDisplayFragment.CategoryDisplayFragmentInteractionListener, ProductDisplayFragment.ProductDisplayFragmentInteractionListener, CartDisplayFragment.OnCartDisplayFragmentInteractionListener, OrderDetailsFragment.OnOrderDetailsFragmentInteractionListener {

    FrameLayout fragmentFrame;
    AHBottomNavigation bottomNavigationView;
    AppDatabase db;
    ProgressDialog progress;
    SharedPreferences sharedPref;
    int walletBal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        progress = new ProgressDialog(this);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);

        Intent intent = getIntent();
        int fragmentNum = intent.getIntExtra("fragment", 0);

        fragmentFrame = findViewById(R.id.fragment_frame);
        if (fragmentNum == 3) {
            getSupportActionBar().setTitle("Cart");
            CartDisplayFragment frag = new CartDisplayFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, frag).commit();
        } else {
            getSupportActionBar().setTitle("Categories");
            CategoryDisplayFragment frag = new CategoryDisplayFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, frag).commit();
        }

        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Home", R.drawable.ic_home);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Categories", R.drawable.ic_apps);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Cart", R.drawable.ic_cart);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.addItem(item1);
        bottomNavigationView.addItem(item2);
        bottomNavigationView.addItem(item3);

        bottomNavigationView.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigationView.setAccentColor(Color.WHITE);
        bottomNavigationView.setInactiveColor(Color.WHITE);

        db = AppDatabase.getAppDatabase(this);

        new GetCartQty().execute();
        bottomNavigationView.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {
                    case 0:
                        Intent userHomeActIntent = new Intent(CreateOrderActivity.this, UserHomeActivity.class);
                        startActivity(userHomeActIntent);
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Categories");
                        CategoryDisplayFragment frag = new CategoryDisplayFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, frag).addToBackStack(null).commit();
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Cart");
                        CartDisplayFragment cartDisplayFragment = new CartDisplayFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, cartDisplayFragment).addToBackStack(null).commit();
                        break;
                }
                return true;
            }
        });
    }


    private void setCartNum(int num) {
        bottomNavigationView.setNotification(String.valueOf(num), 2);
    }

    @Override
    public void categoryFragmentInteraction(Categories.category item) {
        //Toast.makeText(this,"Item Clicked: "+ item.toString(),Toast.LENGTH_LONG).show();
        //Create new fragment containing prodcuts of the category
        ProductDisplayFragment productDisplayFragment = new ProductDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("cat_id", item.getId());
        bundle.putInt("delivery_slot", item.getDeliverySlot());
        bundle.putInt("order_type", 1);// 1 for 1 time order ; 2 for recurring order
        productDisplayFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, productDisplayFragment).addToBackStack(null).commit();
        getSupportActionBar().setTitle("Products");
    }

    @Override
    public void productDisplayFragmentInteraction(Products.Product item, int qty) {
        //Toast.makeText(this,"Item Clicked: "+ item.toString(),Toast.LENGTH_LONG).show();
        new dbAsyncTask(item).execute(qty);
    }

    @Override
    public void goToCart() {
        getSupportActionBar().setTitle("Cart");
        CartDisplayFragment cartDisplayFragment = new CartDisplayFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, cartDisplayFragment).addToBackStack(null).commit();
    }

    @Override
    public void onCartDisplayFragmentInteraction(int proceed) {
        if (proceed == 1) {
            getSupportActionBar().setTitle("Order Details");
            OrderDetailsFragment fragment = new OrderDetailsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, fragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void onOrderDetailsFragmentInteraction(String dateSelected, int deliverySlot) {
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
                obj.put("delivery_slot", deliverySlot);
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
            Reader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
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
                db.oneTimeOrderDetailsDao().insertOnetimeOrderDetails(new OneTimeOrderDetails(c.getUid(), c.getProductId(), c.getCatId(), c.getProductqty(), c.getProductName(), c.getProductDes(), c.getProductDdprice(), status, date, deliverySlot));
            }
            db.userDao().emptyCart();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Toast.makeText(CreateOrderActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrderActivity.this);
            builder.setTitle("Yay!! Order is scheduled")
                    .setMessage("Ensure sufficient wallet balance for order confirmation\n(See Help for more Info).\n" +
                            "Current wallet Balance: Rs." + walletBal);

            builder.setPositiveButton("Recharge Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent walletActivityIntent = new Intent(CreateOrderActivity.this, WalletActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(walletActivityIntent);
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.dismiss();
                    Intent userHomeActivityIntent = new Intent(CreateOrderActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(userHomeActivityIntent);
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
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
                    db.userDao().insertCart(new Cart(item.getCat_id(), item.getId(), item.getProductName(), item.getProductDes(), item.getMrp(), item.getDdPrice(), qty, item.getThumbnailUrl(), item.getDeliverySlot()));
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
}
