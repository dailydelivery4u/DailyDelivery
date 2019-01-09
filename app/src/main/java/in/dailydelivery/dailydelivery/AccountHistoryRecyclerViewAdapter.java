package in.dailydelivery.dailydelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.WalletTransaction;

public class AccountHistoryRecyclerViewAdapter extends RecyclerView.Adapter<AccountHistoryRecyclerViewAdapter.ViewHolder> {

    List<WalletTransaction> items;

    public AccountHistoryRecyclerViewAdapter(List<WalletTransaction> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public AccountHistoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_account_history_list, parent, false);
        TextView dateTV = view.findViewById(R.id.dateTV);
        TextView desTV = view.findViewById(R.id.desTV);
        TextView amountTV = view.findViewById(R.id.amountTV);
        return new ViewHolder(view, dateTV, desTV, amountTV);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        holder.dateTV.setText(holder.mItem.getTransactionDate());
        holder.amountTV.setText(String.valueOf(holder.mItem.getTransactionAmount()));
        if (holder.mItem.getTransactionType() == 1) {
            //amount credited
            holder.amountTV.setTextColor(Color.parseColor("#28942e"));
        } else if (holder.mItem.getTransactionType() == 2) {
            holder.amountTV.setTextColor(Color.parseColor("#b7091d"));
        }
        holder.desTV.setText(holder.mItem.getTransactionDesc());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView dateTV;
        public final TextView desTV;
        public final TextView amountTV;
        public WalletTransaction mItem;

        public ViewHolder(View mView, TextView dateTV, TextView desTV, TextView amountTV) {
            super(mView);
            this.mView = mView;
            this.amountTV = amountTV;
            this.dateTV = dateTV;
            this.desTV = desTV;
        }
    }
}
