package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.edit;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;

import java.util.List;

public class AnalysisRuleAdapter extends RecyclerView.Adapter<AnalysisRuleAdapter.RuleViewHolder> {
    private final List<AnalysisRule> ruleList;

    public static class RuleViewHolder extends RecyclerView.ViewHolder {
        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public AnalysisRuleAdapter(List<AnalysisRule> ruleList) {
        this.ruleList = ruleList;
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }
}
