package com.shen.accountbook3.ui.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shen.accountbook3.R;
import com.shen.accountbook3.Utils.AppUtils;
import com.shen.accountbook3.Utils.FileSizeUtils;
import com.shen.accountbook3.Utils.InputStream2StringUtil;
import com.shen.accountbook3.Utils.LogUtils;
import com.shen.accountbook3.Utils.MemorySizeUtils;
import com.shen.accountbook3.Utils.ToFormatUtil;
import com.shen.accountbook3.Utils.ToastUtil;
import com.shen.accountbook3.config.Constant;
import com.shen.accountbook3.db.biz.TableEx;
import com.shen.accountbook3.domain.UpdateVersionInfo;
import com.shen.accountbook3.global.AccountBookApplication;
import com.shen.accountbook3.service.DownAPKService;
import com.shen.accountbook3.ui.view.CommonProgressDialog;
import com.shen.accountbook3.ui.view.VersionUpdateDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shen on 9/9 0009.
 */
public class OtherFragment extends BaseFragment {

    protected static final String tag = "OtherFragment";


    // 标志位，标志已经初始化完成。
    private boolean isPrepared;

    /** 更新新版本的状态码*/
    protected static final int UPDATE_VERSION = 100;
    /** 当前是最新版本*/
    protected static final int LATEST_VERSION = 101;
    /** 设置进度条的当前进度*/
    protected static final int PROGRESS_PERCENT = 102;
    /** 隐藏进度条*/
    protected static final int PROGRESS_GONE = 103;



    private String mCurrentVersionName;         // 当前版本名称
    private int mCurrentVersionCode;         // 当前版本号

    /******************************标题***********************************/
    private TextView tvTitle;


    private TextView mTvAddTest;             // 添加测试数据
    private TextView mTvCheckVersion;        // "检查版本"
    private TextView mTvVersionState;        // 弹出窗口"更新窗口"


    //    versionCode:主要是用于版本升级所用，是INT类型的，第一个版本定义为1，以后递增，这样只要判断该值就能确定是否需要升级，该值不显示给用户。
    //    versionName:这个是我们常说明的版本号，由三部分组成<major>.<minor>.<point>,该值是个字符串，可以显示给用户。
    //    关于版本号有两个值，如下面的例子
    //
    //    applicationId "com.xxx.xxx"
    //    versionCode  2
    //    versionName="1.1"
    //    ......
    //    versionCode是给设备程序识别版本(升级)用的，必须是一个interger值，整数，代表app更新过多少次
    //    versionName是给用户看的，可以写1.1 , 1.2等等版本
    private TextView mTvVersionCode;                        // 显示版本号
    private TextView mTvVersionName;                        // 显示版本名称

    private UpdateVersionInfo mUpdateVersionInfo = null;

    private VersionUpdateDialog mVersionUpdateDialog;       // "更新窗口"对话框

    CommonProgressDialog mDialog;                               // 进度条对话框
    Thread thread;
    boolean mStop;                                              // 线程停止标志

    /******************************资源详情***********************************/
    private TextView mTvExternalMemorySize;                     // SD卡存储空间
    //private TextView mTvInternalMemorySize;                     // 内部存储空间
    private TextView mTvAppResourceLocation;                    // 本应用资源存放位置
    private TextView mTvAppResourceSize;                        // 资源占用
    private TextView mTvAppUserResourceLocation;                // 当前用户资源存放位置
    private TextView mTvAppUserResourceSize;                    // 资源占用

    /******************************下载文件的进度条**************************/
    private LinearLayout mLvProgress;
    private ProgressBar mProgressBar;
    private TextView mTvProgressPercent;                        // 进度条上的百分比
    private TextView mTvFileSizePercent;


    public OtherFragment() {
    }


