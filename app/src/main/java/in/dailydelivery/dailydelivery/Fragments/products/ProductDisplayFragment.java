package in.dailydelivery.dailydelivery.Fragments.products;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.ProductTuple;
import in.dailydelivery.dailydelivery.Fragments.products.Products.Product;
import in.dailydelivery.dailydelivery.R;

//import android.util.Log;


public class ProductDisplayFragment extends Fragment {

    JSONArray productList;
    RecyclerView recyclerView;
    Context context;
    Button goToCartBtn;
    int delivery_slot, deliverySlotInCart;
    AppDatabase db;
    List<ProductTuple> productsIdsInCart;
    private ProductDisplayFragmentInteractionListener mListener;
    private int cat_id;
    private int orderType;//1 for one time order; 2 for repeating order

    public ProductDisplayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        db = AppDatabase.getAppDatabase(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);

        // Set the adapter
        context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        goToCartBtn = view.findViewById(R.id.goToCartBtn);

        cat_id = getArguments().getInt("cat_id");
        delivery_slot = getArguments().getInt("delivery_slot");
        //Log.d("DD", "Delivery SLot: " + delivery_slot);
        orderType = getArguments().getInt("order_type");

        if (orderType == 2) {
            goToCartBtn.setVisibility(View.GONE);
        } else {
            goToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.goToCart();
                }
            });
        }
        mListener.setActionBarTitle("Products");
        //Toast.makeText(getActivity(),"order type: " + orderType + cat_id,Toast.LENGTH_LONG).show();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Create a JSONObject for sending to server
            JSONObject obj = new JSONObject();
            //get list of categories
            try {
                obj.put("cat_id", cat_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "products_req.php");
        } else {
            Toast.makeText(getActivity(), "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProductDisplayFragmentInteractionListener) {
            mListener = (ProductDisplayFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProductDisplayFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void displayProducts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(new MyProductRecyclerViewAdapter(Products.ITEMS, getActivity(), mListener, orderType, deliverySlotInCart));
    }


    public interface ProductDisplayFragmentInteractionListener {
        void productDisplayFragmentInteraction(Product item, int qty);

        void goToCart();

        void setActionBarTitle(String title);
    }

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
                return "Unable to reach server!";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(getActivity(), "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            } else {
                //Log.d("DD", "Result from webserver in Create Order Activity: " + result);
                try {
                    productList = new JSONArray(result);
                    new populateProducts().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                }
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
                conn.setConnectTimeout(10000); //* milliseconds *//*);
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

    private class populateProducts extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            productsIdsInCart = db.userDao().getpIdofCatId(cat_id);
            deliverySlotInCart = db.userDao().getDeliverySLotInCart();
            //Log.d("DD","deli " + deliverySlotInCart);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Products.ITEMS.clear();
            boolean presentInCart;
            int qty;
            for (int i = 0; i < productList.length(); i++) {
                presentInCart = false;
                qty = 0;
                try {
                    JSONObject obj = productList.getJSONObject(i);
                    int product_id = obj.getInt("id");
                    for (int j = 0; j < productsIdsInCart.size(); j++) {
                        if (product_id == productsIdsInCart.get(j).getProductId()) {
                            presentInCart = true;
                            qty = productsIdsInCart.get(j).getProductqty();
                        }
                    }
                    /*if (productsIdsInCart.size() > 0)
                        deliverySlotInCart = productsIdsInCart.get(0).getDelivery_slot();
                    else deliverySlotInCart = 0;
*/
                    String thumbnailUrl = obj.getString("thumbnail_url").replace("\\", "");
                    Product product = new Product(product_id, cat_id, obj.getString("name"), obj.getString("description"), obj.getInt("price"), obj.getInt("discount_price"), thumbnailUrl, presentInCart, qty, delivery_slot);
                    Products.addItem(product);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            displayProducts();
        }
    }
}

