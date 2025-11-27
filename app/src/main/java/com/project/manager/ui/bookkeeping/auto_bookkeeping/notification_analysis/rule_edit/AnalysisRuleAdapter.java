package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.rule_edit;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.List;

public class AnalysisRuleAdapter extends RecyclerView.Adapter<AnalysisRuleAdapter.AnalysisRuleViewHolder> {
    private final Context context;
    private final List<AnalysisRule> ruleList;

    public static class AnalysisRuleViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView rule_name_text, tag_name_text, type_text;

        public AnalysisRuleViewHolder(@NonNull View itemView) {
            super(itemView);

            rule_name_text = itemView.findViewById(R.id.rule_name_text);
            tag_name_text = itemView.findViewById(R.id.tag_name_text);
            type_text = itemView.findViewById(R.id.type_text);
        }
    }

    public AnalysisRuleAdapter(List<AnalysisRule> ruleList, Context context) {
        this.ruleList = ruleList;
        this.context = context;
    }

    @NonNull
    @Override
    public AnalysisRuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_analysis_rule, parent, false);
        return new AnalysisRuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnalysisRuleViewHolder holder, int position) {
        AnalysisRule rule = ruleList.get(position);
        String rule_name = rule.getRuleName();
        RunningAccountType type = rule.getType();
        long tag_no = rule.getTagNo();

        String type_str = "未知";
        switch (type) {
            case EXPENSE:
                type_str = "支出";
                break;
            case INCOME:
                type_str = "收入";
                break;
        }
        String tag_name = Tag.tagNoTransToName(tag_no, context);

        holder.rule_name_text.setText(rule_name);
        holder.type_text.setText(type_str);
        holder.tag_name_text.setText(tag_name);
    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }

    public void addRule(Bundle newRuleData) {
        long rule_no;
        try {
            rule_no = AnalysisRule.saveNewRule(newRuleData, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "解析规则保存失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //解析规则数据
        String rule_name = newRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        RunningAccountType type = RunningAccountType.valueOf(newRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        long tag_no = newRuleData.getLong(KeyValueStrings.TAG_NO.getValue());
        String package_name = newRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notification_title = newRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notification_content = newRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        //刷新视图
        AnalysisRule newRule = new AnalysisRule(rule_name, rule_no, type, tag_no, package_name, notification_title, notification_content);
        ruleList.add(0, newRule);
        notifyItemInserted(0);
    }
}
