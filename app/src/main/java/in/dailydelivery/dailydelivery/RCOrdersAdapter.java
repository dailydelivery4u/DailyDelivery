package in.dailydelivery.dailydelivery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class RCOrdersAdapter extends RecyclerView.Adapter<RCOrdersAdapter.ViewHolder> {

    List<RcOrderDetails> items;
    DeleteRco deleteRco;

    public RCOrdersAdapter(List<RcOrderDetails> items, DeleteRco deleteRco) {
        this.items = items;
        this.deleteRco = deleteRco;
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
        Button delBtn = view.findViewById(R.id.delBtn);


        return new RCOrdersAdapter.ViewHolder(view, nameTV, priceTV, startDateTV, mon, tue, wed, thu, fri, sat, sun, delBtn);
    }

    @Override
    public void onBindViewHolder(@NonNull final RCOrdersAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        holder.nameTV.setText(holder.mItem.getName() + "(Order Id #" + holder.mItem.getOrderId() + ")");
        holder.priceTV.setText("Price: Rs." + holder.mItem.getPrice());
        holder.startDateTV.setText("Start Date: " + holder.mItem.getStartDate());
        holder.mon.setText(String.valueOf(holder.mItem.getMon()));
        holder.tue.setText(String.valueOf(holder.mItem.getTue()));
        holder.wed.setText(String.valueOf(holder.mItem.getWed()));
        holder.thu.setText(String.valueOf(holder.mItem.getThu()));
        holder.fri.setText(String.valueOf(holder.mItem.getFri()));
        holder.sat.setText(String.valueOf(holder.mItem.getSat()));
        holder.sun.setText(String.valueOf(holder.mItem.getSun()));

        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete RC Order
                deleteRco.deleteRco(holder.mItem.getOrderId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface DeleteRco {
        void deleteRco(int rcoId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV;
        public TextView priceTV;
        public TextView startDateTV;
        public TextView mon, tue, wed, thu, fri, sat, sun;

        public Button delBtn;
        public RcOrderDetails mItem;

        public ViewHolder(@NonNull View itemView, TextView nameTV, TextView priceTV, TextView startDateTV, TextView mon, TextView tue, TextView wed, TextView thu, TextView fri, TextView sat, TextView sun, Button delBtn) {
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
            this.delBtn = delBtn;
        }
    }

}

