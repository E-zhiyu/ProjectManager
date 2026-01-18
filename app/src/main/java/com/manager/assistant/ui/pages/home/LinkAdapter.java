package com.manager.assistant.ui.pages.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.helpers.WebsiteLinkFetchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.noties.markwon.Markwon;

public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.LinkViewHolder> {
    private final List<WebsiteLinkFetchHelper.WebLink> linkList; //链接列表
    private final Context context;        //上下文

    public static class LinkViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView textView;

        public LinkViewHolder(@NonNull MaterialTextView itemView) {
            super(itemView);
            textView = itemView;
        }
    }

    public LinkAdapter(Context context) {
        this.linkList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialTextView textView = new MaterialTextView(context);

        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(0, 5, 16, 5);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);   //设置文字大小为17sp

        return new LinkViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {
        WebsiteLinkFetchHelper.WebLink webLink = linkList.get(position);
        String title = webLink.getTitle();
        String url = webLink.getPageurl();

        //解析为Markdown格式
        Markwon markwon = Markwon.create(context);
        String markDownStr = String.format(Locale.getDefault(), "[%s](%s)", title, url);
        markwon.setMarkdown(holder.textView, markDownStr);
    }

    @Override
    public int getItemCount() {
        return linkList.size();
    }

    /**
     * 刷新链接
     *
     * @param linkList 刷新后的链接列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshLink(List<WebsiteLinkFetchHelper.WebLink> linkList) {
        this.linkList.clear();
        this.linkList.addAll(linkList);
        notifyDataSetChanged();
    }
}
