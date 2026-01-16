package com.project.manager.ui.others.bottom_sheets.tag;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount; // 列数
    private final int spacing;   // 间距（像素）
    private final boolean includeEdge; // 是否包含边缘间距

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // 当前 item 的位置
        int column = position % spanCount; // 计算当前 item 所在的列

        if (includeEdge) {
            // 包含边缘间距
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) { // 第一行
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // 默认底部间距
        } else {
            // 不包含边缘间距
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) { // 非第一行
                outRect.top = spacing;
            }
        }
    }
}
