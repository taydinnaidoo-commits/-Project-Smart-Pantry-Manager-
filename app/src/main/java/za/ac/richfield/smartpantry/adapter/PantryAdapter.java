package za.ac.richfield.smartpantry.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;
import za.ac.richfield.smartpantry.util.QuantityFormat;

/**
 * Custom RecyclerView adapter for the pantry list. Each row shows the
 * ingredient name, its quantity and its expiry status, with a delete button.
 * Items that have expired or are about to are coloured when expiry alerts are
 * switched on in Settings.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Lets the Activity react to taps without the adapter knowing about the database. */
    public interface Listener {
        void onItemClicked(PantryItem item);

        void onDeleteClicked(PantryItem item);
    }

    private final Listener listener;
    private final List<PantryItem> items = new ArrayList<>();
    private boolean highlightExpiring;
    private int warningDays;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the list with fresh data from the database. */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<PantryItem> newItems, boolean highlightExpiring, int warningDays) {
        items.clear();
        items.addAll(newItems);
        this.highlightExpiring = highlightExpiring;
        this.warningDays = warningDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(item.getName());
        holder.quantity.setText(QuantityFormat.amount(item.getQuantity(), item.getUnit()));

        Integer days = DateUtils.daysUntil(item.getExpiryDate());
        holder.expiry.setText(describeExpiry(context, days, item.getExpiryDate()));

        int colorAttr = com.google.android.material.R.attr.colorOnSurfaceVariant;
        boolean warn = false;
        if (highlightExpiring && days != null) {
            if (days < 0) {
                colorAttr = androidx.appcompat.R.attr.colorError;
                warn = true;
            } else if (days <= warningDays) {
                colorAttr = com.google.android.material.R.attr.colorTertiary;
                warn = true;
            }
        }
        holder.expiry.setTextColor(MaterialColors.getColor(holder.expiry, colorAttr));
        holder.expiry.setTypeface(null, warn ? Typeface.BOLD : Typeface.NORMAL);

        holder.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        holder.delete.setOnClickListener(v -> listener.onDeleteClicked(item));
        holder.delete.setContentDescription(
                context.getString(R.string.delete_item_description, item.getName()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String describeExpiry(Context context, Integer days, String date) {
        if (days == null) {
            return context.getString(R.string.expiry_none);
        }
        if (days < 0) {
            return context.getResources().getQuantityString(R.plurals.expiry_expired, -days, -days);
        }
        if (days == 0) {
            return context.getString(R.string.expiry_today);
        }
        if (days == 1) {
            return context.getString(R.string.expiry_tomorrow);
        }
        return context.getString(R.string.expiry_in_days, days, DateUtils.toDisplay(date));
    }

    /** Holds the views of one row so findViewById runs once per row, not per bind. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
            delete = itemView.findViewById(R.id.item_delete);
        }
    }
}
