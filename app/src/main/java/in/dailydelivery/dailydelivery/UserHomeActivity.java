package in.dailydelivery.dailydelivery;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.OneTimeOrderDetails;


public class UserHomeActivity extends AppCompatActivity implements DatePickerListener {
    AppDatabase db;
    RecyclerView ordersforthedayRV;
    OrdersDisplayRecylcerViewAdapter recylcerViewAdapter;
    TextView noOrdersTV;
    boolean isLoadingFirstTime;
    TextView cartQtyTV;
    ImageView cartImage;
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
        ordersforthedayRV.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        //MenuItem item = menu.findItem(R.id.cartItem);
        //MenuItemCompat.setActionView(item, R.layout.layout_badge);
        //RelativeLayout notifCount = (RelativeLayout)   MenuItemCompat.getActionView(item);
        RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.cartItem).getActionView();
        cartQtyTV = badgeLayout.findViewById(R.id.actionbar_notifcation_textview);
        cartImage = badgeLayout.findViewById(R.id.cartImage);
        cartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(UserHomeActivity.this,"cart clicked",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserHomeActivity.this, CreateOrderActivity.class);
                intent.putExtra("fragment", 3);
                startActivity(intent);
            }
        });
        //cartQtyTV.setText("0");
        new UpdateCartQty().execute();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wallet:
                Toast.makeText(this, "Wallet Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSelected(DateTime dateSelected) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        new GetOrders(dateSelected.toString(dtf)).execute();
    }

    public void createOrderBtnOnClick(View view){
        Intent createOrderActivityIntent = new Intent(this, CreateOrderActivity.class);
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
                ordersforthedayRV.setVisibility(View.VISIBLE);
                noOrdersTV.setVisibility(View.GONE);
                recylcerViewAdapter = new OrdersDisplayRecylcerViewAdapter(orders);
                ordersforthedayRV.setAdapter(recylcerViewAdapter);
                isLoadingFirstTime = false;
            }
        } else {
            if (orders.size() > 0) {
                ordersforthedayRV.setVisibility(View.VISIBLE);
                noOrdersTV.setVisibility(View.GONE);
                recylcerViewAdapter.updateData(orders);
            } else {
                ordersforthedayRV.setVisibility(View.GONE);
                noOrdersTV.setVisibility(View.VISIBLE);
            }
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
