package com.example.soilmate;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<FAQModel> faqList;
    private List<FAQModel> faqListFull;

    public FAQAdapter(List<FAQModel> faqList) {
        this.faqList = faqList;
        this.faqListFull = new ArrayList<>(faqList);
    }

    @Override
    public int getItemViewType(int position) {
        return faqList.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_faq_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_faq, parent, false);
            return new FAQViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FAQModel faq = faqList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText(faq.getCategory());
        } else {
            FAQViewHolder itemHolder = (FAQViewHolder) holder;
            itemHolder.questionTextView.setText(faq.getQuestion());

            // Highlight troubleshooting items
            if ("Troubleshooting".equals(faq.getCategory())) {
                itemHolder.questionTextView.setTextColor(Color.RED);
            } else {
                itemHolder.questionTextView.setTextColor(Color.BLACK);
            }

            // Make entire item clickable (including the arrow area)
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FAQDetails.class);
                intent.putExtra("question", faq.getQuestion());
                intent.putExtra("answer", faq.getAnswer());
                v.getContext().startActivity(intent);
            });

            // Optional: Keep the arrow visible but make it non-clickable
            itemHolder.viewButton.setClickable(false);
            itemHolder.viewButton.setFocusable(false);

            // Or if you want to completely hide the arrow:
            // itemHolder.viewButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    // ViewHolders
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.category_header);
        }
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        ImageButton viewButton;
        FAQViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.question);
            viewButton = itemView.findViewById(R.id.view);
        }
    }

    // Filter implementation (unchanged)
    @Override
    public Filter getFilter() {
        return faqFilter;
    }

    private Filter faqFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<FAQModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(faqListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (FAQModel faq : faqListFull) {
                    if (!faq.isHeader() &&
                            (faq.getQuestion().toLowerCase().contains(filterPattern) ||
                                    faq.getAnswer().toLowerCase().contains(filterPattern))) {
                        filteredList.add(faq);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            faqList.clear();
            faqList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}