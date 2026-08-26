package com.sly.coffer.ui.pages.accessibility.pick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.R;
import com.sly.coffer.auxiliary.enums.RadiusStyle;
import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnLongClickListener;
import com.sly.coffer.auxiliary.interfaces.adapter.ViewHolderListener;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.db.entities.composite.ui.PickedViewListUiModel;
import com.sly.coffer.databinding.ViewHolderPickedViewListBinding;
import com.sly.coffer.databinding.ViewHolderSeparatorTextChipBinding;
import com.sly.coffer.helpers.AppListHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;
import com.sly.coffer.ui.others.decoration.sticky.StickyHeaderAdapter;

public class PickedViewListAdapter extends ListAdapter<PickedViewListUiModel, RecyclerView.ViewHolder>
        implements StickyHeaderAdapter<String> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private static final DiffUtil.ItemCallback<PickedViewListUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PickedViewListUiModel oldItem, @NonNull PickedViewListUiModel newItem) {
            if (oldItem instanceof PickedViewListUiModel.Item && newItem instanceof PickedViewListUiModel.Item) {
                PickedViewEntity oldEntity = ((PickedViewListUiModel.Item) oldItem).entity;
                PickedViewEntity newEntity = ((PickedViewListUiModel.Item) newItem).entity;
                return oldEntity.getId() == newEntity.getId();
            } else if (oldItem instanceof PickedViewListUiModel.Separator && newItem instanceof PickedViewListUiModel.Separator) {
                String oldSeparator = ((PickedViewListUiModel.Separator) oldItem).text;
                String newSeparator = ((PickedViewListUiModel.Separator) newItem).text;
                return oldSeparator.equals(newSeparator);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull PickedViewListUiModel oldItem, @NonNull PickedViewListUiModel newItem) {
            if (oldItem instanceof PickedViewListUiModel.Item && newItem instanceof PickedViewListUiModel.Item) {
                PickedViewEntity oldEntity = ((PickedViewListUiModel.Item) oldItem).entity;
                PickedViewEntity newEntity = ((PickedViewListUiModel.Item) newItem).entity;
                return oldEntity.getRemark().equals(newEntity.getRemark()) &&
                        oldEntity.getPackageName().equals(newEntity.getPackageName()) &&
                        oldEntity.getViewId().equals(newEntity.getViewId()) &&
                        oldEntity.getActivityName().equals(newEntity.getActivityName()) &&
                        oldEntity.getContentText().equals(newEntity.getContentText());
            } else
                return oldItem instanceof PickedViewListUiModel.Separator && newItem instanceof PickedViewListUiModel.Separator;
        }
    };

    private final AdapterOnClickListener<PickedViewEntity> clickListener;            //单击监听
    private final AdapterOnLongClickListener<PickedViewEntity> longClickListener;    //长按监听

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSeparatorTextChipBinding binding;

        public SeparatorViewHolder(@NonNull ViewHolderSeparatorTextChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderPickedViewListBinding binding;

        public ItemViewHolder(@NonNull ViewHolderPickedViewListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听器
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //设置长按监听
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    public PickedViewListAdapter(
            AdapterOnClickListener<PickedViewEntity> clickListener,
            AdapterOnLongClickListener<PickedViewEntity> longClickListener
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
        return getItem(position) instanceof PickedViewListUiModel.Separator;
    }

    @Override
    public String getHeaderData(int position, Context context) {
        PickedViewListUiModel model = getItem(position);
        if (model instanceof PickedViewListUiModel.Separator) {
            return AppListHelper.getAppNameByPackageName(((PickedViewListUiModel.Separator) model).text, context);
        } else if (model instanceof PickedViewListUiModel.Item) {
            return AppListHelper.getAppNameByPackageName(((PickedViewListUiModel.Item) model).entity.getPackageName(), context);
        } else {
            return context.getString(R.string.not_applicable);
        }
    }

    @Override
    public int getItemViewType(int position) {
        PickedViewListUiModel item = getItem(position);
        if (item instanceof PickedViewListUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderPickedViewListBinding binding = ViewHolderPickedViewListBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ItemViewHolder(
                    binding,
                    new ViewHolderListener() {
                        @Override
                        public void onClick(int position, View anchor) {
                            PickedViewListUiModel model = getItem(position);
                            if (model instanceof PickedViewListUiModel.Item) {
                                clickListener.onClick(((PickedViewListUiModel.Item) model).entity, anchor);
                            }
                        }

                        @Override
                        public void onLongClick(int position, View anchor) {
                            PickedViewListUiModel model = getItem(position);
                            if (model instanceof PickedViewListUiModel.Item) {
                                longClickListener.onLongClick(((PickedViewListUiModel.Item) model).entity, anchor);
                            }
                        }

                        @Override
                        public void onCheckedChange(int pos, boolean finalStat, View anchor) {
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
        PickedViewListUiModel model = getItem(position);
        Context context = holder.itemView.getContext();

        if (model instanceof PickedViewListUiModel.Separator && holder instanceof SeparatorViewHolder) {
            String text = AppListHelper.getAppNameByPackageName(((PickedViewListUiModel.Separator) model).text, context);
            ((SeparatorViewHolder) holder).binding.separatorText.setText(text);
        } else if (model instanceof PickedViewListUiModel.Item && holder instanceof ItemViewHolder) {
            PickedViewEntity entity = ((PickedViewListUiModel.Item) model).entity;
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.binding.remarkText.setText(entity.getRemark());              //备注
            itemHolder.binding.appNameText.setText(AppListHelper.getAppNameByPackageName(   //应用名称
                    entity.getPackageName(), context
            ));

            //视图 ID
            String viewId = entity.getViewId();
            itemHolder.binding.viewIdText.setText(viewId == null || viewId.isEmpty() ? "<无编号>" : viewId);

            //界面名称
            String[] parts = entity.getActivityName().split("\\.");
            String activityName = parts.length > 0 ? parts[parts.length - 1] : "<未知界面>";
            itemHolder.binding.activityNameText.setText(activityName);

            //设置圆角
            setRadius(itemHolder.binding.getRoot(), position);
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
        PickedViewListUiModel front = getItem(position - 1);
        if (position == getItemCount() - 1) {   //处理最后一个卡片的圆角
            if (front instanceof PickedViewListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前一个是分隔视图，判断为单独类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //前一个不是分隔视图，判断为底部类型
            }
        } else {
            PickedViewListUiModel behind = getItem(position + 1);

            if (front instanceof PickedViewListUiModel.Separator && behind instanceof PickedViewListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.SINGLE); //前后都是分隔视图，判断为单独类型
            } else if (front instanceof PickedViewListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.TOP);    //前一个是分隔但后一个不是，判断为顶部类型
            } else if (behind instanceof PickedViewListUiModel.Separator) {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.BOTTOM); //后一个是分隔但前一个不是，判断为底部类型
            } else {
                AppearanceHelper.setRadiusStyle(view, RadiusStyle.MIDDLE); //前后都不是分隔视图，判断为中间类型
            }
        }
    }
}
