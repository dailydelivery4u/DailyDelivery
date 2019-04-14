package in.dailydelivery.dailydelivery;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import in.dailydelivery.dailydelivery.DB.RcOrderDetails;

public class RcOrdersDisplayRecyclerviewAdapter extends RecyclerView.Adapter<RcOrdersDisplayRecyclerviewAdapter.ViewHolder> {

    List<RcOrderDetails> items;
    private int dayOfWeek;
    private DateTime dateSelected;
    private DateTimeFormatter dtf;

    public RcOrdersDisplayRecyclerviewAdapter(List<RcOrderDetails> items, int dayOfWeek, DateTime dateSelected) {
        this.items = items;
        this.dayOfWeek = dayOfWeek;
        this.dateSelected = dateSelected;
        dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
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
        return new RcOrdersDisplayRecyclerviewAdapter.ViewHolder(view, nameTV, desTV, priceTV, statusTV, delBtn, rcTv);
    }

    @Override
    public void onBindViewHolder(@NonNull final RcOrdersDisplayRecyclerviewAdapter.ViewHolder holder, int position) {
        holder.mItem = items.get(position);
        holder.delBtn.setVisibility(View.GONE);
        holder.rcTV.setVisibility(View.VISIBLE);
        //get quantity for the day of week
        int qty = 1;

        switch (holder.mItem.getFrequency()) {
            case 1:
                //Daily Order
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
                break;

            case 2:
                //Alternate Days order
                DateTime startDate;
                startDate = dtf.parseDateTime(holder.mItem.getStartDate());
                long diffInMillis = dateSelected.withTimeAtStartOfDay().getMillis() - startDate.withTimeAtStartOfDay().getMillis();
                long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
                if (diff % 2 == 0) {
                    qty = holder.mItem.getDay1Qty();
                } else {
                    qty = holder.mItem.getDay2Qty();
                }
                break;
            case 3:
                //Monthly Order
                if (dateSelected.getDayOfMonth() == holder.mItem.getDateOfMonth()) {
                    qty = holder.mItem.getDay1Qty();
                }
                break;
        }
        int ddPrice = holder.mItem.getPrice() * qty;
        holder.nameTV.setText(holder.mItem.getName() + " (" + qty + " Nos.)");
        holder.desTV.setText(holder.mItem.getDes() + " - " + holder.mItem.getQtyDes());
        holder.priceTV.setText("Rs. " + ddPrice);

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

    public void updateData(List<RcOrderDetails> list, int dayOfWeek, DateTime dateSelected) {
        items.clear();
        items = list;
        this.dayOfWeek = dayOfWeek;
        this.dateSelected = dateSelected;
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
        public final TextView statusTV;
        public final ImageView delBtn;
        public final TextView rcTV;
        public RcOrderDetails mItem;

        public ViewHolder(View mView, TextView nameTV, TextView desTV, TextView priceTV, TextView statusTV, ImageView delBtn, TextView rcTV) {
            super(mView);
            this.mView = mView;
            this.nameTV = nameTV;
            this.desTV = desTV;
            this.priceTV = priceTV;
            this.statusTV = statusTV;
            this.delBtn = delBtn;
            this.rcTV = rcTV;
        }
    }

}
