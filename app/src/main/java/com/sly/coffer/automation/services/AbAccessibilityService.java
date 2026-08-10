package com.sly.coffer.automation.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;

import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleEntity;
import com.sly.coffer.data.save.db.entities.composite.AccessibilityRuleWithDetailModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressLint("AccessibilityPolicy")
public class AbAccessibilityService extends AccessibilityService {
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final Map<String, List<AccessibilityRuleWithDetailModel>> ruleCacheMap = new HashMap<>();
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(\\d+\\.?\\d{0,2})");

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(db.accessibilityRuleDao().getOpenedAccessibilityRuleWithDetailFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        modelList -> {
                            ruleCacheMap.clear();
                            Map<String, List<AccessibilityRuleWithDetailModel>> map = modelList.stream()
                                    .collect(Collectors.groupingBy(
                                            model -> model.getRule().getPackageName(),
                                            HashMap::new,
                                            Collectors.toList()
                                    ));

                            ruleCacheMap.putAll(map);
                        },
                        e -> Log.e(LogTags.AB_ACCESSIBILITY_SERVICE.n(), "无障碍规则获取失败")
                )
        );
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        //获取包名
        CharSequence packageName = event.getPackageName();
        if (packageName == null) return;
        String pkgName = packageName.toString();
        Log.d(LogTags.AB_ACCESSIBILITY_SERVICE.n(), "包名：" + pkgName);

        //获取匹配的规则
        List<AccessibilityRuleWithDetailModel> modelList = ruleCacheMap.get(pkgName);
        if (modelList == null || modelList.isEmpty()) {
            Log.d(LogTags.AB_ACCESSIBILITY_SERVICE.n(), "规则中不包含该包名");
            return;
        }

        //获取当前窗口根节点
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // 2. 遍历该应用下的规则进行匹配与数据提取
        for (AccessibilityRuleWithDetailModel model : modelList) {
            AccessibilityRuleEntity rule = model.getRule();

            //如果规则指定了 Activity，校验 Activity 类名
            if (!TextUtils.isEmpty(rule.getTargetActivity())
                    && event.getClassName() != null
                    && !rule.getTargetActivity().equals(event.getClassName().toString())) {
                continue;
            }

            // 3. 根据 viewId 匹配并尝试提取金额文本
            String rawAmountText = extractTextByViewId(rootNode, rule.getViewId());
            if (!TextUtils.isEmpty(rawAmountText)) {
                String cleanAmount = parseAmountStr(rawAmountText);
                if (!TextUtils.isEmpty(cleanAmount)) {
                    //TODO:生成流水记录
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 根据 viewId 在节点树中查找对应的文本
     *
     * @param rootNode 界面的根节点
     * @param viewId   目标视图的 ID
     */
    @Nullable
    private String extractTextByViewId(AccessibilityNodeInfo rootNode, String viewId) {
        if (TextUtils.isEmpty(viewId)) return null;

        //TODO:加上位置匹配
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes != null && !nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getText() != null) {
                    return node.getText().toString();
                }
            }
        }
        return null;
    }

    /**
     * 从原始文本中清洗提取出合法的金额字符串（如 "￥88.50" -> "88.50"）
     *
     * @param rawText 包含金额数据的原始文本
     */
    @Nullable
    private String parseAmountStr(String rawText) {
        Matcher matcher = AMOUNT_PATTERN.matcher(rawText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
