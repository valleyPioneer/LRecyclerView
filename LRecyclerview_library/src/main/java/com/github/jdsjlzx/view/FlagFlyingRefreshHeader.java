package com.github.jdsjlzx.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jdsjlzx.R;
import com.github.jdsjlzx.interfaces.IRefreshHeader;
import com.github.jdsjlzx.progressindicator.AVLoadingIndicatorView;
import com.github.jdsjlzx.recyclerview.ProgressStyle;

import java.util.Date;


public class FlagFlyingRefreshHeader extends RelativeLayout implements IRefreshHeader {

    private LinearLayout mContainer;
    private ImageView mArrowImageView;
    private TextView mStatusTextView;
    private int mState = STATE_NORMAL;


    /** 正向和反向旋转动画 */
    private Animation reverseAnim;
    private Animation forwardAnim;



    private static final int ROTATE_ANIM_DURATION = 1000;

    public int mMeasuredHeight;

    private int hintColor;

    public FlagFlyingRefreshHeader(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public FlagFlyingRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {

        /** 可以有效地解决xml中隐藏控件再显示的问题 */
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_flag_flying_refresh_header, null);
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        mArrowImageView = (ImageView)findViewById(R.id.listview_header_arrow);
        mStatusTextView = (TextView)findViewById(R.id.refresh_status_textview);


        reverseAnim = new RotateAnimation(0.0f, -360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        reverseAnim.setDuration(ROTATE_ANIM_DURATION);
        reverseAnim.setFillAfter(true);
        reverseAnim.setRepeatCount(-1);

        forwardAnim = new RotateAnimation(-360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        forwardAnim.setDuration(ROTATE_ANIM_DURATION);
        forwardAnim.setFillAfter(true);
        forwardAnim.setRepeatCount(-1);

        measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();

        hintColor = R.color.dark;
    }

    public void setState(int state) {
        if (state == mState) {
            return;
        }

        if (state == STATE_REFRESHING) {	// 显示进度
            /** 确保操作执行，防止被主线程其他部分的UI操作挤掉 */
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(forwardAnim);
                }
            });
            smoothScrollTo(mMeasuredHeight);
        }

        mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), hintColor));

        switch(state){
            case STATE_NORMAL:
                if (mState == STATE_RELEASE_TO_REFRESH) {
                    mArrowImageView.startAnimation(reverseAnim);
                }
                mStatusTextView.setText(R.string.listview_header_hint_normal);
                break;
            case STATE_RELEASE_TO_REFRESH:
                if (mState != STATE_RELEASE_TO_REFRESH) {
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(forwardAnim);
                    mStatusTextView.setText(R.string.listview_header_hint_release);
                }
                break;
            case STATE_REFRESHING:
                mStatusTextView.setText(R.string.refreshing);
                break;
            case STATE_DONE:
                mStatusTextView.setText(R.string.refresh_done);
                break;
            default:
        }

        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void refreshComplete(){
        setState(STATE_DONE);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                reset();
            }
        }, 200);
    }

    @Override
    public View getHeaderView() {
        return this;
    }

    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        LayoutParams lp = (LayoutParams) mContainer .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisibleHeight() {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

    //垂直滑动时该方法不实现
    @Override
    public int getVisibleWidth() {
        return 0;
    }

    @Override
    public void onReset() {
        setState(STATE_NORMAL);
    }

    @Override
    public void onPrepare() {
        setState(STATE_RELEASE_TO_REFRESH);
    }

    @Override
    public void onRefreshing() {
        setState(STATE_REFRESHING);
    }

    @Override
    public void onMove(float offSet, float sumOffSet) {

        if (getVisibleHeight() > 0 || offSet > 0) {
            setVisibleHeight((int) offSet + getVisibleHeight());
            if (mState <= STATE_RELEASE_TO_REFRESH) { // 未处于刷新状态，更新箭头
                if (getVisibleHeight() > mMeasuredHeight) {
                    onPrepare();
                } else {
                    onReset();
                }
            }
        }
        else if(getVisibleHeight() <= 0){
            /** 回弹后清除动画 */
            mArrowImageView.clearAnimation();
        }
    }

    @Override
    public boolean onRelease() {
        boolean isOnRefresh = false;
        int height = getVisibleHeight();
        if (height == 0) {// not visible.
            isOnRefresh = false;
        }

        if(getVisibleHeight() > mMeasuredHeight &&  mState < STATE_REFRESHING){
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height > mMeasuredHeight) {
            smoothScrollTo(mMeasuredHeight);
        }
        if (mState != STATE_REFRESHING) {
            smoothScrollTo(0);
        }

        if (mState == STATE_REFRESHING) {
            int destHeight = mMeasuredHeight;
            smoothScrollTo(destHeight);
        }

        return isOnRefresh;
    }

    public void reset() {
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                setState(STATE_NORMAL);
            }
        }, 500);
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

}