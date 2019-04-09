package in.dailydelivery.dailydelivery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
    EditText nameInput, refCodeInput, flatInput, floorInput;
    SharedPreferences sharedPref;
    ProgressDialog progress;
    AutoCompleteTextView societyACTV;
    private ArrayList<Society> societies, suggestions;
    Society societySelected;
    ArrayList<String> blocks;
    Spinner blockSpinner;
    String blockSelected;

    LinearLayout blockLL;
    CardView addCV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        nameInput = findViewById(R.id.nameInput);
        blockSpinner = findViewById(R.id.blockSpinner);
        refCodeInput = findViewById(R.id.referralCodeET);
        progress = new ProgressDialog(this);
        societyACTV = findViewById(R.id.societyACTV);
        addCV = findViewById(R.id.blockDetailsCV);
        blockLL = findViewById(R.id.blockNumLL);
        flatInput = findViewById(R.id.flatInput);
        floorInput = findViewById(R.id.floorInput);
        //getSupportActionBar().setTitle("Welcome");


        new FetchAreas().execute(getString(R.string.server_addr_release) + "req_soc.php");

        //generateTestData();

        societyACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(RegisterActivity.this,"Selected: " + suggestions.get(position).getSocietyName(),Toast.LENGTH_LONG).show();
                societySelected = suggestions.get(position);
                String query = "soc_id=" + societySelected.getSocietyId();
                new FetchBlocks(query).execute(getString(R.string.server_addr_release) + "req_blocks.php");
                progress.show();
                societyACTV.clearFocus();
            }
        });
        blockSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                blockSelected = blocks.get(position);
            }
        });
    }

