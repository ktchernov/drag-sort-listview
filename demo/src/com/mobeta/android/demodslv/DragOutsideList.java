package com.mobeta.android.demodslv;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ktchernov on 19/10/2014.
 */
public class DragOutsideList extends ListActivity {

    private ArrayAdapter<String> adapter;

    private String[] array;
    private ArrayList<String> list;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    String item=adapter.getItem(from);

                    adapter.notifyDataSetChanged();
                    adapter.remove(item);
                    adapter.insert(item, to);
                }
            };

    private DragSortListView.RemoveListener onRemove =
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                    adapter.remove(adapter.getItem(which));
                }
            };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drag_outside_list);

        DragSortListView lv = (DragSortListView) getListView();

        lv.setDropListener(onDrop);
        lv.setRemoveListener(onRemove);

        DragSortController dragSortController = new DragOutsideSortController(lv);
        dragSortController.setDragHandleId(R.id.drag_handle);

        lv.setFloatViewManager(dragSortController);
        lv.setOnTouchListener(dragSortController);

        array = getResources().getStringArray(R.array.jazz_artist_names);
        list = new ArrayList<String>(Arrays.asList(array));

        adapter = new ArrayAdapter<String>(this, R.layout.list_item_handle_left, R.id.text, list);
        setListAdapter(adapter);
    }


    private static class DragOutsideSortController extends DragSortController {
        public static final int REMOVE_DELAY_MILLIS = 500;
        public static final int SCROLL_TIMEOUT_MILLIS = 500;
        private DragSortListView dragSortListView;
        private long lastScrollTime;

        private Handler handler = new Handler();
        private Runnable delayedStopAndRemoveRunnable;

        public DragOutsideSortController(DragSortListView dslv) {
            super(dslv);
            dragSortListView = dslv;
            dragSortListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                private int previousFirstVisible;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (previousFirstVisible != firstVisibleItem) {
                        previousFirstVisible = firstVisibleItem;
                        lastScrollTime = System.currentTimeMillis();
                        stopDelayedCallback();
                    }
                }
            });
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
            super.onDragFloatView(floatView, floatPoint, touchPoint);

            if (floatView != null) {
                int listViewHeight = dragSortListView.getMeasuredHeight();
                int floatViewHeightThreshold = floatView.getMeasuredHeight() / 2;
                int lastVisiblePosition = dragSortListView.getLastVisiblePosition();
                int numItems = dragSortListView.getAdapter().getCount();

                boolean draggingOutsideUp = floatPoint.y < -floatViewHeightThreshold && dragSortListView.getFirstVisiblePosition() == 0;
                boolean draggingOutsideDown = (floatPoint.y + floatViewHeightThreshold > listViewHeight) && lastVisiblePosition == numItems - 1;

                if (draggingOutsideUp || draggingOutsideDown) {
                    boolean scrollTimedOut = (lastScrollTime == 0 || System.currentTimeMillis() - lastScrollTime > SCROLL_TIMEOUT_MILLIS);
                    if (delayedStopAndRemoveRunnable == null && scrollTimedOut) {
                        delayedStopAndRemoveRunnable = new StopAndRemoveRunnable();
                        handler.postDelayed(delayedStopAndRemoveRunnable, REMOVE_DELAY_MILLIS);
                    }
                }
            }
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            super.onDestroyFloatView(floatView);
            stopDelayedCallback();
        }

        private void stopDelayedCallback() {
            handler.removeCallbacks(delayedStopAndRemoveRunnable);
            delayedStopAndRemoveRunnable = null;
        }

        private class StopAndRemoveRunnable implements Runnable {
            @Override
            public void run() {
                dragSortListView.stopDrag(true);
                delayedStopAndRemoveRunnable = null;
            }
        }
    }

}
