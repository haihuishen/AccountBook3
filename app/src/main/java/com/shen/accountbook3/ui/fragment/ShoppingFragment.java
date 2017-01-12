package com.shen.accountbook3.ui.fragment;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shen.accountbook3.R;
import com.shen.accountbook3.Utils.BitmapUtils.CacheUtils;
import com.shen.accountbook3.Utils.BitmapUtils.MyBitmapUtils;
import com.shen.accountbook3.Utils.LogUtils;
import com.shen.accountbook3.Utils.MyOkHttpUtils;
import com.shen.accountbook3.Utils.ToastUtil;
import com.shen.accountbook3.config.Constant;
import com.shen.accountbook3.domain.PhotosBean;
import com.shen.accountbook3.global.AccountBookApplication;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by shen on 9/9 0009.
 */
public class ShoppingFragment extends BaseFragment{

    // 标志位，标志已经初始化完成。
    private boolean isPrepared;

    /******************************标题***********************************/
    private TextView tvTitle;
    private ImageButton mBtnLvGv;

    /******************************下拉刷新布局***********************************/
    private SwipeRefreshLayout mSwipeRefreshLayout_Lv;

    /******************************加载成功***********************************/
    /** 加载成功显示的layout*/
    private View mLayoutLoadSuccess;
    /** 组图的样式——listview*/
    private ListView mLvPhoto;
    /** 组图的样式——GridView 九宫格之类的*/
    private GridView mGvPhoto;

    /******************************加载失败***********************************/
    /** 加载失败显示的layout*/
    private View mLayoutLoadError;
    /** 重新加载的按钮*/
    private Button mRefreshTry;

    /**
     *  图片对象	————"{}"	<br>
     *  public class PhotoNews {<br>
     *  	public int id;<br>
     *  	public String listimage;<br>
     *  	public String title;<br>
     *  }
     */
    private ArrayList<PhotosBean.Shens> mShensList;

    private Handler mHandler ;

    public ShoppingFragment() {
    }

    @Override
    public View initUI() {

        // 將 R.layout.base_pager布局 填充成 view,作为其布局
        View view = View.inflate(mContext, R.layout.fragment_shopping, null);

        /******************************标题***********************************/
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        mBtnLvGv = (ImageButton) view.findViewById(R.id.btn_lv_gv);

        // 下拉刷新布局
        mSwipeRefreshLayout_Lv = (SwipeRefreshLayout)  view.findViewById(R.id.swipeLayout_lv);

        //加载成功
        mLayoutLoadSuccess = view.findViewById(R.id.layout_Load_success);
        mLvPhoto = (ListView) view.findViewById(R.id.lv_photo);
        mGvPhoto = (GridView) view.findViewById(R.id.gv_photo);

        //加载失败
        mLayoutLoadError = view.findViewById(R.id.layout_load_error);
        mRefreshTry = (Button) view.findViewById(R.id.btn_retry);



        return view;
    }