/*    private void generateTestData() {
        societies = new ArrayList<>();
        Society s1 = new Society(1,"Sita Arcade", "Balapur");
        societies.add(s1);
        Society s2 = new Society(2,"Srinivasa Residency", "Santosh Nagar");
        societies.add(s2);

    }*/

    public void registerBtnOnClick(View view) {
        //int selectedAreaIndex = areaSpinner.getSelectedIndex();
        //Toast.makeText(this,areaList.get(selectedAreaIndex).getAreaId() + " " + areaList.get(selectedAreaIndex).getAreaName(),Toast.LENGTH_LONG).show();
        //int areaId = areaList.get(selectedAreaIndex).getAreaId();
        progress.setMessage("Registering Our Beloved Customer...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(30);
        progress.setCanceledOnTouchOutside(false);

        if (nameInput.getText().toString().equals("") || flatInput.getText().toString().equals("") || floorInput.getText().toString().equals("")) {
            Toast.makeText(this, "Please fill your name and address", Toast.LENGTH_LONG).show();
        } else {

//----------------------------------Connect to Server
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                progress.show();
                //Create a JSONObject for sending to server
                JSONObject obj = new JSONObject();
                //String address = addInput.getText().toString().toUpperCase() + ", " + areaList.get(selectedAreaIndex).getAreaName();

                try {
                    obj.put("name", nameInput.getText().toString());
                    obj.put("id", sharedPref.getInt(getString(R.string.sp_tag_user_id), 0));
                    obj.put("block_name", blockSelected);
                    obj.put("flat", flatInput.getText().toString());
                    obj.put("floor", floorInput.getText().toString());
                    //  obj.put("add", address);
                    obj.put("rc", refCodeInput.getText().toString());
                    //obj.put("areaId", areaId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new RegisterActivity.PostDataToServer(obj).execute(getString(R.string.server_addr_release) + "add_user.php");
            } else {
                Toast.makeText(this, "No Network Connection detected!", Toast.LENGTH_LONG).show();
            }
        }
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
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to contact server... check net connection" + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(RegisterActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result COde
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    //Registered succesfully
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("logged_in", true);
                    editor.putString(getString(R.string.sp_tag_user_name), resultJson.getString("name"));
                    editor.putString(getString(R.string.sp_tag_user_phone), resultJson.getString("phone"));
                    editor.putString(getString(R.string.sp_tag_user_add), resultJson.getString("add"));
                    editor.commit();
                    Intent userHomeActivityIntent = new Intent(RegisterActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (!resultJson.getString("title").isEmpty() && !resultJson.getString("msg").isEmpty()) {
                        userHomeActivityIntent.putExtra("title", resultJson.getString("title"));
                        userHomeActivityIntent.putExtra("message", resultJson.getString("msg"));
                    }
                    startActivity(userHomeActivityIntent);
                    finish();
                } else if (resultJson.getInt("responseCode") == 275) {
                    //Regsitration failed
                    Toast.makeText(RegisterActivity.this, "Something went wrong.. try again later", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
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

    private class FetchAreas extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to contact server... check net connection" + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(RegisterActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Result from webserver: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                JSONArray socJSON = resultArrayJson.getJSONArray("soc");
                societies = new ArrayList<>();
                blocks = new ArrayList<>();

                for (int i = 0; i < socJSON.length(); i++) {
                    JSONObject j = (JSONObject) socJSON.get(i);
                    societies.add(new Society(j.getInt("id"), j.getString("society_name"), j.getString("society_add")));
                }
                AddressAdapter addressAdapter = new AddressAdapter(RegisterActivity.this, R.layout.custom_address_dropdown_display, societies);
                societyACTV.setThreshold(1);
                societyACTV.setAdapter(addressAdapter);
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
                //OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                //out.write(Integer.parseInt(URLEncoder.encode(userDetails.toString(), "UTF-8")));
                //out.write(query.getBytes());
                //out.flush();
                //out.close();

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

    private class FetchBlocks extends AsyncTask<String, Void, String> {
        String query;

        public FetchBlocks(String query) {
            this.query = query;
        }

        @Override
        protected String doInBackground(String... urls) {
            progress.setProgress(50);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to contact server... check net connection" + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("timeout")) {
                Toast.makeText(RegisterActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            Log.d("DD", "Result from webserver: " + result);
            try {
                JSONArray resultArrayJson = new JSONArray(result);
                blocks.clear();
                for (int i = 0; i < resultArrayJson.length(); i++) {
                    JSONObject j = resultArrayJson.getJSONObject(i);
                    blocks.add(j.getString("block_name"));
                }

                blockSelected = blocks.get(0);
                if (blocks.size() == 1) {
                    blockLL.setVisibility(View.GONE);
                } else {
                    blockLL.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> blockSpinnerAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, blocks);
                    blockSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    blockSpinner.setAdapter(blockSpinnerAdapter);
                }
                addCV.setVisibility(View.VISIBLE);
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

                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(query.getBytes());
                out.flush();
                out.close();
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


    private class Society {
        private int societyId;
        private String societyName;
        private String societyAdd;

        public Society(int societyId, String societyName, String societyAdd) {
            this.societyId = societyId;
            this.societyName = societyName;
            this.societyAdd = societyAdd;
        }

        public int getSocietyId() {
            return societyId;
        }

        public void setSocietyId(int societyId) {
            this.societyId = societyId;
        }

        public String getSocietyName() {
            return societyName;
        }

        public void setSocietyName(String societyName) {
            this.societyName = societyName;
        }

        public String getSocietyAdd() {
            return societyAdd;
        }

        public void setSocietyAdd(String societyAdd) {
            this.societyAdd = societyAdd;
        }
    }

    public class AddressAdapter extends ArrayAdapter<Society> {
        private Context context;
        private int resourceId;
        private ArrayList<Society> items, tempItems;


        public AddressAdapter(Context context, int resource, ArrayList<Society> items) {
            super(context, resource, items);
            this.context = context;
            this.items = items;
            this.resourceId = resource;
            suggestions = new ArrayList<>();
            tempItems = new ArrayList<>(items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            try {
                if (convertView == null) {
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    view = inflater.inflate(resourceId, parent, false);
                }
                Society society = getItem(position);
                TextView name = view.findViewById(R.id.society_name);
                TextView add = view.findViewById(R.id.society_add);
                name.setText(society.getSocietyName());
                add.setText(society.getSocietyAdd());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return view;
        }

        @Override
        public Society getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return addFilter;
        }

        private Filter addFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                Society society = (Society) resultValue;
                return society.getSocietyName() + society.getSocietyAdd();
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                if (charSequence != null) {
                    suggestions.clear();
                    for (Society society : tempItems) {
                        if (society.getSocietyName().toLowerCase().contains(charSequence.toString().toLowerCase()) ||
                                society.getSocietyAdd().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            suggestions.add(society);
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ArrayList<Society> tempValues = (ArrayList<Society>) filterResults.values;
                if (filterResults.count > 0) {
                    clear();
                    for (Society fruitObj : tempValues) {
                        add(fruitObj);
                        notifyDataSetChanged();
                    }
                } else {
                    clear();
                    notifyDataSetChanged();
                }
            }
        };
    }
}
