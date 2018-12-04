package in.dailydelivery.dailydelivery;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;
import in.dailydelivery.dailydelivery.DB.RcOrderDetails;
import in.dailydelivery.dailydelivery.DB.Vacation;


public class UserHomeActivity extends AppCompatActivity implements DatePickerListener {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSelected(DateTime dateSelected) {
        this.dateSelected = dateSelected;
        new GetOrders(dateSelected.toString(dtf)).execute();
    }

    public void createOrderBtnOnClick(View view){
        Intent createOrderActivityIntent = new Intent(this, CreateOrderActivity.class);
        startActivity(createOrderActivityIntent);
    }

    public void createRecurringOrderBtnOnClick(View view) {
        Intent createOrderActivityIntent = new Intent(this, CreateRecurringOrderActivity.class);
        startActivity(createOrderActivityIntent);
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
                recylcerViewAdapter = new OrdersDisplayRecylcerViewAdapter(orders);
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
                startDate = dtf.parseDateTime(rcOrderDetails.getStartDate());
                if (dateSelected.isAfter(startDate.minusDays(1))) {
                    rcOrderDetails.setStatus(1);
                    Log.d("dd", "Vacations: " + vacations.size());
                    for (Vacation v : vacations) {
                        Log.d("dd", "Vacation start Date: " + v.getStartDate());

                        vStartDate = dtf.parseDateTime(v.getStartDate());
                        vEndDate = dtf.parseDateTime(v.getEndDate());
                        if (dateSelected.isAfter(vStartDate.minusDays(1)) && dateSelected.isBefore(vEndDate.plusDays(1))) {
                            rcOrderDetails.setStatus(2);
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
                    rcOrdersDisplayRecyclerviewAdapter.updateData(rcOrdersForTheDay, dateSelected.getDayOfWeek());
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
}