    private Handler mHandler = new Handler(){
        @Override
        //alt+ctrl+向下箭头,向下拷贝相同代码
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    mTvVersionState.setText("最新版本:"+ msg.obj);
                    //弹出对话框,提示用户更新
                    showVersionUpdateDialog(AppUtils.getVersionName(mContext),mUpdateVersionInfo.versionName,mUpdateVersionInfo.versionDes);
                    break;
                case LATEST_VERSION:
                    ToastUtil.show("当前是最新版本");
                    mTvVersionState.setText("当前是最新版本");
                    break;
                case PROGRESS_PERCENT:
                    // Activityon在Destroy之前，activity#isFinishing返回false，
                    // Activityon在Destroy之后，返回true。
                    if (!mActivity.isFinishing()){
                        if(mLvProgress.getVisibility() == View.GONE)
                            mLvProgress.setVisibility(View.VISIBLE);
                        mProgressBar.setProgress(msg.getData().getInt("ProgressPercent"));
                        mTvProgressPercent.setText(msg.getData().getInt("ProgressPercent")+"%");
                        mTvFileSizePercent.setText(msg.getData().getString("FileSizePercent"));
                    }
                    break;
                case PROGRESS_GONE:
                    // Activityon在Destroy之前，activity#isFinishing返回false，
                    // Activityon在Destroy之后，返回true。
                    if (!mActivity.isFinishing()){
                        if(mLvProgress.getVisibility() == View.VISIBLE)
                            mLvProgress.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View initUI() {
        View view = View.inflate(mContext, R.layout.fragment_other, null);

        /******************************标题***********************************/
        tvTitle = (TextView) view.findViewById(R.id.tv_title);

        mTvAddTest = (TextView) view.findViewById(R.id.tv_addTest);
        mTvCheckVersion = (TextView) view.findViewById(R.id.tv_check_version);
        mTvVersionState = (TextView) view.findViewById(R.id.tv_version_state);

        mTvVersionCode = (TextView) view.findViewById(R.id.tv_versionCode);
        mTvVersionName = (TextView) view.findViewById(R.id.tv_versionName);

        /******************************资源详情***********************************/
        mTvExternalMemorySize = (TextView) view.findViewById(R.id.tv_External_Memory_Size);
        //mTvInternalMemorySize = (TextView) view.findViewById(R.id.tv_Internal_Memory_Size);
        mTvAppResourceLocation = (TextView) view.findViewById(R.id.tv_app_resource_location);
        mTvAppResourceSize = (TextView) view.findViewById(R.id.tv_app_resource_size);
        mTvAppUserResourceLocation = (TextView) view.findViewById(R.id.tv_app_user_resource_location);
        mTvAppUserResourceSize = (TextView) view.findViewById(R.id.tv_app_user_resource_size);

        /******************************下载文件的进度条**************************/
        mLvProgress = (LinearLayout) view.findViewById(R.id.layout_progress);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_downfile);
        mTvProgressPercent = (TextView) view.findViewById(R.id.tv_progress_percent);
        mTvFileSizePercent = (TextView) view.findViewById(R.id.tv_filesize_Percent);

        return view;
    }

    @Override
    public void initListener() {
        mTvAddTest.setOnClickListener(this);
        mTvCheckVersion.setOnClickListener(this);
    }

    @Override
    public void initData() {
        tvTitle.setText("其他");
        mStop = false;

        isPrepared = true;
        lazyLoad();

        checkVersion();
    }

    @Override
    protected void lazyLoad() {
        LogUtils.i("OtherFragment:========isPrepared:"+isPrepared+"=======isVisible:"+isVisible);
        if(!isPrepared || !isVisible) {
            return;
        }
        //填充各控件的数据
        mCurrentVersionCode = AppUtils.getVersionCode(mContext);
        mCurrentVersionName = AppUtils.getVersionName(mContext);

        //mTvVersionState.setText("");
        mTvVersionCode.setText("versionCode:  "+ mCurrentVersionCode);
        mTvVersionName.setText("versionName:  "+ mCurrentVersionName);

        /******************************资源详情***********************************/
        mTvExternalMemorySize.setText(MemorySizeUtils.formatFileSize(MemorySizeUtils.getAvailableExternalMemorySize(),false)
                +"/"+ MemorySizeUtils.formatFileSize(MemorySizeUtils.getTotalExternalMemorySize(),false));
        //mTvInternalMemorySize.setText(MemorySizeUtils.formatFileSize(MemorySizeUtils.getAvailableInternalMemorySize(),false)
        //        +"/"+ MemorySizeUtils.formatFileSize(MemorySizeUtils.getTotalInternalMemorySize(),false));

        mTvAppResourceLocation.setText(Constant.APP_RESOURCE_PATH);
        mTvAppResourceSize.setText(FileSizeUtils.getAutoFileOrFilesSize(Constant.APP_RESOURCE_PATH));

        if(AccountBookApplication.isLogin()) {
            mTvAppUserResourceLocation.setText(Constant.IMAGE_PATH + AccountBookApplication.getUserInfo().getUserName() + File.separator);
            mTvAppUserResourceSize.setText(
                    FileSizeUtils.getAutoFileOrFilesSize(Constant.IMAGE_PATH + AccountBookApplication.getUserInfo().getUserName() + File.separator));
        }else{
            mTvAppUserResourceLocation.setText("");
            mTvAppUserResourceSize.setText("");

        }
    }

    /************************ 对话框——"加载数据窗口" 和 "版本更新"  ******************************/

    /**
     * 显示添加数据的窗口
     */
    private void showDialog(){
        mDialog = new CommonProgressDialog(getActivity()) {
            @Override
            public void cancel() {          // 点击取消按钮
                mDialog.dismiss();
                mStop = true;
            }
        };
        mDialog.setMessage("正在添加");
        mDialog.show();
    }

    /**
     * 弹出对话框,提示用户更新
     *
     */
    private void showVersionUpdateDialog(String currentVersionName, String newVersionName, String versionDescribe){
        // android.view.WindowManager$BadTokenException: Unable to add window — token null
        // 导致报这个错是在于new AlertDialog.Builder(mcontext)，虽然这里的参数是AlertDialog.Builder(Context context)
        // 但我们不能使用getApplicationContext()获得的Context,而必须使用Activity,因为只有一个Activity才能添加一个窗体。
        //
        // 解决方法：将new AlertDialog.Builder(Context context)中的参数用Activity.this（Activity是你的Activity的名称）
        // 来填充就可以正确的创建一个Dialog了。
        //对话框,是依赖于activity存在的
        mVersionUpdateDialog = new VersionUpdateDialog(getActivity()) {  // 注意这个上下文，用父的，还是自己的，全局的

            @Override
            public void up() {
                // downloadApk();

                Intent intent = new Intent(mActivity, DownAPKService.class);
                intent.putExtra("apk_url",mUpdateVersionInfo.downloadUrl);
                intent.putExtra("apk_dir",Constant.APP_RESOURCE_PATH);
                mActivity.startService(intent);

                mVersionUpdateDialog.dismiss();
            }

            @Override
            public void cancel() {
                mVersionUpdateDialog.dismiss();
            }
        };

        mVersionUpdateDialog.setCurrentVersionName(currentVersionName);
        mVersionUpdateDialog.setNewVersionName(newVersionName);
        mVersionUpdateDialog.setVersionDescribe(mContext.getResources().getString(R.string.the_indentation) + versionDescribe);
        mVersionUpdateDialog.show();
    }

    /**
     * 检测版本号
     *  json中内容包含:
     * 更新版本的版本名称
     * 新版本的描述信息
     * 服务器版本号
     * 新版本apk下载地址
     */
    private void checkVersion() {

        Request request = new Request.Builder().url(Constant.APK_URL).build();
        AccountBookApplication.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.i("失败了:" + e.getMessage());
                ToastUtil.show("检测失败!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = response.body().byteStream();
                String result = InputStream2StringUtil.Inputstr2Str_ByteArrayOutputStream(is, "UTF-8");
                processData(result);

                LogUtils.i("检测成功了");
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
        Message msg = Message.obtain();
        try {
            // 將json解析到  参数2：Javabean类字节码
            // ***返回一个 参数2的javabean类
            mUpdateVersionInfo = gson.fromJson(result, UpdateVersionInfo.class);
            LogUtils.i("json String:\n" + mUpdateVersionInfo.toString());

            // 比对版本号(服务器版本号>本地版本号,提示用户更新)
            if(mCurrentVersionCode < Integer.parseInt(mUpdateVersionInfo.versionCode)){
                msg.what = UPDATE_VERSION;                                   // 提示用户更新,弹出对话框(UI),消息机制
                msg.obj = mUpdateVersionInfo.versionName;
            }else{
                msg.what = LATEST_VERSION;                                  // 进入应用程序主界面
            }
            mHandler.sendMessage(msg);                                          // 发送消息
        }catch (Exception e){
            LogUtils.i("Gson Error:" + e.getMessage());
        }

    }

    /**
     * 下载apk <p>
     * 使用了 xUtils
     */
    protected void downloadApk() {

        //1,判断sd卡是否可用,是否挂在上
        if(MemorySizeUtils.externalMemoryAvailable()) {

            // 从网址中获取"文件名"
            String url = mUpdateVersionInfo.downloadUrl;
            String fileName = url.substring(url.lastIndexOf('/')+1);

            //2,获取sd路径
            final String path = Constant.APP_RESOURCE_PATH + fileName;

            Request request = new Request.Builder().url(mUpdateVersionInfo.downloadUrl).build();
            AccountBookApplication.getmOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.i("失败了:" + e.getMessage());
                    ToastUtil.show("链接失败!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    final InputStream is = response.body().byteStream();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                File file = FilesUtils.InputStream2File(is, path);      // 将下载的文件的"流"弄成"File"
//                                LogUtils.i("is2file 成功");
//
//                                installApk(file);                                       // 提示用户安装
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();


                    final Response myResponse = response;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                File file = saveFile(myResponse, path);
                                installApk(file);                                       // 提示用户安装
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }
    }


    /**
     * 保存文件
     * @param response
     * @param path
     * @return
     * @throws IOException
     */
    public File saveFile(Response response, String path) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();                          // 拿到响应体的"流"
            long total = response.body().contentLength();

            long sum = 0;

            File file = new File(path);                                 // 文件
            fos = new FileOutputStream(file);

            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                int intSum = Integer.valueOf(ToFormatUtil.toDecimalFormat((finalSum * 1.0f / total)*100, 0));
                LogUtils.i("下载进度:" + ToFormatUtil.toDecimalFormat((finalSum * 1.0f / total)*100, 2) + "%");
                String fileSizePercent = MemorySizeUtils.formatFileSize(sum,false) + "/" +
                        MemorySizeUtils.formatFileSize(total,false);

                Message msg = Message.obtain();
                msg.what = PROGRESS_PERCENT;                                   // 提示用户更新,弹出对话框(UI),消息机制
                Bundle bundle = new Bundle();
                bundle.putInt("ProgressPercent",intSum);
                bundle.putString("FileSizePercent",fileSizePercent);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

            }
            fos.flush();

            return file;

        } finally {
            try {
                response.body().close();
                if (is != null) is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
            Message msg = Message.obtain();
            msg.what = PROGRESS_GONE;                                   // 提示用户更新,弹出对话框(UI),消息机制
            mHandler.sendMessage(msg);
        }
    }


    /**
     * 安装对应apk
     * @param file	安装文件
     */
    protected void installApk(File file) {
        //（安装apk的界面是）系统应用界面,源码,安装apk入口
        // 使用"隐式意图"
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        /**
         * //文件作为数据源
         * intent.setData(Uri.fromFile(file));
         * //设置安装的类型
         * intent.setType("application/vnd.android.package-archive");
         */
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        // startActivity(intent);
        startActivityForResult(intent, 0);
    }

    /**
     * 添加测试数据!
     */
    public void addTest(){

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                final int MAINTTPE = 12;
                int TYPE1 = 0;
                int mt_Random = 0;
                int t1_Random = 0;
                int M = 1;
                int day = 1;
                int max = 0;
                int startYear = 2016;
                int endYear = 2018;
                int yearIndex = 900;           // 每年循环1800次，分摊到每一天
                int progress = 0;


                TableEx tableEx = new TableEx(AccountBookApplication.getContext());
                tableEx.Delete(Constant.TABLE_USER,"name=?",new String[]{"test"});
                tableEx.Delete(Constant.TABLE_CONSUMPTION,"user=?",new String[]{"test"});

//                String path = "data/data/com.shen.accountbook3/databases/AccountBook3.db";
//                SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,
//                        SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.CREATE_IF_NECESSARY );//读写、若不存在则创建

                // db.delete(Constant.TABLE_CONSUMPTION,"user=?",new String[]{"test"});

                // 添加"test用户"

                ContentValues userValues = new ContentValues();
                //   userValues.put("id", id);                    // 主键可以不写
                userValues.put("name", "test");                        // 字段  ： 值
                userValues.put("password", "123456");
                userValues.put("sex", 1);
                tableEx.Add(Constant.TABLE_USER, userValues);




                String[] mt ={"就餐","交通","零食","蒲","教育","生活用品","各种月费",
                        "家电","电子产品","交通工具","游戏","其他"};

                String[] ty0 ={"早餐","午餐","晚餐","夜宵","饭局","非正餐","其他"};
                String[] ty1 ={"船","火车","地铁","公交车","飞机","神州n号","的士","快车","其他"};
                String[] ty2 ={"饮料","酒","其他"};
                String[] ty3 ={"喝酒","唱K","其他"};
                String[] ty4 ={"学车","夜校","孩子学费","技能考试","其他"};
                String[] ty5 ={"洗头水","沐浴露","牙膏","洗衣粉","毛巾","其他"};
                String[] ty6 ={"仅房租","房租+水电","水费","电费","话费","其他"};
                String[] ty7 ={"电视","洗衣机","空调","其他"};
                String[] ty8 ={"手机","手表","其他"};
                String[] ty9 ={"自行车","摩托","其他"};
                String[] ty10 ={"其他"};
                String[] ty11 ={"其他"};

                Random ra =new Random();

                max = yearIndex * (endYear - startYear);        // 插入的总条数
                mDialog.setMax(max);

                for(int y = startYear; y<endYear; y++) {
                    for (int i = 0; i < yearIndex; i++) {
                        String type1 = null;
                        String maintype = null;
                        float price = 0;
                        int number = 0;
                        float unitPrice = 0;

                        mt_Random = ra.nextInt(MAINTTPE);
                        maintype = mt[mt_Random];

                        switch (mt_Random) {
                            case 0:
                                TYPE1 = 7;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty0[t1_Random];
                                break;
                            case 1:
                                TYPE1 = 9;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty1[t1_Random];
                                break;
                            case 2:
                                TYPE1 = 3;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty2[t1_Random];
                                break;
                            case 3:
                                TYPE1 = 3;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty3[t1_Random];
                                break;
                            case 4:
                                TYPE1 = 5;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty4[t1_Random];
                                break;
                            case 5:
                                TYPE1 = 6;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty5[t1_Random];
                                break;
                            case 6:
                                TYPE1 = 6;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty6[t1_Random];
                                break;
                            case 7:
                                TYPE1 = 4;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty7[t1_Random];
                                break;
                            case 8:
                                TYPE1 = 3;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty8[t1_Random];
                                break;
                            case 9:
                                TYPE1 = 3;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty9[t1_Random];
                                break;
                            case 10:
                                TYPE1 = 1;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty10[t1_Random];
                                break;
                            case 11:
                                TYPE1 = 1;
                                t1_Random = ra.nextInt(TYPE1);
                                type1 = ty11[t1_Random];
                                break;
                        }

                        number = ra.nextInt(10) + 1;                        // 数量
                        unitPrice = ra.nextFloat() * (100 - 1) + 1;         // 单价
                        price = number * unitPrice;                         // 总价

                        String id = null;   // 空不要写成 ""  ，不要写成 "null" 要写成String id = null;  主键可以不写
                        String concreteness = "自定义";
                        String image = null;    // 空不要写成 ""  ，不要写成 "null" 要写成String image = null;
                        String date = null;
                        image = "test.png";

                        NumberFormat nf = NumberFormat.getInstance();   // 得到一个NumberFormat的实例
                        nf.setGroupingUsed(false);                      // 设置是否使用分组
                        nf.setMaximumIntegerDigits(2);                  // 设置最大整数位数
                        nf.setMinimumIntegerDigits(2);                  // 设置最小整数位数

                        if (0 < M && M < 12) {
                            if (0 < day && day <= 28) {
                                date = y + "-" + nf.format(M) + "-" + nf.format(day);
                                day++;
                            } else {
                                M++;
                                day = 1;
                                date = y + "-" +nf.format(M) + "-" + nf.format(day);
                                day++;
                            }
                        } else {
                            M = 1;
                            if (0 < day && day <= 28) {
                                date = y + "-" + nf.format(M) + "-" + nf.format(day);
                                day++;
                            } else {
                                M++;
                                day = 1;
                                date = y + "-" + nf.format(M) + "-" + nf.format(day);
                                day++;
                            }
                        }

                        ContentValues values = new ContentValues();
                        //   values.put("id", id);                   // 主键可以不写
                        values.put("user", "test");                        // 字段  ： 值
                        values.put("maintype", maintype);                        // 字段  ： 值
                        values.put("type1", type1);
                        values.put("concreteness", concreteness);
                        values.put("price", ToFormatUtil.toDecimalFormat(price, 2));
                        values.put("number", number);
                        values.put("unitPrice", ToFormatUtil.toDecimalFormat(unitPrice, 2));
                        values.put("image", image);
                        values.put("date", date);   // 这里只要填写 YYYY-M-DD  ，不用填date(2016-9-12 00:00:00) 这么麻烦

                        tableEx.Add(Constant.TABLE_CONSUMPTION, values);
                        progress++;
                        mDialog.setProgress(progress);

                        if(mStop)               // 对话框点击"取消"就会将 mStop = true
                            i = yearIndex+1;    // 使用"最大值"来停止"进程"
                    }
                    if(mStop)                   // 对话框点击"取消"就会将 mStop = true
                        y = endYear+1;          // 使用"最大值"来停止"进程"
                }
                mDialog.dismiss();

            }
        });
        thread.start();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_addTest:
                showDialog();
                mStop = false;
                addTest();
                break;

            case R.id.tv_check_version:
                checkVersion();
                break;

            default:
                break;
        }

    }



}
