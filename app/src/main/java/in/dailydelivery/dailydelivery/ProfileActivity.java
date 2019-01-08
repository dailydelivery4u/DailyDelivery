package in.dailydelivery.dailydelivery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nameTV, phTV, addTV;
        nameTV = findViewById(R.id.nameTV);
        phTV = findViewById(R.id.phoneTV);
        addTV = findViewById(R.id.addressTV);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);

        nameTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_name), "ERROR"));
        phTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_phone), "ERROR"));
        addTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_add), "ERROR"));
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
