package com.sly.coffer.ui.others.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import com.sly.coffer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 不带内容匹配过滤器的数组适配器
 *
 * @param <T> 元素种类
 */
public class NoFilteringArrayAdapter<T> extends ArrayAdapter<T> {
    private final List<T> originalData;

    public NoFilteringArrayAdapter(@NonNull Context context, @NonNull List<T> objects) {
        super(context, R.layout.exposed_dropdown_popup_item, objects);
        this.originalData = new ArrayList<>(objects);
    }

    public NoFilteringArrayAdapter(Context context, T[] objects) {
        super(context, R.layout.exposed_dropdown_popup_item, objects);
        this.originalData = Arrays.asList(objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new NonFilteringFilter();
    }

    private class NonFilteringFilter extends Filter {
        @NonNull
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            results.values = originalData;
            results.count = originalData.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            notifyDataSetChanged();
        }
    }
}
