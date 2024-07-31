

package boilerplate.widget.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class PhotoSpaceDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;
    private final int halfSpacing;
    private int spanCount = -1;

    public PhotoSpaceDecoration(Context context, @DimenRes int spacingDimen) {
        spacing = context.getResources().getDimensionPixelSize(spacingDimen);
        halfSpacing = spacing / 2;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (spanCount == -1) {
            spanCount = getTotalSpan(parent);
        }

        int childCount = parent.getLayoutManager().getItemCount();
        int childIndex = parent.getChildAdapterPosition(view);

        int itemSpanSize = getItemSpanSize(parent, childIndex);
        if (itemSpanSize == spanCount) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        int spanIndex = getItemSpanIndex(parent, childIndex);

        if (spanCount < 1) return;
        setSpacings(outRect, parent, childCount, childIndex, itemSpanSize, spanIndex);
    }

    protected void setSpacings(Rect outRect, RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {
        outRect.top = halfSpacing;
        outRect.bottom = halfSpacing;
        outRect.left = halfSpacing;
        outRect.right = halfSpacing;
        if (isTopEdge(parent, childIndex)) {
            outRect.top = spacing;
        }
        if (isLeftEdge(spanIndex)) {
            outRect.left = spacing;
        }
        if (isRightEdge(itemSpanSize, spanIndex)) {
            outRect.right = spacing;
        }
        if (isBottomEdge(parent, childCount, childIndex, spanIndex)) {
            outRect.bottom = spacing;
        }
    }

    protected int getTotalSpan(RecyclerView parent) {
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            return ((GridLayoutManager) mgr).getSpanCount();
        }
        if (mgr instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) mgr).getSpanCount();
        }
        if (mgr instanceof LinearLayoutManager) {
            return 1;
        }
        return -1;
    }

    protected int getItemSpanSize(RecyclerView parent, int childIndex) {
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            return ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanSize(childIndex);
        }
        if (mgr instanceof StaggeredGridLayoutManager) {
            return 1;
        }
        if (mgr instanceof LinearLayoutManager) {
            return 1;
        }
        return -1;
    }

    protected int getItemSpanIndex(RecyclerView parent, int childIndex) {
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            return ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanIndex(childIndex, spanCount);
        }
        if (mgr instanceof StaggeredGridLayoutManager) {
            return childIndex % spanCount;
        }
        if (mgr instanceof LinearLayoutManager) {
            return 0;
        }
        return -1;
    }

    protected boolean isLeftEdge(int spanIndex) {
        return spanIndex == 0;
    }

    protected boolean isRightEdge(int itemSpanSize, int spanIndex) {
        return (spanIndex + itemSpanSize) == spanCount;
    }

    protected boolean isTopEdge(RecyclerView parent, int childIndex) {
        return (childIndex == 0) || isFirstItemEdgeValid((childIndex < spanCount), parent, childIndex);

    }

    protected boolean isBottomEdge(RecyclerView parent, int childCount, int childIndex, int spanIndex) {
        return isLastItemEdgeValid((childIndex >= childCount - spanCount), parent, childCount, childIndex, spanIndex);
    }

    protected boolean isFirstItemEdgeValid(boolean isOneOfFirstItems, RecyclerView parent, int childIndex) {
        int totalSpanArea = 0;
        if (isOneOfFirstItems) {
            for (int i = childIndex; i >= 0; i--) {
                totalSpanArea = totalSpanArea + getItemSpanSize(parent, i);
            }
        }
        return isOneOfFirstItems && totalSpanArea <= spanCount;
    }

    protected boolean isLastItemEdgeValid(boolean isOneOfLastItems, RecyclerView parent, int childCount, int childIndex, int spanIndex) {
        int totalSpanRemaining = 0;
        if (isOneOfLastItems) {
            for (int i = childIndex; i < childCount; i++) {
                totalSpanRemaining = totalSpanRemaining + getItemSpanSize(parent, i);
            }
        }
        return isOneOfLastItems && (totalSpanRemaining <= spanCount - spanIndex);
    }
}