package com.project.manager.ui.pages.bookkeeping;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.enums.KeyValueStrings;
import com.project.manager.data.data_class.running_account.ExpenseRunningAccount;
import com.project.manager.data.data_class.running_account.IncomeRunningAccount;
import com.project.manager.data.data_class.running_account.RunningAccountBase;
import com.project.manager.data.data_class.running_account.TransferRunningAccount;
import com.project.manager.enums.LogTags;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AccountRecyclerAdapter extends GroupAdapter<GroupieViewHolder> {
    private final List<RunningAccountBase> accountList;         //数据源
    private final Context context;                              //上下文
    private final OnRunningAccountViewClickListener listener;   //单击接口
    private final HashMap<String, Section> sectionHashMap;      //分组哈希表

    /**
     * 流水记录点击接口
     */
    public interface OnRunningAccountViewClickListener {
        void onRunningAccountClick(RunningAccountBase runningAccountBase);
    }

    static class HeaderItem extends Item<GroupieViewHolder> {
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
            return R.layout.item_date_header;
        }
    }

    public class ContentItem extends Item<GroupieViewHolder> {
        private final RunningAccountBase runningAccount;

        public ContentItem(@NonNull RunningAccountBase runningAccount) {
            this.runningAccount = runningAccount;
        }

        @Override
        public void bind(@NonNull GroupieViewHolder groupieViewHolder, int i) {
            MaterialTextView amountText = groupieViewHolder.itemView.findViewById(R.id.amount_text);
            MaterialTextView remarkText = groupieViewHolder.itemView.findViewById(R.id.remark_text);
            MaterialTextView typeDatetimeText = groupieViewHolder.itemView.findViewById(R.id.type_datetime_textview);

            String type = runningAccount.getType().getTitle();
            String datetime = runningAccount.getDatetime();
            String type_datetime = String.format(Locale.getDefault(), "%s·%s", type, datetime);
            String remark = runningAccount.getRemark();
            double amount = runningAccount.getAmount();

            amountText.setText(String.format(Locale.getDefault(), "%.2f", amount));
            remarkText.setText(remark);
            typeDatetimeText.setText(type_datetime);

            groupieViewHolder.itemView.setOnClickListener(v -> listener.onRunningAccountClick(runningAccount));
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
     * @param context  上下文
     */
    public AccountRecyclerAdapter(OnRunningAccountViewClickListener listener, Context context) {
        this.accountList = new ArrayList<>();
        this.listener = listener;
        this.context = context;
        this.sectionHashMap = new HashMap<>();
    }

    /**
     * 添加新流水视图
     *
     * @param dataBundle    新建流水的数据包
     * @param filter_tag_no 过滤器的标签编号
     */
    public void addNewRunningAccount(@NonNull Bundle dataBundle, long filter_tag_no) {
        //获取基本流水数据
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());
        if (tag_no == filter_tag_no || filter_tag_no == 0) {
            RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
            String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
            if (remark == null) remark = "";
            boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
            double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
            String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
            long rno = dataBundle.getLong(KeyValueStrings.RNO.getValue(), 0);
            if (rno == 0) return;   //如果为0则说明数据库保存失败，直接结束该方法

            //获取特殊数据并实例化流水类
            RunningAccountBase runningAccount;
            if (type == RunningAccountType.EXPENSE) {
                runningAccount = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
            } else if (type == RunningAccountType.INCOME) {
                runningAccount = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
            } else if (type == RunningAccountType.TRANSFER) {
                String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
                String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
                runningAccount = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
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
            ContentItem contentItem = new ContentItem(runningAccount);
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
    }

    /**
     * 在界面中添加新流水记录视图但是不保存到数据库(用于自动记账防止重复保存)
     *
     * @param dataBundle 新流水记录数据包
     */
    public void addNewRunningAccountByNotification(@NonNull Bundle dataBundle) {
        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
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
        ContentItem contentItem = new ContentItem(runningAccount);
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
     * 修改指定下标的流水视图
     *
     * @param dataBundle 修改后的流水数据
     */
    public void modifyRunningAccount(@NonNull Bundle dataBundle) {
        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        runningAccount.setRno(rno);

        //更新列表中的数据
        for (int index = 0; index < accountList.size(); index++) {
            long account_no = accountList.get(index).getRno();
            if (rno == account_no) {
                accountList.set(index, runningAccount);
                break;
            }
        }
        //遍历更新ContentItem以刷新UI
        Section section = sectionHashMap.get(runningAccount.getDatetime().substring(0, 10));
        if (section != null) {
            List<ContentItem> contentItemList = new ArrayList<>();
            for (int position = 0; position < section.getItemCount(); position++) {
                Item<?> item = section.getItem(position);
                if (item instanceof ContentItem && ((ContentItem) item).getRno() == rno) {
                    contentItemList.add(new ContentItem(runningAccount));
                } else if (item instanceof ContentItem) {
                    contentItemList.add((ContentItem) item);
                }
            }
            section.update(contentItemList);
        }
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param rno_to_delete 待删除的流水记录的编号
     */
    public void deleteRunningAccount(long rno_to_delete) {
        if (rno_to_delete == -1) {
            Log.e(LogTags.ACCOUNT_ADAPTER.getV(), "未获取到合法的流水编号，无法删除流水记录");
            return;
        } else {
            Log.i(LogTags.ACCOUNT_ADAPTER.getV(), String.format(Locale.getDefault(), "待删除流水编号：%d", rno_to_delete));
        }

        //从数据库中删除
        try {
            RunningAccountBase.deleteAccount(rno_to_delete, context);
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
            if (rno_to_delete == runningAccount.getRno()) {
                Log.i(LogTags.ACCOUNT_ADAPTER.getV(), "成功在List中找到需要删除的数据类");
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
                    if (item instanceof ContentItem && ((ContentItem) item).getRno() != rno_to_delete) {
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
            ContentItem contentItem = new ContentItem(runningAccount);
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
