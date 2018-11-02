package in.dailydelivery.dailydelivery;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    boolean isLoaddingFirstTime;
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
        DateTime today = new DateTime();
        picker.setDate(today);
        isLoaddingFirstTime = true;
        recylcerViewAdapter = new OrdersDisplayRecylcerViewAdapter();
        ordersforthedayRV = findViewById(R.id.ordersforthedayRV);
        ordersforthedayRV.setLayoutManager(new LinearLayoutManager(this));
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        new GetOrders(today.toString(dtf)).execute();
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
        if (isLoaddingFirstTime) {
            if (orders.size() > 0) {
                recylcerViewAdapter = new OrdersDisplayRecylcerViewAdapter(orders);
                ordersforthedayRV.setAdapter(recylcerViewAdapter);
            } else {
                //TODO: DIsplay no orders for the day
                Toast.makeText(UserHomeActivity.this, "No Orders Today", Toast.LENGTH_SHORT).show();
            }
            isLoaddingFirstTime = false;
        } else {
            if (orders.size() > 0) {
                recylcerViewAdapter.updateData(orders);
            } else {
                Toast.makeText(UserHomeActivity.this, "No Orders Today", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
