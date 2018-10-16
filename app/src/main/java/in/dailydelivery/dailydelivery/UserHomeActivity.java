package in.dailydelivery.dailydelivery;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;

import org.joda.time.DateTime;


public class UserHomeActivity extends AppCompatActivity implements DatePickerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // find the picker
        HorizontalPicker picker = (HorizontalPicker) findViewById(R.id.datePicker);

        // initialize it and attach a listener
        picker
                .setListener(this)
                .init();
        //picker.setBackgroundColor(Color.LTGRAY);
        picker.setDate(new DateTime());
    }

    @Override
    public void onDateSelected(DateTime dateSelected) {
        Log.d("DD","Date selected!");
    }

    public void createOrderBtnOnClick(View view){
        Intent createOrderActivityIntent = new Intent(this, CreateOrderActivity.class);
        startActivity(createOrderActivityIntent);
    }
}
