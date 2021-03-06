package in.dailydelivery.dailydelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class RcOrdersDisplayRecyclerviewAdapter extends RecyclerView.Adapter<RcOrdersDisplayRecyclerviewAdapter.ViewHolder> {

    List<RcOrderDetails> items;
    private int dayOfWeek;

    public RcOrdersDisplayRecyclerviewAdapter(List<RcOrderDetails> items, int dayOfWeek) {
        this.items = items;
        this.dayOfWeek = dayOfWeek;
    }

    @NonNull
    @Override
    public RcOrdersDisplayRecyclerviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order_list, parent, false);
        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView desTV = view.findViewById(R.id.desTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView slotTV = view.findViewById(R.id.deliverySlotTV);
        TextView statusTV = view.findViewById(R.id.statusTV);
        ImageView delBtn = view.findViewById(R.id.delBtn);
        TextView rcTv = view.findViewById(R.id.rcTV);
        return new RcOrdersDisplayRecyclerviewAdapter.ViewHolder(view, nameTV, desTV, priceTV, slotTV, statusTV, delBtn, rcTv);
    }

    @Override
    public void onBindViewHolder(@NonNull final RcOrdersDisplayRecyclerviewAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        holder.delBtn.setVisibility(View.GONE);
        holder.rcTV.setVisibility(View.VISIBLE);
        //get quantity for the day of week
        int qty = 1;
        switch (dayOfWeek) {
            case 1:
                qty = holder.mItem.getMon();
                break;
            case 2:
                qty = holder.mItem.getTue();
                break;
            case 3:
                qty = holder.mItem.getWed();
                break;
            case 4:
                qty = holder.mItem.getThu();
                break;
            case 5:
                qty = holder.mItem.getFri();
                break;
            case 6:
                qty = holder.mItem.getSat();
                break;
            case 7:
                qty = holder.mItem.getSun();
                break;
        }
        int ddPrice = holder.mItem.getPrice() * qty;
        holder.nameTV.setText(holder.mItem.getName() + "(" + qty + " Nos.)");
        holder.desTV.setText(holder.mItem.getDes());
        holder.priceTV.setText("Rs. " + ddPrice);
        if (holder.mItem.getDeliverySlot() == 1) {
            holder.slotTV.setText(holder.mView.getContext().getString(R.string.delivery_slot_1));
        } else if (holder.mItem.getDeliverySlot() == 2) {
            holder.slotTV.setText(holder.mView.getContext().getString(R.string.delivery_slot_2));
        }
        String status;
        switch (holder.mItem.getStatus()) {
            case 1:
                status = "Scheduled";
                holder.statusTV.setTextColor(Color.parseColor("#732525"));
                break;
            case 8:
                status = "Paused (Vacation)";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            case 3:
                status = "On Hold";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            case 4:
                status = "Confirmed";
                holder.statusTV.setTextColor(Color.parseColor("#33862e"));
                break;
            case 5:
                status = "Delivered";
                holder.statusTV.setTextColor(Color.parseColor("#33862e"));
                break;
            case 6:
                status = "Undelivered";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            case 7:
                status = "Cancelled";
                holder.statusTV.setTextColor(Color.parseColor("#e41b2b"));
                break;
            default:
                status = "";
                break;
        }
        status = "Status: " + status;
        holder.statusTV.setText(status);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<RcOrderDetails> list, int dayOfWeek) {
        items.clear();
        items = list;
        this.dayOfWeek = dayOfWeek;
        notifyDataSetChanged();
    }

    public void clearData() {
        items.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameTV;
        public final TextView desTV;
        public final TextView priceTV;
        public final TextView slotTV;
        public final TextView statusTV;
        public final ImageView delBtn;
        public final TextView rcTV;
        public RcOrderDetails mItem;

        public ViewHolder(View mView, TextView nameTV, TextView desTV, TextView priceTV, TextView slotTV, TextView statusTV, ImageView delBtn, TextView rcTV) {
            super(mView);
            this.mView = mView;
            this.nameTV = nameTV;
            this.desTV = desTV;
            this.priceTV = priceTV;
            this.slotTV = slotTV;
            this.statusTV = statusTV;
            this.delBtn = delBtn;
            this.rcTV = rcTV;
        }
    }

}
