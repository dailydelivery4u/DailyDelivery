package in.dailydelivery.dailydelivery.Fragments.categories;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;

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
import java.util.ArrayList;

import in.dailydelivery.dailydelivery.R;


public class CategoryDisplayFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    ArrayList<CatDataModel> catDataModel;
    Context context;
    RecyclerView catTyperecyclerView;
    //ProgressDialog progress;
    SearchView searchView;
    private SliderLayout mDemoSlider;
    private CategoryDisplayFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategoryDisplayFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorydisplay_list, container, false);

        context = view.getContext();
        catTyperecyclerView = view.findViewById(R.id.list);
        mDemoSlider = view.findViewById(R.id.slider);
        mListener.setActionBarTitle("Daily Delivery");
        mListener.showBottom();
        searchView = view.findViewById(R.id.searchView);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        catDataModel = new ArrayList<>();
        ArrayList<String> listUrl = new ArrayList<>();
        listUrl.add(getString(R.string.server_addr_release) + "pics/Milk_banner.jpg");
        listUrl.add(getString(R.string.server_addr_release) + "pics/Referral_banner.jpg");
        listUrl.add(getString(R.string.server_addr_release) + "pics/summer_banner.jpg");


        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        //requestOptions.centerInside();
        //requestOptions.fitCenter();


        for (int i = 0; i < listUrl.size(); i++) {
            DefaultSliderView sliderView = new DefaultSliderView(context);
            //TextSliderView sliderView = new TextSliderView(context);
            // if you want show image only / without description text use DefaultSliderView instead

            // initialize SliderLayout
            sliderView
                    .image(listUrl.get(i))
                    .setRequestOption(requestOptions)
                    .setProgressBarVisible(true);


            mDemoSlider.addSlider(sliderView);
        }

        // set Slider Transition Animation
        // mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);

        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        //mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CategoryDisplayFragmentInteractionListener) {
            mListener = (CategoryDisplayFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CategoryDisplayFragmentInteractionListener");
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        /*progress = new ProgressDialog(getContext());
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCanceledOnTouchOutside(false);*/
        //----------------------------------Connect to Server
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //Create a JSONObject for sending to server
            JSONObject obj = new JSONObject();
            //get list of categories
            try {
                obj.put("cat_token", 2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "categories_req.php");
            //progress.show();
        } else {
            Toast.makeText(getActivity(), "No Network Connection detected!", Toast.LENGTH_LONG).show();
        }
        //--------------------------------

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void displayCategories() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        //GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        catTyperecyclerView.setLayoutManager(layoutManager);
        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
        //layoutManager.getOrientation());
        //recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
        //      DividerItemDecoration.VERTICAL));
        catTyperecyclerView.addItemDecoration(new DividerItemDecoration(catTyperecyclerView.getContext(), DividerItemDecoration.HORIZONTAL));
        //recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(5,3));
        catTyperecyclerView.setAdapter(new CategoryTypesRecyclerViewAdapter(catDataModel, context, mListener));

    }

    public void displayTechincialError() {
        //show the user status with an alert dailogue
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Snap!!!")
                .setMessage("Internet not Connected!");
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
                /*Intent loginActivityIntent = new Intent(getActivity(), UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginActivityIntent);
                getActivity().finish();*/
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface CategoryDisplayFragmentInteractionListener {
        void categoryFragmentInteraction(Category item);

        void setActionBarTitle(String title);

        void showBottom();
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
                return "Unable to retrieve contact server!";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //progress.dismiss();
            if (result.equals("timeout")) {
                //Toast.makeText(getActivity(), "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
                displayTechincialError();
            } else {
                Log.d("DD", "Result from webserver in Category fetching: " + result);
                try {
                    catDataModel.clear();
                    JSONObject resultArrayJson = new JSONObject(result);
                    JSONObject resultJson = resultArrayJson.getJSONObject("result");
                    int rowCnt = resultJson.getInt("row_cnt");
                    JSONArray catTypes = resultArrayJson.getJSONArray("cat_types");
                    for (int i = 0; i < rowCnt; i++) {
                        JSONArray categoryList = resultArrayJson.getJSONArray("result_details" + i);
                        ArrayList<Category> categories = new ArrayList<>();
                        for (int j = 0; j < categoryList.length(); j++) {
                            try {
                                JSONObject obj = categoryList.getJSONObject(j);
                /*Categories.category cat = new category(obj.getInt("id"), obj.getInt("delivery_slot"), obj.getString("name"), obj.getString("pic"));
                Categories.addItem(cat);*/
                                Category cat = new Category(obj.getInt("id"), obj.getInt("delivery_slot"), obj.getString("name"), obj.getString("pic"), obj.getInt("cat_type_id"));
                                categories.add(cat);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        CatDataModel c = new CatDataModel(catTypes.getString(i), categories);
                        catDataModel.add(c);
                    }

                    displayCategories();
                } catch (JSONException e) {
                    e.printStackTrace();
                    displayTechincialError();
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
}
