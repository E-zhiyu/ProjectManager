package com.manager.assistant.ui.others.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

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

    public NoFilteringArrayAdapter(@NonNull Context context,
                                    int resource,
                                    @NonNull List<T> objects) {
        super(context, resource, objects);
        // 确保使用可变列表
        this.originalData = new ArrayList<>(objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new NonFilteringFilter();
    }

    private class NonFilteringFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            results.values = originalData;
            results.count = originalData.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // 不调用 clear() 和 addAll()，直接操作数据
            if (getCount() > 0) {
                // 逐步删除，避免异常
                while (getCount() > 0) {
                    remove(getItem(0));
                }
            }

            // 添加新数据
            if (results.values instanceof List) {
                List<T> filteredList = (List<T>) results.values;
                for (T item : filteredList) {
                    add(item);
                }
            }
            notifyDataSetChanged();
        }
    }
}
