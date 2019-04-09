package in.dailydelivery.dailydelivery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class RCOrdersAdapter extends RecyclerView.Adapter<RCOrdersAdapter.ViewHolder> {

    List<RcOrderDetails> items;
    DeleteRco deleteRco;
    Context context;

    public RCOrdersAdapter(List<RcOrderDetails> items, DeleteRco deleteRco, Context context) {
        this.items = items;
        this.deleteRco = deleteRco;
        this.context = context;
    }

    public void updateItems(List<RcOrderDetails> items) {
        this.items.clear();
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RCOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rc_orders_list, parent, false);
        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView startDateTV = view.findViewById(R.id.startDateTV);
        TextView mon = view.findViewById(R.id.mon);
        TextView tue = view.findViewById(R.id.tue);
        TextView wed = view.findViewById(R.id.wed);
        TextView thu = view.findViewById(R.id.thu);
        TextView fri = view.findViewById(R.id.fri);
        TextView sat = view.findViewById(R.id.sat);
        TextView sun = view.findViewById(R.id.sun);
        TextView day1Qty = view.findViewById(R.id.day1QtyTV);
        TextView day2Qty = view.findViewById(R.id.day2QtyTV);
        TextView monQty = view.findViewById(R.id.monthlyQty);
        TextView monDate = view.findViewById(R.id.monthlyStartDateTV);
        Button delBtn = view.findViewById(R.id.delBtn);
        LinearLayout weeklyLL = view.findViewById(R.id.weeklyLL);
        LinearLayout alterndateLL = view.findViewById(R.id.alternateDayLL);
        LinearLayout monthlyLL = view.findViewById(R.id.monthlyLL);
        Button pauseBtn = view.findViewById(R.id.pauseBtn);
        TextView frequencyTv = view.findViewById(R.id.frequencyTV);


        return new RCOrdersAdapter.ViewHolder(view, nameTV, priceTV, startDateTV, mon, tue, wed, thu, fri, sat, sun, day1Qty, day2Qty, monQty, monDate, weeklyLL, alterndateLL, monthlyLL, delBtn, pauseBtn, frequencyTv);
    }

    @Override
    public void onBindViewHolder(@NonNull final RCOrdersAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        String productName = holder.mItem.getName() + " " + holder.mItem.getDes();
        holder.nameTV.setText(productName);
        holder.priceTV.setText("Price: Rs." + holder.mItem.getPrice());
        holder.startDateTV.setText("Start Date: " + holder.mItem.getStartDate());

        switch (holder.mItem.getFrequency()) {
            case 1:
                holder.frequencyTV.setText("Frequency: Daily");
                holder.weeklyLL.setVisibility(View.VISIBLE);
                holder.mon.setText(String.valueOf(holder.mItem.getMon()));
                holder.tue.setText(String.valueOf(holder.mItem.getTue()));
                holder.wed.setText(String.valueOf(holder.mItem.getWed()));
                holder.thu.setText(String.valueOf(holder.mItem.getThu()));
                holder.fri.setText(String.valueOf(holder.mItem.getFri()));
                holder.sat.setText(String.valueOf(holder.mItem.getSat()));
                holder.sun.setText(String.valueOf(holder.mItem.getSun()));
                break;
            case 2:
                holder.frequencyTV.setText("Frequency: Alternate Days");
                holder.alternateDayLL.setVisibility(View.VISIBLE);
                String qty1 = "First Day Qty: " + holder.mItem.getDay1Qty();
                String qty2 = "Next Day Qty: " + holder.mItem.getDay2Qty();
                holder.day1Qty.setText(qty1);
                holder.day2Qty.setText(qty2);
                break;
            case 3:
                holder.frequencyTV.setText("Frequency: Once a Month");
                holder.monthlyLL.setVisibility(View.VISIBLE);
                String monQty = "Quantity: " + holder.mItem.getDay1Qty();
                String monDate = "Delivery Date Every Month: " + holder.mItem.getDateOfMonth();
                holder.monQty.setText(monQty);
                holder.monStartDate.setText(monDate);
                break;
        }

        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete RC Order
                deleteRco.deleteRco(holder.mItem.getOrderId());
            }
        });

        holder.pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mItem.getStatus() != 10) {
                    deleteRco.pauseRco(holder.mItem.getOrderId());
                } else {
                    deleteRco.resumeRco(holder.mItem.getOrderId());
                }
            }
        });

        if (holder.mItem.getStatus() != 10) {
            holder.pauseBtn.setText("PAUSE");
            holder.pauseBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_pause), null, null, null);
        } else {
            holder.pauseBtn.setText("RESUME");
            holder.pauseBtn.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_play), null, null, null);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface DeleteRco {
        void deleteRco(int rcoId);

        void pauseRco(int rcoId);

        void resumeRco(int rcoId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV;
        public TextView priceTV;
        public TextView startDateTV, frequencyTV;
        public TextView mon, tue, wed, thu, fri, sat, sun, day1Qty, day2Qty, monQty, monStartDate;
        public LinearLayout weeklyLL, alternateDayLL, monthlyLL;

        public Button delBtn, pauseBtn;
        public RcOrderDetails mItem;

        public ViewHolder(@NonNull View itemView, TextView nameTV, TextView priceTV, TextView startDateTV, TextView mon, TextView tue, TextView wed, TextView thu, TextView fri, TextView sat, TextView sun, TextView day1Qty, TextView day2Qty, TextView monQty, TextView monStartDate, LinearLayout weeklyLL, LinearLayout alternateDayLL, LinearLayout monthlyLL, Button delBtn, Button pauseBtn, TextView frequencyTV) {
            super(itemView);
            this.nameTV = nameTV;
            this.priceTV = priceTV;
            this.startDateTV = startDateTV;
            this.mon = mon;
            this.tue = tue;
            this.wed = wed;
            this.thu = thu;
            this.fri = fri;
            this.sat = sat;
            this.sun = sun;
            this.day1Qty = day1Qty;
            this.day2Qty = day2Qty;
            this.monQty = monQty;
            this.monStartDate = monStartDate;
            this.weeklyLL = weeklyLL;
            this.alternateDayLL = alternateDayLL;
            this.monthlyLL = monthlyLL;
            this.delBtn = delBtn;
            this.pauseBtn = pauseBtn;
            this.frequencyTV = frequencyTV;
        }
    }
}

