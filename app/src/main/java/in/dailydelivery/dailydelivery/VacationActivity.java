package in.dailydelivery.dailydelivery;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.Vacation;

public class VacationActivity extends AppCompatActivity implements VacationsRVAdapter.VacationAdapterInterface {
    DatePickerDialog.OnDateSetListener startDateListner, endDateLisnter;
    DateTime startDate, endDate;
    DateTimeFormatter dtf, dtf_display;
    SharedPreferences sharedPref;
    AppDatabase db;
    List<Vacation> setVacations;
    RecyclerView vacationsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation);

        //vacationsListView = findViewById(R.id.vacationsListView);
        vacationsRV = findViewById(R.id.vacationsRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        vacationsRV.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(vacationsRV.getContext(),
                layoutManager.getOrientation());
        vacationsRV.addItemDecoration(dividerItemDecoration);


        startDate = new DateTime();
        endDate = new DateTime();
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        dtf_display = DateTimeFormat.mediumDate();
        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);
        db = AppDatabase.getAppDatabase(this);
        setVacations = new ArrayList<>();
        new GetVacationDetails().execute();

        getSupportActionBar().setTitle("My Vacations");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startDateListner = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                startDate = new DateTime(newDate);

                DatePickerDialog endDateDialog = new DatePickerDialog(VacationActivity.this, endDateLisnter, startDate.getYear(), startDate.getMonthOfYear() - 1, startDate.getDayOfMonth());
                endDateDialog.getDatePicker().setMinDate(startDate.getMillis());
                endDateDialog.setMessage("Select Vacation End Date");
                endDateDialog.show();
            }
        };

        endDateLisnter = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                endDate = new DateTime(newDate);

                //Log.d("Vacation: ","Start Date: " + startDate.toString(dtf) + " End Date: " + endDate.toString(dtf));
                JSONObject vacationDetails = new JSONObject();
                try {
                    vacationDetails.put("user_id", sharedPref.getInt(getString(R.string.sp_tag_user_id), 273));
                    vacationDetails.put("start_date", startDate.toString(dtf));
                    vacationDetails.put("end_date", endDate.toString(dtf));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new UpdateVacationInServer(vacationDetails).execute(getString(R.string.server_addr_release) + "add_vacation.php");

            }
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent userHomeActivityIntent = new Intent(VacationActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent userHomeActivityIntent = new Intent(VacationActivity.this, UserHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userHomeActivityIntent);
        finish();
    }

    public void onAddVacationBtnClicked(View view) {
        DateTime d = new DateTime();
        int hourOfDay = d.hourOfDay().get();
        if (hourOfDay >= 22 && hourOfDay < 24) {
            d = d.plusDays(2);
        } else {
            d = new DateTime().plusDays(1);
        }
        DatePickerDialog startDateDialog = new DatePickerDialog(this, startDateListner, d.getYear(), d.getMonthOfYear() - 1, d.getDayOfMonth());
        startDateDialog.getDatePicker().setMinDate(d.getMillis());
        startDateDialog.setMessage("Select Vacation Start Date");
        startDateDialog.show();
    }

    @Override
    public void onVacDel(final int vacId) {
        //Delete selected vacation in server
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        // Set the dialog title
        builder.setTitle("Confirm Vacation Delete");
        builder.setMessage("Are you sure you want to delete the Vacation?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = "orderId=" + vacId;
                new DeleteOrder(query, vacId).execute(getString(R.string.server_addr_release) + "del_vac.php");
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

    private class UpdateVacationInServer extends AsyncTask<String, Void, String> {
        JSONObject vacationDetails;

        public UpdateVacationInServer(JSONObject vacationDetails) {
            this.vacationDetails = vacationDetails;
        }

        @Override
        protected String doInBackground(String... urls) {
            //progress.setProgress(50);
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
            if (result.equals("timeout")) {
                Toast.makeText(VacationActivity.this, "Your net connection is slow.. Please try again later.", Toast.LENGTH_LONG).show();
            }
            //Log.d("DD", "Result from webserver vacation activity: " + result);
            try {
                JSONObject resultArrayJson = new JSONObject(result);
                //Check for Result Code
                //If result is OK, update user Id in editor
                JSONObject resultJson = resultArrayJson.getJSONObject("result");
                if (resultJson.getInt("responseCode") == 273) {
                    //int orderStatus = resultJson.getInt("status");
                    Toast.makeText(VacationActivity.this, "Vacation Set.\nRepeating Orders during the set period will be paused", Toast.LENGTH_LONG).show();
                    vacationDetails.put("id", resultJson.getInt("id"));
                    new UpdateVacInDb(vacationDetails).execute();
                } else if (resultJson.getInt("responseCode") == 275) {
                    Toast.makeText(VacationActivity.this, "Some Error occured! Pls try again", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(VacationActivity.this, "Error in connection with Server.. Please try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                //progress.dismiss();
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
                String query = "json=" + vacationDetails.toString();
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

    private class UpdateVacInDb extends AsyncTask<Void, Void, Void> {
        private JSONObject vacationJson;

        public UpdateVacInDb(JSONObject vacationJson) {
            this.vacationJson = vacationJson;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            try {
                db.vacationDao().addVacation(new Vacation(vacationJson.getInt("id"), vacationJson.getString("start_date"), vacationJson.getString("end_date")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            VacationActivity.this.recreate();
        }
    }

    private class GetVacationDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... integers) {
            setVacations = db.vacationDao().getAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            VacationsRVAdapter adapter = new VacationsRVAdapter(setVacations, VacationActivity.this);
            vacationsRV.setAdapter(adapter);
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
            String msg;
            if (result.equals("ok")) {
                msg = "Vacation Deleted";
            } else {
                msg = "Error in deleting Vacation";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(VacationActivity.this);
            // Set the dialog title
            builder.setTitle(msg);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VacationActivity.this.recreate();
                    dialog.dismiss();
                }
            });

            AlertDialog mDialog = builder.create();
            mDialog.show();
        }


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
                        db.vacationDao().deleteVac(orderId);
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
