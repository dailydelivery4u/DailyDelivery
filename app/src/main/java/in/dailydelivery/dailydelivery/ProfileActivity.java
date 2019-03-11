package in.dailydelivery.dailydelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nameTV, phTV, addTV, rcTV;
        nameTV = findViewById(R.id.nameTV);
        phTV = findViewById(R.id.phoneTV);
        addTV = findViewById(R.id.addressTV);
        rcTV = findViewById(R.id.rcTV);


        sharedPref = getSharedPreferences(getString(R.string.private_sharedpref_file), MODE_PRIVATE);

        nameTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_name), "ERROR"));
        phTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_phone), "ERROR"));
        addTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_add), "ERROR"));
        rcTV.setText(sharedPref.getString(getString(R.string.sp_tag_user_phone), "ERROR"));
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void shareBtnOnClick(View view) {
        String shareText;
        shareText = "Download this awesome APP - Daily Delivery - https://bit.ly/2FK8WpN.\nUse my Referral Code: " + sharedPref.getString(getString(R.string.sp_tag_user_phone), "ERROR") + " and get Rs.25 instantly in your wallet.";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }
}
