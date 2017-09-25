package com.lzx.demo.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnItemLongClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.github.jdsjlzx.view.ArrowRefreshHeader;
import com.github.jdsjlzx.view.FlagFlyingRefreshFooter;
import com.github.jdsjlzx.view.FlagFlyingRefreshHeader;
import com.github.jdsjlzx.view.HArrowRefreshHeader;
import com.lzx.demo.R;
import com.lzx.demo.adapter.DataAdapter;
import com.lzx.demo.bean.ItemModel;
import com.lzx.demo.util.AppToast;
import com.lzx.demo.util.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 带HeaderView的分页加载LinearLayout RecyclerView
 */
public class EndlessLinearLayoutActivity extends AppCompatActivity{
    private static final String TAG = "lzx";

    /**服务器端一共多少条数据*/
    private static final int TOTAL_COUNTER = 34;//如果服务器没有返回总数据或者总页数，这里设置为最大值比如10000，什么时候没有数据了根据接口返回判断

    /**每一页展示多少条数据*/
    private static final int REQUEST_COUNT = 10;

    /**已经获取到多少条数据了*/
    private static int mCurrentCounter = 0;

    private LRecyclerView mRecyclerView = null;

    private DataAdapter mDataAdapter = null;

    private PreviewHandler mHandler = new PreviewHandler(this);
    private LRecyclerViewAdapter mLRecyclerViewAdapter = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_ll_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (LRecyclerView) findViewById(R.id.list);

        mDataAdapter = new DataAdapter(this);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

        /** 设置头部和尾部刷新布局 */
        mRecyclerView.setRefreshHeader(new FlagFlyingRefreshHeader(this));
        mRecyclerView.setLoadMoreFooter(new FlagFlyingRefreshFooter(this));

        mRecyclerView.setAdapter(mLRecyclerViewAdapter);

        DividerDecoration divider = new DividerDecoration.Builder(this)
                .setHeight(R.dimen.default_divider_height)
                .setPadding(R.dimen.default_divider_padding)
                .setColorResource(R.color.split)
                .build();

        mRecyclerView.addItemDecoration(divider);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {

                mDataAdapter.clear();
                mLRecyclerViewAdapter.notifyDataSetChanged();//fix bug:crapped or attached views may not be recycled. isScrap:false isAttached:true
                mCurrentCounter = 0;
                requestData();

            }
        });

        //是否禁用自动加载更多功能,false为禁用, 默认开启自动加载更多功能
        mRecyclerView.setLoadMoreEnabled(true);

        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                if (mCurrentCounter < TOTAL_COUNTER) {
                    // loading more
                    requestData();
                } else {
                    //the end
                    mRecyclerView.setNoMore(true);
                }
            }
        });

        mRecyclerView.setLScrollListener(new LRecyclerView.LScrollListener() {

            @Override
            public void onScrollUp() {
            }

            @Override
            public void onScrollDown() {
            }


            @Override
            public void onScrolled(int distanceX, int distanceY) {
            }

            @Override
            public void onScrollStateChanged(int state) {

            }

        });

        mRecyclerView.refresh();

        mLRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mDataAdapter.getDataList().size() > position) {
                    ItemModel item = mDataAdapter.getDataList().get(position);
                    AppToast.showShortText(getApplicationContext(), item.title);
                    mDataAdapter.remove(position);
                }

            }

        });

        mLRecyclerViewAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                ItemModel item = mDataAdapter.getDataList().get(position);
                AppToast.showShortText(getApplicationContext(), "onItemLongClick - " + item.title);
            }
        });

    }


    private void addItems(ArrayList<ItemModel> list) {

        mDataAdapter.addAll(list);
        mCurrentCounter += list.size();

    }

    private class PreviewHandler extends Handler {

        private WeakReference<EndlessLinearLayoutActivity> ref;

        PreviewHandler(EndlessLinearLayoutActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final EndlessLinearLayoutActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            switch (msg.what) {

                case -1:

                    int currentSize = activity.mDataAdapter.getItemCount();

                    //模拟组装10个数据
                    ArrayList<ItemModel> newList = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        if (newList.size() + currentSize >= TOTAL_COUNTER) {
                            break;
                        }

                        ItemModel item = new ItemModel();
                        item.id = currentSize + i;
                        item.title = "item" + (item.id);

                        newList.add(item);
                    }

                    activity.addItems(newList);

                    activity.mRecyclerView.refreshComplete(REQUEST_COUNT);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 模拟请求网络
     */
    private void requestData() {
        Log.d(TAG, "requestData");
        /** 模拟一秒的延迟信息 */
        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.sendEmptyMessage(-1);
            }
        }.start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_refresh) {
            mRecyclerView.forceToRefresh();
        }
        return true;
    }
}