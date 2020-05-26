package com.app.ecommerce.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.ecommerce.Config;
import com.app.ecommerce.R;
import com.app.ecommerce.models.Category;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapterCategory extends RecyclerView.Adapter<RecyclerAdapterCategory.MyViewHolder> implements Filterable {

    private Context context;
    private List<Category> categoryList;
    private List<Category> categoryListFiltered;
    private ContactsAdapterListener listener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView category_name, product_count;
        ImageView category_image;

        MyViewHolder(View view) {
            super(view);
            category_name = view.findViewById(R.id.category_name);
            product_count = view.findViewById(R.id.product_count);
            category_image = view.findViewById(R.id.category_image);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onContactSelected(categoryListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }

    public RecyclerAdapterCategory(Context context, List<Category> categoryList, ContactsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.categoryList = categoryList;
        this.categoryListFiltered = categoryList;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Category category = categoryListFiltered.get(position);
        holder.category_name.setText(category.getCategory_name());
        holder.product_count.setText(category.getProduct_count() + " " + context.getResources().getString(R.string.txt_items));

        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(6)
                .oval(false)
                .build();

        Picasso.with(context)
                .load(Config.ADMIN_PANEL_URL + "/upload/category/" + category.getCategory_image())
                .placeholder(R.drawable.ic_loading)
                .resize(250, 250)
                .centerCrop()
                .transform(transformation)
                .into(holder.category_image);

    }

    @Override
    public int getItemCount() {
        return categoryListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    categoryListFiltered = categoryList;
                } else {
                    List<Category> filteredList = new ArrayList<>();
                    for (Category row : categoryList) {
                        if (row.getCategory_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    categoryListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = categoryListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                categoryListFiltered = (ArrayList<Category>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Category category);
    }
}
