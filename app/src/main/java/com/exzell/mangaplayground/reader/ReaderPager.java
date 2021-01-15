package com.exzell.mangaplayground.reader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.function.Consumer;
import java.util.function.Function;

import kotlin.jvm.functions.Function1;


public class ReaderPager extends ViewPager {

    private Function1<MotionEvent, Boolean> onSingleTap;

    private Consumer<Integer> onPageChanged;

    private GestureDetectorCompat mGestureDetector = new GestureDetectorCompat(getContext(), new SwipeListener(null, new Function1<MotionEvent, Boolean>() {
        @Override
        public Boolean invoke(MotionEvent motionEvent) {
            return onSingleTap.invoke(motionEvent);
        }
    }));

    private SimpleOnPageChangeListener mPageListener = new SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected(int position) {
            onPageChanged.accept(position);
        }
    };


    public ReaderPager(@NonNull Context context) {
        super(context);
        init();
    }

    public ReaderPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        addOnPageChangeListener(mPageListener);
    }

    public void setOnSingleTap(Function1<MotionEvent, Boolean> onSingleTap) {
        this.onSingleTap = onSingleTap;
    }

    public void setOnPageChanged(Consumer<Integer> onPageChanged) {
        this.onPageChanged = onPageChanged;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean ret = mGestureDetector.onTouchEvent(ev);
        if(!ret) ret = super.dispatchTouchEvent(ev);
        return ret;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearOnPageChangeListeners();
    }
}
