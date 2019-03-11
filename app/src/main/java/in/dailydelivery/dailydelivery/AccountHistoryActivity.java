package in.dailydelivery.dailydelivery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.AppDatabase;
import in.dailydelivery.dailydelivery.DB.WalletTransaction;

public class AccountHistoryActivity extends AppCompatActivity {
    AppDatabase db;
    RecyclerView accountHistoryRV;
    TextView noHistoryTV;
    List<WalletTransaction> walletTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);
        accountHistoryRV = findViewById(R.id.accountHistoryRV);
        noHistoryTV = findViewById(R.id.noHistoryTV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        accountHistoryRV.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(accountHistoryRV.getContext(),
                layoutManager.getOrientation());
        accountHistoryRV.addItemDecoration(dividerItemDecoration);
        db = AppDatabase.getAppDatabase(this);
        new GetAccountHistory().execute();
        getSupportActionBar().setTitle("Account History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class GetAccountHistory extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            walletTransactions = db.walletTransactionDao().getAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (walletTransactions.size() != 0) {
                AccountHistoryRecyclerViewAdapter adapter = new AccountHistoryRecyclerViewAdapter(walletTransactions);
                accountHistoryRV.setAdapter(adapter);
            } else {
                noHistoryTV.setVisibility(View.VISIBLE);
            }
        }
    }
}
