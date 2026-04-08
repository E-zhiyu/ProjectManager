package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.Shapeable;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.data.classes.Tag;

import java.util.List;

public class AnalysisRuleAdapter extends RecyclerView.Adapter<AnalysisRuleAdapter.AnalysisRuleViewHolder> {
    private final RuleClickedListener listener; //规则视图点击监听器
    private final List<AnalysisRule> ruleList;  //规则列表

    public static class AnalysisRuleViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView ruleNameText, tagNameText, typeText;
        SpringAnimationOnTouchListener onTouchListener;

        public AnalysisRuleViewHolder(@NonNull View itemView) {
            super(itemView);

            ruleNameText = itemView.findViewById(R.id.rule_name_text);
            tagNameText = itemView.findViewById(R.id.tag_name_text);
            typeText = itemView.findViewById(R.id.type_text);

            //设置触摸监听器
            Shapeable shapeable = (Shapeable) itemView;
            Vibrator vibrator = (Vibrator) itemView.getContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            onTouchListener = new SpringAnimationOnTouchListener(shapeable, vibrator);
            itemView.setOnTouchListener(onTouchListener);
        }
    }

    public interface RuleClickedListener {
        /**
         * 规则点击回调方法
         *
         * @param position 点击的规则的下标
         * @param rule     被点击的规则实例
         */
        void onRuleClicked(int position, AnalysisRule rule);
    }

    public AnalysisRuleAdapter(List<AnalysisRule> ruleList, RuleClickedListener listener) {
        this.ruleList = ruleList;
        this.listener = listener;
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
        //获取规则数据
        AnalysisRule rule = ruleList.get(position);
        String ruleName = rule.getRuleName();
        RunningAccountType type = rule.getType();
        Tag ruleTag = TagDataController.getTagByRuleNo(rule.getRuleNo(), holder.itemView.getContext());

        //初始化规则视图
        String typeStr = type.getTitle();
        holder.ruleNameText.setText(ruleName);
        holder.typeText.setText(typeStr);
        holder.tagNameText.setText(ruleTag.getName());

        //设置圆角大小
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.itemView, ruleList.size(), position);

        //设置点击监听
        holder.itemView.setOnClickListener(v ->
                listener.onRuleClicked(holder.getBindingAdapterPosition(), rule));
    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }

    /**
     * 添加新规则
     *
     * @param newRuleData 新规则数据
     */
    public void addRule(@NonNull Bundle newRuleData) {
        //解析规则数据
        String ruleName = newRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        RunningAccountType type = RunningAccountType.valueOf(newRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String packageName = newRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notificationTitle = newRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notificationContent = newRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());
        long ruleNo = newRuleData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());

        //刷新视图
        AnalysisRule newRule = new AnalysisRule(ruleName, ruleNo, type, packageName, notificationTitle, notificationContent);
        ruleList.add(newRule);
        notifyItemInserted(ruleList.size() - 1);

        //刷新圆角
        notifyItemChanged(ruleList.size() - 2);
    }

    /**
     * 修改规则
     *
     * @param modifiedRuleData 修改后的规则数据
     */
    public void modifyRule(@NonNull Bundle modifiedRuleData) {
        //解析规则数据
        int position = modifiedRuleData.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        String ruleName = modifiedRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        long ruleNo = modifiedRuleData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(modifiedRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String packageName = modifiedRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notificationTitle = modifiedRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notificationContent = modifiedRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        //更新UI
        AnalysisRule modifiedRule = new AnalysisRule(ruleName, ruleNo, type, packageName, notificationTitle, notificationContent);
        ruleList.set(position, modifiedRule);
        notifyItemChanged(position);
    }

    /**
     * 删除规则
     *
     * @param position 待删除标签的下标
     */
    public void deleteRule(int position) {
        //刷新 UI
        ruleList.remove(position);
        notifyItemRemoved(position);

        //刷新圆角
        if (position == ruleList.size()) {
            notifyItemChanged(ruleList.size() - 1);
        } else if (position == 0) {
            notifyItemChanged(0);
        }
    }

    /**
     * 刷新通知解析规则列表
     *
     * @param ruleList 刷新后的列表
     */
    public void refreshRuleList(List<AnalysisRule> ruleList) {
        int oldCount = this.ruleList.size();
        this.ruleList.clear();
        notifyItemRangeRemoved(0, oldCount);

        this.ruleList.addAll(ruleList);
        notifyItemRangeInserted(0, ruleList.size());
    }
}
