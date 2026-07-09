package com.manager.assistant.ui.pages.tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.R;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.auxiliary.enums.RadiusStyle;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.manager.assistant.auxiliary.interfaces.adapter.ViewHolderListener;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.TagListUiModel;
import com.manager.assistant.databinding.ViewHolderSeparatorTextChipBinding;
import com.manager.assistant.databinding.ViewHolderTagListBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.ui.others.decoration.sticky.StickyHeaderAdapter;

public class TagListAdapter extends ListAdapter<TagListUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final AdapterOnClickListener<TagEntity> clickListener;           //点击监听器
    private final AdapterOnLongClickListener<TagEntity> longClickListener;   //长按监听器
    private static final DiffUtil.ItemCallback<TagListUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagListUiModel oldItem, @NonNull TagListUiModel newItem) {
            if (oldItem instanceof TagListUiModel.Item && newItem instanceof TagListUiModel.Item) {
                TagListUiModel.Item oldI = (TagListUiModel.Item) oldItem;
                TagListUiModel.Item newI = (TagListUiModel.Item) newItem;
                return oldI.entity.getTagId() == newI.entity.getTagId();
            } else if (oldItem instanceof TagListUiModel.Separator && newItem instanceof TagListUiModel.Separator) {
                TagListUiModel.Separator oldS = (TagListUiModel.Separator) oldItem;
                TagListUiModel.Separator newS = (TagListUiModel.Separator) newItem;
                return oldS.text.equals(newS.text);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagListUiModel oldItem, @NonNull TagListUiModel newItem) {
            if (oldItem instanceof TagListUiModel.Item && newItem instanceof TagListUiModel.Item) {
                TagListUiModel.Item oldI = (TagListUiModel.Item) oldItem;
                TagListUiModel.Item newI = (TagListUiModel.Item) newItem;
                return oldI.entity.getName().equals(newI.entity.getName()) &&
                        oldI.entity.getScope() == newI.entity.getScope();
            } else
                return oldItem instanceof TagListUiModel.Separator && newItem instanceof TagListUiModel.Separator;
        }
    };

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagListBinding binding;

        public TagViewHolder(@NonNull ViewHolderTagListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), view);
                return true;
            });
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSeparatorTextChipBinding binding;

        public SeparatorViewHolder(@NonNull ViewHolderSeparatorTextChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /***
     * @param clickListener 点击监听器
     * @param longClickListener 长按监听器
     */
    public TagListAdapter(
            AdapterOnClickListener<TagEntity> clickListener,
            AdapterOnLongClickListener<TagEntity> longClickListener
    ) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        //注册数据变更监听器，用于自动更新圆角
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);           //更新前面的
                notifyItemChanged(positionStart + itemCount);   //更新后面的
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemChanged(positionStart - 1);   //更新前面的
                notifyItemChanged(positionStart);               //更新后面的
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemChanged(fromPosition - 1);    //更新前面的
                notifyItemChanged(fromPosition);                //更新后面的

                notifyItemChanged(toPosition - 1);      //更新前面的
                notifyItemChanged(toPosition + 1);      //更新后面的
            }
        });
    }

    @Override
    public boolean isHeader(int position) {
        return getItem(position) instanceof TagListUiModel.Separator;
    }

    @Override
    public String getHeaderData(int position, Context context) {
        TagListUiModel model = getItem(position);
        if (model instanceof TagListUiModel.Separator) {
            return ((TagListUiModel.Separator) model).text;
        } else if (model instanceof TagListUiModel.Item) {
            return ((TagListUiModel.Item) model).entity.getName();
        } else {
            return context.getString(R.string.not_applicable);
        }
    }

    @Override
    public int getItemViewType(int position) {
        TagListUiModel item = getItem(position);
        if (item instanceof TagListUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderTagListBinding binding = ViewHolderTagListBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new TagViewHolder(
                    binding,
                    new ViewHolderListener() {
                        @Override
                        public void onClick(int position, View anchor) {
                            TagListUiModel emotionTag = getItem(position);
                            if (emotionTag instanceof TagListUiModel.Item) {
                                clickListener.onClick(((TagListUiModel.Item) emotionTag).entity, anchor);
                            }
                        }

                        @Override
                        public void onLongClick(int position, View view) {
                            TagListUiModel emotionTag = getItem(position);
                            if (emotionTag instanceof TagListUiModel.Item) {
                                longClickListener.onLongClick(((TagListUiModel.Item) emotionTag).entity, view);
                            }
                        }
                    }
            );
        } else {
            ViewHolderSeparatorTextChipBinding binding = ViewHolderSeparatorTextChipBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new SeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TagListUiModel model = getItem(position);

        if (model instanceof TagListUiModel.Item && holder instanceof TagViewHolder) {
            TagEntity tag = ((TagListUiModel.Item) model).entity;
            TagViewHolder itemHolder = (TagViewHolder) holder;

            //标签名称
            itemHolder.binding.nameText.setText(tag.getName());

            //标签作用域
            int scope = tag.getScope();
            StringBuilder scopeBuilder = new StringBuilder();
            for (AccountType accountType : AccountType.values()) {
                int pow = (int) Math.pow(2, accountType.ordinal());
                if ((scope & pow) == 0) {
                    if (scopeBuilder.length() > 0) {
                        scopeBuilder.append("、");
                    }

                    scopeBuilder.append(accountType.getTitle());
                }
            }
            itemHolder.binding.scopeText.setText(scopeBuilder.length() > 0 ? scopeBuilder.toString() : "<无作用域>");

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
        } else if (model instanceof TagListUiModel.Separator && holder instanceof SeparatorViewHolder) {
            String text = ((TagListUiModel.Separator) model).text;
            ((SeparatorViewHolder) holder).binding.separatorText.setText(text);
        }
    }

    /**
     * 设置圆角
     *
     * @param view     需要设置圆角的视图
     * @param position 该视图所处的位置
     */
    private void setRadius(View view, int position) {
        if (position == 0) {    //第0个不参与圆角设置，因为它是日期分隔视图
            return;
        }

        //不需要考虑当前是分隔视图的情况，因为不是Shapable不会执行任何操作
        TagListUiModel front = getItem(position - 1);
        if (position == getItemCount() - 1) {   //处理最后一个卡片的圆角
            if (front instanceof TagListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            TagListUiModel behind = getItem(position + 1);

            if (front instanceof TagListUiModel.Separator && behind instanceof TagListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof TagListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof TagListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
