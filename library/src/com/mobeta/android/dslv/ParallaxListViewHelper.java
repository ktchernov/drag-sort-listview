package com.mobeta.android.dslv;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

class ParallaxListViewHelper implements OnScrollListener {

	private static final float DISABLE_ALPHA_FACTOR = -1F;
    private static final float DEFAULT_ALPHA_FACTOR = 0.8F;
	private static final float DEFAULT_PARALLAX_FACTOR = 1.9F;
	private static final boolean DEFAULT_IS_CIRCULAR = true;
	private float parallaxFactor = DEFAULT_PARALLAX_FACTOR;
	private float alphaFactor = DEFAULT_ALPHA_FACTOR;
	private ParallaxedView parallaxedView;
	private boolean isCircular;
	private OnScrollListener listener = null;
	private ListView listView;

	ParallaxListViewHelper(Context context, AttributeSet attrs, ListView listView) {
		init(context, attrs, listView);
	}

	protected void init(Context context, AttributeSet attrs, ListView listView) {
		this.listView = listView;
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.DragSortListView);
        this.parallaxFactor = typeArray.getFloat(R.styleable.DragSortListView_parallax_factor, DEFAULT_PARALLAX_FACTOR);
        this.alphaFactor = typeArray.getFloat(R.styleable.DragSortListView_parallax_alpha_factor, DEFAULT_ALPHA_FACTOR);
        this.isCircular = typeArray.getBoolean(R.styleable.DragSortListView_circular_parallax, DEFAULT_IS_CIRCULAR);
        typeArray.recycle();
	}

	protected void setOnScrollListener(OnScrollListener l) {
		this.listener = l;
	}
	
	protected void addParallaxedHeaderView(View v) {
		addParallaxedView(v);
	}

	protected void addParallaxedView(View v) {
		this.parallaxedView = new ListViewParallaxedItem(v);
	}

	protected void parallaxScroll() {
		if (isCircular)
			circularParallax();
		else
			headerParallax();
	}

	private void circularParallax() {
		if (listView.getChildCount() > 0) {
			int top = -listView.getChildAt(0).getTop();
			if (top >= 0) {
				fillParallaxedViews();
				setFilters(top);
			}
            else if (parallaxedView != null) {
                parallaxedView.resetEffects();
                parallaxedView = null;
            }
		}
	}

	private void headerParallax() {
		if (parallaxedView != null) {
			if (listView.getChildCount() > 0) {
				int top = -listView.getChildAt(0).getTop();
				if (top >= 0) {
					setFilters(top);
				}
                else if (parallaxedView != null) {
                    parallaxedView.resetEffects();
                }
			}
		}
	}

	private void setFilters(int top) {
        float offset = (float)top / parallaxFactor;

		parallaxedView.setOffset(offset);
		if (alphaFactor != DISABLE_ALPHA_FACTOR) {
            int height = parallaxedView.getMeasuredHeight();
            float alphaMod = ((float) (height - top)) / ((float) height);
			float alpha = (alphaMod <= 0) ? 0 : alphaMod * alphaFactor;
            parallaxedView.setAlpha(alpha);
		}
		parallaxedView.animateNow();
	}

	private void fillParallaxedViews() {
		if (parallaxedView == null || !parallaxedView.is(listView.getChildAt(0))) {
			if (parallaxedView != null) {
				resetFilters();
				parallaxedView.setView(listView.getChildAt(0));
			} else {
				parallaxedView = new ListViewParallaxedItem(listView.getChildAt(0));
			}
		}
	}

	private void resetFilters() {
		parallaxedView.setOffset(0);
		if (alphaFactor != DISABLE_ALPHA_FACTOR)
			parallaxedView.setAlpha(1F);
		parallaxedView.animateNow();
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		parallaxScroll();
		if (this.listener != null)
			this.listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (this.listener != null)
			this.listener.onScrollStateChanged(view, scrollState);
	}
	
	protected class ListViewParallaxedItem extends ParallaxedView {

		public ListViewParallaxedItem(View view) {
			super(view);
		}
		
		@Override
		protected void translatePreICS(View view, float offset) {
			addAnimation(new TranslateAnimation(0, 0, offset, offset));
		}
	}
}
