package com.github.jdsjlzx.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jdsjlzx.R;
import com.github.jdsjlzx.interfaces.ILoadMoreFooter;
import com.github.jdsjlzx.progressindicator.AVLoadingIndicatorView;
import com.github.jdsjlzx.recyclerview.ProgressStyle;

public class FlagFlyingRefreshFooter extends RelativeLayout implements ILoadMoreFooter{

    protected State mState = State.Normal;
    private View mLoadingView;
    private View mTheEndView;

    private TextView mLoadingText;
    private TextView mNoMoreText;

    private String loadingHint;
    private String noMoreHint;

    private int hintColor = R.color.dark;

    public FlagFlyingRefreshFooter(Context context) {
        super(context);
        init(context);
    }

    public FlagFlyingRefreshFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlagFlyingRefreshFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        inflate(context, R.layout.layout_flag_flying_refresh_footer, this);
        setOnClickListener(null);

        onReset();//初始为隐藏状态

    }

    public State getState() {
        return mState;
    }

    public void setState(State status ) {
        setState(status, true);
    }


    @Override
    public void onReset() {
        onComplete();
    }

    @Override
    public void onLoading() {
        setState(State.Loading);
    }

    @Override
    public void onComplete() {
        setState(State.Normal);
    }

    @Override
    public void onNoMore() {
        setState(State.NoMore);
    }

    @Override
    public View getFootView() {
        return this;
    }

    /**
     * 设置状态
     *
     * @param status
     * @param showView 是否展示当前View
     */
    public void setState(State status, boolean showView) {
        if (mState == status) {
            return;
        }
        mState = status;

        switch (status) {

            case Normal:
                setOnClickListener(null);
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }

                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }

                break;
            case Loading:
                setOnClickListener(null);
                if (mTheEndView != null) {
                    mTheEndView.setVisibility(GONE);
                }

                if (mLoadingView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.loading_viewstub);
                    mLoadingView = viewStub.inflate();

                    mLoadingText = (TextView) mLoadingView.findViewById(R.id.loading_text);
                }

                mLoadingView.setVisibility(showView ? VISIBLE : GONE);


                mLoadingText.setText(TextUtils.isEmpty(loadingHint) ? getResources().getString(R.string.list_footer_loading) : loadingHint);
                mLoadingText.setTextColor(ContextCompat.getColor(getContext(), hintColor));

                break;
            case NoMore:
                setOnClickListener(null);
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(GONE);
                }

                if (mTheEndView == null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.end_viewstub);
                    mTheEndView = viewStub.inflate();

                    mNoMoreText = (TextView) mTheEndView.findViewById(R.id.loading_end_text);
                } else {
                    mTheEndView.setVisibility(VISIBLE);
                }

                mTheEndView.setVisibility(showView ? VISIBLE : GONE);
                mNoMoreText.setText(TextUtils.isEmpty(noMoreHint) ? getResources().getString(R.string.list_footer_end) : noMoreHint);
                mNoMoreText.setTextColor(ContextCompat.getColor(getContext(), hintColor));
                break;
            default:
                break;
        }
    }


    public enum State {
        Normal/**正常*/, NoMore/**加载到最底了*/, Loading/**加载中..*/
    }
}