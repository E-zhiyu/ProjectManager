package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.Shapeable;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.broadcast.BroadcastConstants;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.data.data_class.AnalysisRule;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.data.data_class.Tag;

import java.util.List;

public class AnalysisRuleAdapter extends RecyclerView.Adapter<AnalysisRuleAdapter.AnalysisRuleViewHolder> {
    private final Context context;
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

    public AnalysisRuleAdapter(List<AnalysisRule> ruleList, RuleClickedListener listener, Context context) {
        this.ruleList = ruleList;
        this.listener = listener;
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
        Tag rule_tag = Tag.getTagByRuleNo(rule.getRuleNo(), context);

        String typeStr = type.getTitle();
        holder.ruleNameText.setText(rule_name);
        holder.typeText.setText(typeStr);
        holder.tagNameText.setText(rule_tag.getName());

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
        String ruleName = newRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        RunningAccountType type = RunningAccountType.valueOf(newRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String packageName = newRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notificationTitle = newRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notificationContent = newRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        //刷新视图
        AnalysisRule newRule = new AnalysisRule(ruleName, rule_no, type, packageName, notificationTitle, notificationContent);
        ruleList.add(newRule);
        notifyItemInserted(ruleList.size() - 1);
        Toast.makeText(context, "解析规则添加成功", Toast.LENGTH_SHORT).show();

        sendRuleUpdatedBroadcast();
    }

    /**
     * 修改规则
     *
     * @param modifiedRuleData 修改后的规则数据
     */
    public void modifyRule(Bundle modifiedRuleData) {
        try {
            AnalysisRule.modifyRule(modifiedRuleData, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "规则数据保存失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //解析规则数据
        int position = modifiedRuleData.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        String rule_name = modifiedRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        long rule_no = modifiedRuleData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(modifiedRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String package_name = modifiedRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notification_title = modifiedRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notification_content = modifiedRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        //更新UI
        AnalysisRule modifiedRule = new AnalysisRule(rule_name, rule_no, type, package_name, notification_title, notification_content);
        ruleList.set(position, modifiedRule);
        notifyItemChanged(position);
        Toast.makeText(context, "解析规则修改成功", Toast.LENGTH_SHORT).show();

        sendRuleUpdatedBroadcast();
    }

    /**
     * 删除规则
     *
     * @param position 待删除标签的下标
     */
    public void deleteRule(int position) {
        AnalysisRule rule = ruleList.get(position);
        long rule_no = rule.getRuleNo();

        try {
            AnalysisRule.deleteRule(rule_no, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "规则删除失败", Toast.LENGTH_SHORT).show();
            return;
        }

        ruleList.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "规则删除成功", Toast.LENGTH_SHORT).show();

        sendRuleUpdatedBroadcast();
    }

    /**
     * 刷新通知解析规则列表
     *
     * @param ruleList 刷新后的列表
     */
    public void refreshRuleList(List<AnalysisRule> ruleList) {
        int old_count = this.ruleList.size();
        this.ruleList.clear();
        notifyItemRangeRemoved(0, old_count);

        this.ruleList.addAll(ruleList);
        notifyItemRangeInserted(0, ruleList.size());
    }

    /**
     * 发送规则变更的广播
     */
    private void sendRuleUpdatedBroadcast() {
        Intent ruleUpdated = new Intent(BroadcastConstants.ACTION_RULES_UPDATED.toString());
        context.sendBroadcast(ruleUpdated);
    }
}