    @Override
    public void initListener() {
        mBtnLvGv.setOnClickListener(this);

        mRefreshTry.setOnClickListener(this);


        //设置刷新时动画的颜色，可以设置4个
        mSwipeRefreshLayout_Lv.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        // 下拉刷新
        mSwipeRefreshLayout_Lv.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getDataFromServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // 为 listview设置 "项点击事件"监听
        mLvPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                LogUtils.i("第" + position + "个被点击了");
            }
        });
    }


    @Override
    public void initData() throws IOException {

        mHandler = AccountBookApplication.getHandler();

        tvTitle.setText("商城");
        // mBtnLvGv.setVisibility(View.VISIBLE);
        mBtnLvGv.setVisibility(View.GONE);

        mLayoutLoadSuccess.setVisibility(View.GONE);      // 一开始隐藏
        mLayoutLoadError.setVisibility(View.GONE);        // 一开始隐藏

        isPrepared = true;
        lazyLoad();
    }

    @Override
    protected void lazyLoad() throws IOException {
        LogUtils.i("ShoppingFragment:========isPrepared:"+isPrepared+"=======isVisible:"+isVisible);
        if(!isPrepared || !isVisible) {
            return;
        }

        // 拿缓存
        String cache = CacheUtils.getCache(Constant.PHOTOS_URL,mContext);
        if (!TextUtils.isEmpty(cache)) {
            processData(cache);
            mLayoutLoadSuccess.setVisibility(View.VISIBLE);
            mLayoutLoadError.setVisibility(View.GONE);
            mLvPhoto.setAdapter(new PhotoAdapter());
        }else {
            //填充各控件的数据
            getDataFromServer();
        }
    }


    /**
     * 使用OkHttp  网络
     * 拿到数据
     */
    private void getDataFromServer() throws IOException {

        MyOkHttpUtils.getInstence().requestGetAsyn(Constant.PHOTOS_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout_Lv.setRefreshing(false);
                        mLayoutLoadSuccess.setVisibility(View.GONE);
                        mLayoutLoadError.setVisibility(View.VISIBLE);

                        ToastUtil.show("加载失败");
                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                PhotosBean photosBean = (PhotosBean) MyOkHttpUtils.fromJson(response, PhotosBean.class);
                mShensList = photosBean.getShen();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout_Lv.setRefreshing(false);
                        mLayoutLoadSuccess.setVisibility(View.VISIBLE);
                        mLayoutLoadError.setVisibility(View.GONE);
                        mLvPhoto.setAdapter(new PhotoAdapter());
                    }
                });
            }
        });

    }

    /**
     * 解析json 数据
     *
     * Gson: Google Json<br>
     * 要使用到gson-2.3.1.jar
     *
     * @param result			String类型（json文件的内容）
     */
    protected void processData(String result) {

        // Gson: Google Json
        Gson gson = new Gson();
        PhotosBean photosBean = null;
        try {
            // 將json解析到  参数2：Javabean类字节码
            // ***返回一个 参数2的javabean类
            photosBean = gson.fromJson(result, PhotosBean.class);
        }catch (Exception e){
            LogUtils.i("Gson Error:" + e.getMessage());
        }
        if(photosBean != null) {
            mShensList = photosBean.getShen();
        }
    }

    /**
     * 适配器<p>
     *
     * 适合listview  和  GridView
     *
     *
     */
    class PhotoAdapter extends BaseAdapter {

        private MyBitmapUtils mBitmapUtils;

        public PhotoAdapter() {
            mBitmapUtils = new MyBitmapUtils();

        }

        @Override
        public int getCount() {
            return mShensList.size();
        }

        @Override
        public PhotosBean.Shens getItem(int position) {
            return mShensList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                // 讲R.layout.list_item_photos布局 填充成  view 作为"每一个项"
                convertView = View.inflate(mContext, R.layout.item_list_photos, null);

                holder = new ViewHolder();
                holder.ivPic = (ImageView) convertView.findViewById(R.id.iv_pic);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);

                convertView.setTag(holder);	// 将复用控件类，设到view中

            } else {
                holder = (ViewHolder) convertView.getTag();  // 从 缓存view中拿到，复用控件类
            }

            PhotosBean.Shens item = getItem(position);

            holder.tvTitle.setText(item.title);
            mBitmapUtils.display(holder.ivPic, item.listimage);

            return convertView;
        }

    }


    /**
     * 存放"R.layout.list_item_photos布局"的控件的声明的了<p>
     * 为了复用(省内存)<p>
     *	复用控件类
     */
    static class ViewHolder {
        public ImageView ivPic;
        public TextView tvTitle;
    }


    /**
     *  标记当前是否是listview展示<br>
     *  或是想使用(GridView)
     */
    private boolean isListView = true;


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_lv_gv:
                if (isListView) {
                    // 切成gridview
                    mLvPhoto.setVisibility(View.GONE);
                    mGvPhoto.setVisibility(View.VISIBLE);
                    mBtnLvGv.setImageResource(R.mipmap.icon_pic_list_type);

                    isListView = false;
                } else {
                    // 切成listview
                    mLvPhoto.setVisibility(View.VISIBLE);
                    mGvPhoto.setVisibility(View.GONE);
                    mBtnLvGv.setImageResource(R.mipmap.icon_pic_grid_type);

                    isListView = true;
                }
                break;

            case R.id.btn_retry:
                try {
                    getDataFromServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtils.i("来了这里了：HomeFragment");
    }

}
