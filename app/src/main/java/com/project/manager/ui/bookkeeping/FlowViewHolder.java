package com.project.manager.ui.bookkeeping;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.R;

public class FlowViewHolder extends RecyclerView.ViewHolder {
    TextView amount_text,remark_text,name_datetime_text;

    public FlowViewHolder(@NonNull View itemView) {
        super(itemView);
        amount_text = itemView.findViewById(R.id.amount_textview);
        remark_text = itemView.findViewById(R.id.remark_textview);
        name_datetime_text = itemView.findViewById(R.id.name_datetime_textview);
    }
}
