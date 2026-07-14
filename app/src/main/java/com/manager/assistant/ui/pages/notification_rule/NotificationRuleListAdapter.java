package com.manager.assistant.ui.pages.notification_rule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.manager.assistant.auxiliary.interfaces.adapter.ViewHolderListener;
import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.databinding.ViewHolderNotificationRuleListBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

public class NotificationRuleListAdapter extends ListAdapter<NotificationRuleEntity, NotificationRuleListAdapter.NotificationRuleViewHolder> {
    private final static DiffUtil.ItemCallback<NotificationRuleEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull NotificationRuleEntity oldItem, @NonNull NotificationRuleEntity newItem) {
            return oldItem.getRuleId() == newItem.getRuleId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NotificationRuleEntity oldItem, @NonNull NotificationRuleEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getType() == newItem.getType() &&
                    oldItem.getPackageName().equals(newItem.getPackageName());
        }
    };
    private final AdapterOnClickListener<NotificationRuleEntity> clickListener;
    private final AdapterOnLongClickListener<NotificationRuleEntity> longClickListener;

    public NotificationRuleListAdapter(AdapterOnClickListener<NotificationRuleEntity> clickListener, AdapterOnLongClickListener<NotificationRuleEntity> longClickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public static class NotificationRuleViewHolder extends RecyclerView.ViewHolder {
        ViewHolderNotificationRuleListBinding binding;

        public NotificationRuleViewHolder(@NonNull ViewHolderNotificationRuleListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //点击监听器
            binding.getRoot().setOnClickListener(v ->
                    listener.onClick(getBindingAdapterPosition(), binding.getRoot())
            );

            //长按监听器
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    @NonNull
    @Override
    public NotificationRuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderNotificationRuleListBinding binding = ViewHolderNotificationRuleListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationRuleViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                        clickListener.onClick(getItem(pos), anchor);
                    }

                    @Override
                    public void onLongClick(int pos, View anchor) {
                        longClickListener.onLongClick(getItem(pos), anchor);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationRuleViewHolder holder, int position) {
        NotificationRuleEntity rule = getItem(position);

        //名称
        holder.binding.nameText.setText(rule.getName());

        //种类
        holder.binding.typeText.setText(AccountType.values()[rule.getType()].getTitle());

        //包名
        holder.binding.packageNameText.setText(rule.getPackageName());
    }
}
