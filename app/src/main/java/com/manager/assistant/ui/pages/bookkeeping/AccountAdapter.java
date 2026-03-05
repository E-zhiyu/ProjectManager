package com.manager.assistant.ui.pages.bookkeeping;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.material.shape.Shapeable;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.running_account.ExpenseRunningAccount;
import com.manager.assistant.data.classes.running_account.IncomeRunningAccount;
import com.manager.assistant.data.classes.running_account.RunningAccountBase;
import com.manager.assistant.data.classes.running_account.TransferRunningAccount;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.sync.account.AccountUpdateReason;
import com.manager.assistant.ui.sync.account.RunningAccountViewModel;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends GroupAdapter<GroupieViewHolder> {
    private final List<RunningAccountBase> accountList;         //数据源
    private final OnRunningAccountViewClickListener listener;   //单击接口
    private final HashMap<String, Section> sectionHashMap;      //分组哈希表

    /**
     * 流水记录点击接口
     */
    public interface OnRunningAccountViewClickListener {
        void onRunningAccountClick(RunningAccountBase runningAccountBase);
    }

    private static class HeaderItem extends Item<GroupieViewHolder> {
        private final String date;

        public HeaderItem(String date) {
            this.date = date;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int i) {
            MaterialTextView titleText = viewHolder.itemView.findViewById(R.id.header_title);
            titleText.setText(date);
        }

        @Override
        public int getLayout() {
            return R.layout.item_text_header;
        }
    }

    private static class ContentItem extends Item<GroupieViewHolder> {
        private final RunningAccountBase runningAccount;
        SpringAnimationOnTouchListener onTouchListener;
        OnRunningAccountViewClickListener onClickListener;

        /**
         * 内容Item构造方法
         *
         * @param runningAccount  流水记录实例
         * @param onClickListener 视图点击监听器
         */
        public ContentItem(@NonNull RunningAccountBase runningAccount, OnRunningAccountViewClickListener onClickListener) {
            this.runningAccount = runningAccount;
            this.onClickListener = onClickListener;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder groupieViewHolder, int i) {
            //设置触摸监听器
            Shapeable shapeable = (Shapeable) groupieViewHolder.itemView;
            Vibrator vibrator = (Vibrator) groupieViewHolder.itemView.getContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            onTouchListener = new SpringAnimationOnTouchListener(shapeable, vibrator);
            groupieViewHolder.itemView.setOnTouchListener(onTouchListener);

            MaterialTextView amountText = groupieViewHolder.itemView.findViewById(R.id.amount_text);
            MaterialTextView remarkText = groupieViewHolder.itemView.findViewById(R.id.remark_text);
            MaterialTextView typeDatetimeText = groupieViewHolder.itemView.findViewById(R.id.type_datetime_textview);

            String type = runningAccount.getType().getTitle();
            String datetime = runningAccount.getDatetime();
            String typeAndDatetime = String.format(Locale.getDefault(), "%s·%s", type, datetime);
            String remark = runningAccount.getRemark();
            double amount = runningAccount.getAmount();

            amountText.setText(String.format(Locale.getDefault(), "%.2f", amount));
            remarkText.setText(remark.isEmpty() ? runningAccount.getDefaultRemark() : remark);
            typeDatetimeText.setText(typeAndDatetime);

            groupieViewHolder.itemView.setOnClickListener(v -> onClickListener.onRunningAccountClick(runningAccount));
        }

        @Override
        public int getLayout() {
            return R.layout.item_running_account;
        }

        public long getRno() {
            return runningAccount.getRno();
        }
    }

    /**
     * 构造方法
     *
     * @param listener 流水记录点击监听
     */
    public AccountAdapter(OnRunningAccountViewClickListener listener) {
        this.accountList = new ArrayList<>();
        this.listener = listener;
        this.sectionHashMap = new HashMap<>();
    }

    /**
     * 添加新流水视图
     *
     * @param dataBundle 新建流水的数据包
     * @param owner      ViewModel提供者
     * @param context    上下文
     */
    public void addNewRunningAccount(
            @NonNull Bundle dataBundle,
            ViewModelStoreOwner owner,
            Context context
    ) {
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long rno = dataBundle.getLong(KeyValueStrings.RNO.getValue(), 0);
        if (rno == 0) return;   //如果为0则说明数据库保存失败，直接结束该方法

        //使用ViewModel刷新UI（主页的简易报表）
        RunningAccountViewModel viewModel = new ViewModelProvider(owner).get(RunningAccountViewModel.class);
        viewModel.onAccountUpdated(amount, datetime, type, AccountUpdateReason.ADD);

        //获取特殊数据并实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, datetime, amount, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        runningAccount.setRno(rno);  //保存流水编号

        //刷新UI
        this.accountList.add(0, runningAccount);
        String date = runningAccount.getDatetime().substring(0, 10);
        Section section = sectionHashMap.get(date);
        ContentItem contentItem = new ContentItem(runningAccount, listener);
        if (section == null) {
            Section newSection = new Section();
            sectionHashMap.put(date, newSection);
            HeaderItem headerItem = new HeaderItem(date);
            newSection.setHeader(headerItem);

            newSection.add(contentItem);
            this.add(0, newSection);
        } else {
            section.add(contentItem);
        }
    }

    /**
     * 在界面中添加新流水记录视图但是不保存到数据库(用于自动记账防止重复保存)
     *
     * @param dataBundle 新流水记录数据包
     * @param owner      ViewModel提供者
     * @param context    上下文
     */
    public void addNewRunningAccountByNotification(
            @NonNull Bundle dataBundle,
            ViewModelStoreOwner owner,
            Context context
    ) {
        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //使用ViewModel刷新UI（主页的简易报表）
        RunningAccountViewModel viewModel = new ViewModelProvider(owner).get(RunningAccountViewModel.class);
        viewModel.onAccountUpdated(amount, datetime, type, AccountUpdateReason.ADD);

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, datetime, amount, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        runningAccount.setRno(rno);

        //刷新UI
        this.accountList.add(0, runningAccount);
        String date = runningAccount.getDatetime().substring(0, 10);
        Section section = sectionHashMap.get(date);
        ContentItem contentItem = new ContentItem(runningAccount, listener);
        if (section == null) {
            Section newSection = new Section();
            sectionHashMap.put(date, newSection);
            HeaderItem headerItem = new HeaderItem(date);
            newSection.setHeader(headerItem);

            newSection.add(0, contentItem);
            this.add(0, newSection);
        } else {
            section.add(contentItem);
        }
    }

    /**
     * 修改指定下标的流水视图
     *
     * @param dataBundle 修改后的流水数据
     * @param owner      ViewModel提供者
     * @param context    上下文
     */
    public void modifyRunningAccount(@NonNull Bundle dataBundle, ViewModelStoreOwner owner, Context context) {
        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //使用ViewModel刷新UI（主页的简易报表）
        RunningAccountViewModel viewModel = new ViewModelProvider(owner).get(RunningAccountViewModel.class);
        viewModel.onAccountUpdated(amount, datetime, type, AccountUpdateReason.MODIFIED);

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, datetime, amount, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        runningAccount.setRno(rno);

        //更新列表中的数据
        String oldDatetime = "";                                            //原来的日期
        String newDatetime = runningAccount.getDatetime().substring(0, 10); //编辑后的日期
        for (int index = 0; index < accountList.size(); index++) {
            long list_rno = accountList.get(index).getRno();
            if (rno == list_rno) {
                oldDatetime = accountList.get(index).getDatetime().substring(0, 10);
                accountList.set(index, runningAccount);
                break;
            }
        }

        //更新UI
        if (oldDatetime.equals(newDatetime)) {
            Section section = sectionHashMap.get(oldDatetime);
            if (section != null) {
                List<ContentItem> contentItemList = new ArrayList<>();
                for (int position = 0; position < section.getItemCount(); position++) {
                    Item<?> item = section.getItem(position);
                    if (item instanceof ContentItem && ((ContentItem) item).getRno() == rno) {
                        contentItemList.add(new ContentItem(runningAccount, listener));
                    } else if (item instanceof ContentItem) {
                        contentItemList.add((ContentItem) item);
                    }
                }
                section.update(contentItemList);
            }
        } else {
            //删除旧的
            Section oldSection = sectionHashMap.get(oldDatetime);
            if (oldSection != null) {
                int old_count = oldSection.getItemCount();
                if (old_count > 2) {
                    List<ContentItem> contentItemList = new ArrayList<>();
                    for (int i = 0; i < old_count; i++) {
                        Item<?> item = oldSection.getItem(i);
                        if (item instanceof ContentItem && ((ContentItem) item).getRno() != rno) {
                            contentItemList.add((ContentItem) item);
                        }
                    }
                    oldSection.update(contentItemList);
                } else {
                    Log.d(LogTags.ACCOUNT_ADAPTER.getV(), "Section为空，删除整个Section");
                    this.remove(oldSection);
                    sectionHashMap.remove(oldDatetime);
                }
            }

            //添加新的
            Section newSection = sectionHashMap.get(newDatetime);
            ContentItem contentItem = new ContentItem(runningAccount, listener);
            if (newSection != null) {
                newSection.add(contentItem);
            } else {
                newSection = new Section();
                sectionHashMap.put(newDatetime, newSection);

                HeaderItem headerItem = new HeaderItem(newDatetime);
                newSection.setHeader(headerItem);
                newSection.add(contentItem);
                this.add(0, newSection);
            }
        }
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param rno_delete 待删除的流水记录的编号
     * @param owner      ViewModel的提供者
     * @param context    上下文
     */
    public void deleteRunningAccount(long rno_delete, ViewModelStoreOwner owner, Context context) {
        if (rno_delete == -1) {
            Log.e(LogTags.ACCOUNT_ADAPTER.getV(), "未获取到合法的流水编号，无法删除流水记录");
            return;
        } else {
            Log.i(LogTags.ACCOUNT_ADAPTER.getV(), String.format(Locale.getDefault(), "待删除流水编号：%d", rno_delete));
        }

        //从数据库中删除
        try {
            RunningAccountBase.deleteAccount(rno_delete, context);
            Log.i(LogTags.ACCOUNT_ADAPTER.getV(), "数据库中删除成功");
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Log.e(LogTags.ACCOUNT_ADAPTER.getV(), "数据库中删除失败");
            Toast.makeText(context, "流水记录删除失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //删除列表中的流水记录
        String date = "";
        for (int index = 0; index < accountList.size(); index++) {
            RunningAccountBase runningAccount = accountList.get(index);
            if (rno_delete == runningAccount.getRno()) {
                Log.i(LogTags.ACCOUNT_ADAPTER.getV(), "成功在List中找到需要删除的数据类");

                //使用ViewModel刷新UI（主页的简易报表）
                double amount = runningAccount.getAmount();
                String datetime = runningAccount.getDatetime();
                RunningAccountType type = runningAccount.getType();
                RunningAccountViewModel viewModel = new ViewModelProvider(owner).get(RunningAccountViewModel.class);
                viewModel.onAccountUpdated(amount, datetime, type, AccountUpdateReason.DELETE);

                date = runningAccount.getDatetime().substring(0, 10);
                accountList.remove(index);
                break;
            }
        }

        //删除Section中的流水条目
        if (date.isEmpty()) {
            Log.w(LogTags.ACCOUNT_ADAPTER.getV(), "未在List中找到需要删除的数据类");
            return;
        }
        Section section = sectionHashMap.get(date);
        if (section != null) {
            int item_num = section.getItemCount();

            //判断是否没有ContentItem(HeaderItem会占用一个数量，因此判断是否大于2)
            if (item_num > 2) {
                List<ContentItem> contentItemList = new ArrayList<>();
                for (int index = 0; index < section.getItemCount(); index++) {
                    Item<?> item = section.getItem(index);
                    if (item instanceof ContentItem && ((ContentItem) item).getRno() != rno_delete) {
                        contentItemList.add((ContentItem) item);
                    }
                }
                section.update(contentItemList);
            } else {
                //当没有任何一个ContentItem时删除该Section
                Log.d(LogTags.ACCOUNT_ADAPTER.getV(), "Section为空，删除整个Section");
                sectionHashMap.remove(date);    //删除哈希表中的Section
                remove(section);                //从适配器中移除
            }
        }
    }

    /**
     * 刷新流水账视图
     *
     * @param refreshedList 刷新后的流水账数据列表
     */
    public void refreshRunningAccount(List<RunningAccountBase> refreshedList) {
        //清空整个视图
        sectionHashMap.clear();
        accountList.clear();
        accountList.addAll(refreshedList);
        this.clear();

        //依次添加视图
        for (RunningAccountBase runningAccount : accountList) {
            String datetime = runningAccount.getDatetime();
            String date = datetime.substring(0, 10);

            Section dateSection = sectionHashMap.get(date);
            ContentItem contentItem = new ContentItem(runningAccount, listener);
            if (dateSection == null) {
                Section section = new Section();
                sectionHashMap.put(date, section);
                HeaderItem headerItem = new HeaderItem(date);
                section.setHeader(headerItem);

                section.add(contentItem);
                this.add(section);
            } else {
                dateSection.add(contentItem);
            }
        }
    }
}
