package com.shen.accountbook3.ui.fragment.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.bm.library.PhotoView;
import com.google.gson.Gson;
import com.shen.accountbook3.R;
import com.shen.accountbook3.Utils.ImageFactory;
import com.shen.accountbook3.Utils.LogUtils;
import com.shen.accountbook3.Utils.MyOkHttpUtils;
import com.shen.accountbook3.Utils.PhotoSelectedHelper;
import com.shen.accountbook3.Utils.SetImageUtil;
import com.shen.accountbook3.Utils.ToastUtil;
import com.shen.accountbook3.config.Constant;
import com.shen.accountbook3.db.biz.TableEx;
import com.shen.accountbook3.domain.UserInfo;
import com.shen.accountbook3.global.AccountBookApplication;
import com.shen.accountbook3.ui.ChangePasswordActivity;
import com.shen.accountbook3.ui.view.ChangeDialog;
import com.shen.accountbook3.ui.view.CircleImageView;
import com.shen.loadingdialog.View.SpotsDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.PopupWindow.OnDismissListener;

/**
 * Created by shen on 10/30 0030.
 */
public class MineInfoActivity extends Activity implements View.OnClickListener,OnDismissListener{

    public static final int OK = 1;

    private Context mContext;

    private TextView tvTitle;
    private ImageButton btnMenu;
    private ImageButton btnBack;
    private Button btnFinish;

    private RelativeLayout layoutId;            // _id布局
    private RelativeLayout layoutHeadImage;     // 头像布局
    private RelativeLayout layoutUser;           // 用户名布局
    private RelativeLayout layoutSex;            // 性别布局
    private RelativeLayout layoutBirthday;     // 出生日期布局
    private RelativeLayout layoutQQ;             // 输入QQ布局
    private RelativeLayout layoutPassword;      // 更密码

    private TextView tvId;
    private TextView tvUser;
    private TextView tvSex;
    private TextView tvBirthday;
    private TextView tvQQ;

    private Bitmap mBitmap;
    /** "裁剪"过后图片的"结对路径"*/
    private String tempImagePath;               // "裁剪"过后图片的"结对路径"
    private CircleImageView civHead;

    private ChangeDialog changeDialogQQ;
    private ChangeDialog changeDialogUserName;

    /** "性别"条件选择器*/
    OptionsPickerView pvOptionsSex;
    /** "年龄"条件选择器*/
    TimePickerView pvTimeAge;


    //popwindow
    RelativeLayout rlPopNull;
    TextView tvPopCancels;
    TextView tvPopCheck;
    TextView tvPopChange;

    //popwindows
    RelativeLayout rlPopNulls;
    TextView tvPopPhoto;
    TextView tvPopCamera;
    TextView tvPopCancel;

    PopupWindow pop;                // 改变头像，查看头像
    PopupWindow pops;               // 选择头像的方式

    PhotoSelectedHelper mPhotoSelectedHelper;

    private final static int SHOW_POP = 1;
    private final static int SHOW_POPS = 2;

    View mParent;
    View mBg;
    /** 放大后存放图片的控件*/
    PhotoView mPhotoView;

    AlphaAnimation in;
    AlphaAnimation out;

    private Handler mHandler = AccountBookApplication.getHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_info);

        mContext = this;

        initView();
        initListener();
        initData();
        initialPopup();
        initialPopups();
    }

    private void initView(){
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnMenu = (ImageButton) findViewById(R.id.btn_menu);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnFinish = (Button) findViewById(R.id.btn_title_confirm);

        layoutHeadImage = (RelativeLayout) findViewById(R.id.layout_head_image);
        layoutId = (RelativeLayout) findViewById(R.id.layout_id);
        layoutUser = (RelativeLayout) findViewById(R.id.layout_user);
        layoutSex = (RelativeLayout) findViewById(R.id.layout_sex);
        layoutBirthday = (RelativeLayout) findViewById(R.id.layout_birthday);
        layoutQQ = (RelativeLayout) findViewById(R.id.layout_qq);
        layoutPassword = (RelativeLayout) findViewById(R.id.layout_password);

        civHead = (CircleImageView) findViewById(R.id.Circle_Image_head);

        tvId = (TextView) findViewById(R.id.tv_id);
        tvUser = (TextView) findViewById(R.id.tv_userid);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvBirthday = (TextView) findViewById(R.id.tv_birthdate);
        tvQQ = (TextView) findViewById(R.id.tv_qq);

        mParent = findViewById(R.id.parent);
        mBg = findViewById(R.id.bg);
        mPhotoView = (PhotoView) findViewById(R.id.img);

    }

    private void initListener(){
        btnBack.setOnClickListener(this);
        btnFinish.setOnClickListener(this);

        layoutHeadImage.setOnClickListener(this);
        layoutUser.setOnClickListener(this);
        layoutSex.setOnClickListener(this);
        layoutBirthday.setOnClickListener(this);
        layoutQQ.setOnClickListener(this);
        layoutPassword.setOnClickListener(this);

        mPhotoView.setOnClickListener(this);
    }

    private void initData(){
        tvTitle.setText("修改资料");
        btnMenu.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.VISIBLE);
        btnFinish.setText("完成");


        // 预览图片的动画
        in = new AlphaAnimation(0, 1);
        out = new AlphaAnimation(1, 0);

        in.setDuration(300);
        out.setDuration(300);
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mBg.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        /*******************************根据用户信息更新UI******************************/
        UserInfo userInfo = AccountBookApplication.getUserInfo();

        if(userInfo != null){
            tvId.setText(userInfo.getId()+"");
            tvUser.setText(userInfo.getUserName());
            tvSex.setText(userInfo.getSex() == 1 ? "男" : "女");
            tvBirthday.setText(userInfo.getBirthday());
            tvQQ.setText(userInfo.getQq());

            if(!TextUtils.isEmpty(userInfo.getImage()) &&
                new File(userInfo.getImage()).exists()) {                       // 如果"注册用户"有图片
                tempImagePath = userInfo.getImage();
                mBitmap = ImageFactory.getBitmap(userInfo.getImage());
            }else {                                                             // 如果"注册用户"没有图片
                tempImagePath = Constant.CACHE_IMAGE_PATH + "cat_head.png";
                mBitmap = ImageFactory.getBitmap(Constant.CACHE_IMAGE_PATH + "cat_head.png");
            }
            civHead.setImageBitmap(mBitmap);
        }

        /******************************************************************************/
        //时间选择器
        pvTimeAge = new TimePickerView(this, TimePickerView.Type.YEAR_MONTH_DAY);
        //控制时间范围
        Calendar calendar = Calendar.getInstance();
        pvTimeAge.setRange(calendar.get(Calendar.YEAR) - 116, calendar.get(Calendar.YEAR) + 50);//要在setTime 之前才有效果哦
        pvTimeAge.setTitle("年龄");         // 设置"标题"
        pvTimeAge.setTime(new Date());         // 设置当前的时间，到时间选择器
        pvTimeAge.setCyclic(false);             // 是否循环滚动
        pvTimeAge.setCancelable(false);         // true:点击弹出"布局"外部，收回"布局";false:没反应
        //时间选择后回调
        pvTimeAge.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                tvBirthday.setText(getTime(date));
            }
        });

        //选项选择器
        pvOptionsSex = new OptionsPickerView(this);
        pvOptionsSex.setCancelable(false);     // true:点击弹出"布局"外部，收回"布局";false:没反应

        final ArrayList<String> sexList = new ArrayList<String>();
        sexList.add("男");
        sexList.add("女");
        //三级联动效果
        pvOptionsSex.setPicker(sexList,null, null, true);
        //设置选择的三级单位
        // pwOptions.setLabels("主类型", "次类型", "次次类型");
        pvOptionsSex.setTitle("性别");
        pvOptionsSex.setCyclic(false, false, false);         // 三级联动，哪个可以循环滚动

        //设置默认选中的三级项目
        //监听确定选择按钮
        pvOptionsSex.setSelectOptions(1, 0, 0);
        //选项选择器后回调
        pvOptionsSex.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                String tx = sexList.get(options1);
                 //    +"-"+AccountBookApplication.getListType1List().get(options1).get(option2);
                //     + options3Items.get(options1).get(option2).get(options3).getPickerViewText();
                tvSex.setText(tx);
            }
        });
        /******************************************************************************/

        mPhotoSelectedHelper = new PhotoSelectedHelper(MineInfoActivity.this);
    }

    /**
     * 初始化popupwindow
     */
    private void initialPopup() {

        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View view = inflater.inflate(R.layout.popupwindow_change_image, null);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 创建PopupWindow对象
        pop = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        pop.setOnDismissListener(this);

        rlPopNull = (RelativeLayout) view.findViewById(R.id.layout_null);
        tvPopCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvPopCheck = (TextView) view.findViewById(R.id.tv_check_image);
        tvPopChange = (TextView) view.findViewById(R.id.tv_change_image);

        rlPopNull.setOnClickListener(this);
        tvPopCancel.setOnClickListener(this);
        tvPopCheck.setOnClickListener(this);
        tvPopChange.setOnClickListener(this);

        // 需要顺利让PopUpWindow dimiss（即点击PopuWindow之外的地方此或者back键PopuWindow会消失）；
        // PopUpWindow的背景不能为空。必须在popuWindow.showAsDropDown(v);
        // 或者其它的显示PopuWindow方法之前设置它的背景不为空：

        // 需要设置一下此参数，点击外边可消失
        pop.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        pop.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        pop.setFocusable(true);
    }

    /**
     * 初始化popupwindows
     */
    private void initialPopups() {
        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        View view = inflater.inflate(R.layout.popupwindows_third_image, null);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 创建PopupWindow对象
        pops = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        pops.setOnDismissListener(this);

        rlPopNulls = (RelativeLayout) view.findViewById(R.id.layout_nulls);
        tvPopCancels = (TextView) view.findViewById(R.id.tv_cancels);
        tvPopPhoto = (TextView) view.findViewById(R.id.tv_photo);
        tvPopCamera = (TextView) view.findViewById(R.id.tv_camera);

        rlPopNulls.setOnClickListener(this);
        tvPopCancels.setOnClickListener(this);
        tvPopPhoto.setOnClickListener(this);
        tvPopCamera.setOnClickListener(this);

        // 需要顺利让PopUpWindow dimiss（即点击PopuWindow之外的地方此或者back键PopuWindow会消失）；
        // PopUpWindow的背景不能为空。必须在popuWindow.showAsDropDown(v);
        // 或者其它的显示PopuWindow方法之前设置它的背景不为空：

        // 需要设置一下此参数，点击外边可消失
        pops.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        pops.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        pops.setFocusable(true);
    }

    /**
     * 显示popupwindow
     */
    private void showPopupWindow(int index) {
        if(index == SHOW_POP){
            if (pop.isShowing()) {
                // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                pop.dismiss();
            } else {
                // 显示窗口
                // pop.showAsDropDown(v);
                // 获取屏幕和PopupWindow的width和height
                pop.setAnimationStyle(R.style.MenuAnimationFade);           // 动画怎么设置，怎会动!
                pop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                pop.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                // 设置显示PopupWindow的位置位于View的左下方，x,y表示坐标偏移量
                pop.showAsDropDown(layoutHeadImage, 0, 0);                  // 绑定哪个控件来"控制"控件弹出  ???

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 0.7f;
                getWindow().setAttributes(params);
            }
        }else if(index == SHOW_POPS){
            if (pop != null) {
                pop.dismiss();
                if (pops.isShowing()) {
                    // 隐藏窗口，如果设置了点击窗口外小时即不需要此方式隐藏
                    pops.dismiss();
                } else {
                    // 显示窗口
                    // pop.showAsDropDown(v);
                    // 获取屏幕和PopupWindow的width和height
                    pops.setAnimationStyle(R.style.MenuAnimationFade);
                    pops.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    pops.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
                    pops.showAsDropDown(layoutHeadImage, 0, 0);

                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.alpha = 0.7f;
                    getWindow().setAttributes(params);
                }
            }
        }
    }


    /**
     * 将传进来的"时间"，按照一定的格式生成"时间字符串"
     * @param date 传进来的时间
     * @return
     */
    public static String getTime(Date date) {
        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
     * 显示"更改框"
     */
    private void showDialogUserName(){
        changeDialogUserName = new ChangeDialog(mContext) {  // 注意这个上下文，用父的，还是自己的，全局的
            @Override
            public void confirm(String text) {
                tvUser.setText(text);
                changeDialogUserName.dismiss();
            }
            @Override
            public void cancel() {
                changeDialogUserName.dismiss();
            }
        };

        changeDialogUserName.setTitle("更改\"用户名\"");
        changeDialogUserName.setEditInputType(ChangeDialog.EDIT_TEXT);
        changeDialogUserName.show();
    }

    /**
     * 显示"更改框"
     */
    private void showDialogQQ(){
        changeDialogQQ = new ChangeDialog(mContext) {  // 注意这个上下文，用父的，还是自己的，全局的
            @Override
            public void confirm(String text) {
                tvQQ.setText(text);
                changeDialogQQ.dismiss();
            }
            @Override
            public void cancel() {
                changeDialogQQ.dismiss();
            }
        };

        changeDialogQQ.setTitle("更改\"QQ\"");
        changeDialogQQ.setEditInputType(ChangeDialog.EDIT_NUMBER);
        changeDialogQQ.show();
    }


    /**
     * 点击"完成"按钮
     */
    private void confirm(){
        final UserInfo userInfo = new UserInfo();

        userInfo.setId(Long.valueOf(tvId.getText().toString()));
        userInfo.setUserName(tvUser.getText().toString());
        userInfo.setSex(tvSex.getText().toString().equals("男")? 1 : 0);
        userInfo.setImage(tempImagePath);
        userInfo.setBirthday(tvBirthday.getText().toString());
        userInfo.setQq(tvQQ.getText().toString());
        userInfo.setPassWord(AccountBookApplication.getUserInfo().getPassWord());

        // 将用户的数据生成 json
        Gson gson = new Gson();
        String json = gson.toJson(userInfo);
        LogUtils.i(json);

        final SpotsDialog checkDialog = new SpotsDialog(mContext,"正在修改资料");
        checkDialog.show();

        // 将数据传递到网站，正确插入后，回应true，将数据插入本地
        MyOkHttpUtils.getInstence().requestPostAsyn(Constant.UPDATE_USER_INFO_URL,
                MyOkHttpUtils.JSON, json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show("链接失败");
                        checkDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.body().string().equals("true")){
                    TableEx tableEx = new TableEx(mContext);
                    ContentValues values = new ContentValues();
                    values.put("name", userInfo.getUserName());
                    values.put("sex", userInfo.getSex());
                    values.put("image", userInfo.getImage());
                    values.put("birthday", userInfo.getBirthday());
                    values.put("qq", userInfo.getQq());

                    final int num = tableEx.Update(Constant.TABLE_USER, values, "_id=? and password=?", new String[]{userInfo.getId()+"", userInfo.getPassWord()});
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(num != 0) {
                                AccountBookApplication.setUserInfo(userInfo);
                                ToastUtil.show("修改成功");
                                checkDialog.dismiss();
                            }
                            else {
                                ToastUtil.show("修改失败");
                                checkDialog.dismiss();
                            }
                        }
                    });

                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show("修改失败");
                            checkDialog.dismiss();
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_back:                                         // 退出本Activity
                intent = new Intent();
                Bundle bundle = new Bundle();

                bundle.putBoolean("refresh",true);
                intent.putExtras(bundle);
                setResult(OK,intent);

                finish();
                break;

            case R.id.btn_title_confirm:                               // 完成按钮
                confirm();
                break;

            case R.id.layout_head_image:                               // 更换头像
                showPopupWindow(SHOW_POP);
                break;

            case R.id.layout_user:                                         // 名称更改
                if(changeDialogUserName == null)
                    showDialogUserName();
                else
                    changeDialogUserName.show();
                break;

            case R.id.layout_sex:                                        // 性别更改
                pvOptionsSex.show();                                     // 点击弹出选项选择器
                break;

            case R.id.layout_birthday:                                         // 年龄更改
                pvTimeAge.show();                                          // 弹出时间选择器
                break;

            case R.id.layout_qq:                                         // QQ更改
                if(changeDialogQQ == null)
                    showDialogQQ();
                else
                    changeDialogQQ.show();
                break;

            case R.id.layout_password:                                  // 密码更改
                intent = new Intent(MineInfoActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                break;

            /******************************* pop ************************************/
            case R.id.layout_null:                                      // popupwindow上的"空白布局"
                if (pop != null) {
                    pop.dismiss();
                }
                break;

            case R.id.img:                                          // 点击"放大后的预览图片的控件"，缩小、隐藏那个预览布局
                mBg.startAnimation(out);
                mParent.setVisibility(View.GONE);
                break;

            case R.id.tv_check_image:                                 // popupwindow上的"查看大头像"
                if (pop != null) {
                    pop.dismiss();
                }

                mBitmap = ImageFactory.getBitmap(tempImagePath);
                mPhotoView.setImageBitmap(mBitmap);
                mBg.startAnimation(in);             // 执行动画
                mBg.setVisibility(View.VISIBLE);
                mParent.setVisibility(View.VISIBLE);
                break;

            case R.id.tv_change_image:                                 // popupwindow上的"更换头像"
                showPopupWindow(SHOW_POPS);
                break;

            case R.id.tv_cancel:                                        // popupwindow上的"取消"
                if (pop != null) {
                    pop.dismiss();
                }
                break;
            /******************************* pops ************************************/
            case R.id.layout_nulls:                                      // popupwindows上的"空白布局"
                if (pops != null) {
                    pops.dismiss();
                }
                break;

            case R.id.tv_camera:                                        // popupwindows上的"拍照"
                if (pops != null) {
                    pops.dismiss();
                }
                if (AccountBookApplication.getUserInfo() != null) {
                    mPhotoSelectedHelper.imageSelection(AccountBookApplication.getUserInfo().getId()+"", "take");
                }
                ToastUtil.show("拍照");
                break;

            case R.id.tv_photo:                                         // popupwindows上的"从相册找头像"
                if (pops != null) {
                    pops.dismiss();
                }
                if (AccountBookApplication.getUserInfo() != null) {
                    mPhotoSelectedHelper.imageSelection(AccountBookApplication.getUserInfo().getId()+"", "pic");
                }
                ToastUtil.show("从相册找头像");
                break;

            case R.id.tv_cancels:                                        // popupwindows上的"取消"
                if (pops != null) {
                    pops.dismiss();
                }
                break;
        }
    }

    /**
     * 将图片上传到网络
     * @param imagePath 图片在手机上的路径
     */
    private void upLoadImage(String imagePath){
        final SpotsDialog checkDialog = new SpotsDialog(mContext,"正在上传图片");
        checkDialog.show();

        MyOkHttpUtils.getInstence().requestPostAsyn(Constant.UPDATE_IMAGE_URL, MyOkHttpUtils.MEDIA_TYPE_PNG, imagePath, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(checkDialog.isShowing())
                            checkDialog.dismiss();
                        LogUtils.i("upLoadImage:failure:"+e.getMessage());
                        ToastUtil.show("图片上传失败");
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UserInfo userInfo = AccountBookApplication.getUserInfo();     // 全局变量中头像图片的路径
                        userInfo.setImage(tempImagePath);
                        AccountBookApplication.setUserInfo(userInfo);

                        Long id = userInfo.getId();                                   // 修改数据库中 头像图片路径
                        String passWord = userInfo.getPassWord();

                        TableEx tableEx = new TableEx(mContext);
                        ContentValues values = new ContentValues();
                        values.put("image", tempImagePath);

                        int num = tableEx.Update(Constant.TABLE_USER, values, "_id=? and password=?",
                                new String[]{id+"", passWord});

                        if(num >= 0){
                            ToastUtil.show("图片上传成功");
                        }
                        if(checkDialog.isShowing())
                            checkDialog.dismiss();
                        try {
                            LogUtils.i("upLoadImage:true:"+response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == mPhotoSelectedHelper.TAKE_PHOTO) {           // "拍照"的返回
            if (!(resultCode == RESULT_OK)) {
                return;
            }

            if (data != null) {              // 如果返回的Intent不为"null" 从"Intent"拿到图片数据
                mPhotoSelectedHelper.cropImageUri(data.getData(), 200, 200,
                        AccountBookApplication.getUserInfo().getId()+"");
            } else {                        // 如果返回的Intent为"null" 从"拍照前设置的路径"拿到图片数据
                mPhotoSelectedHelper.cropImageUri(mPhotoSelectedHelper.getCaptureUri(), 200, 200,
                        AccountBookApplication.getUserInfo().getId()+"");
            }

        } else if (requestCode == mPhotoSelectedHelper.PHOTO_CROP) {    // "裁剪"的返回
            if (!(resultCode == RESULT_OK)) {
                return;
            }
            tempImagePath = mPhotoSelectedHelper.getCropPath();

            civHead.setImageBitmap(ImageFactory.getBitmap(tempImagePath));

            upLoadImage(tempImagePath);                                 // 上传图片到网络

            // 上传到网站
            // if (cropPath != null) {
            //     pDialog.show();
            //     new Thread() {
            //         @Override
            //         public void run() {
            //             super.run();
            //             upload(cropPath, "tack");
            //         }
            //     }.start();
            // }
        } else if (requestCode == mPhotoSelectedHelper.PIC_PHOTO) {     // 从"相册"返回
            if (data == null) {
                return;
            } else {
                Uri uri = data.getData();
                if (uri != null) {
                   String path = SetImageUtil.getPath(this, uri);       // 从"相册"中获取"图片"的路径要解析的

                    File mediaFile = new File(path);
                    //Uri u = Uri.parse("/storage/emulated/0/Pictures/com.shen.accountbook3/test_20161031_014039.jpg");
                    Uri u = Uri.fromFile(mediaFile);
                    // civHead.setImageBitmap(ImageFactory.getBitmap(path));
                    // 进去裁剪
                    mPhotoSelectedHelper.cropImageUri(u, 200, 200, AccountBookApplication.getUserInfo().getId()+"");
                }
            }
        }
    }




    // 按钮监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {                 // 如果点击的是"返回按钮"
            if(pvOptionsSex.isShowing()||pvTimeAge.isShowing()){
                pvOptionsSex.dismiss();
                pvTimeAge.dismiss();
                return true;
            }
            if(pvTimeAge.isShowing()){
                pvTimeAge.dismiss();
                return true;
            }

            if(changeDialogQQ != null){
                if(changeDialogQQ.isShowing()){
                    changeDialogQQ.dismiss();
                    return true;
                }
            }

            if(mParent.getVisibility() == View.VISIBLE && mBg.getVisibility() == View.VISIBLE){   // 缩小、隐藏那个预览布局
                mBg.startAnimation(out);
                mParent.setVisibility(View.GONE);
                return true;
            }

            Intent intent = new Intent();
            Bundle bundle = new Bundle();

            bundle.putBoolean("refresh",true);
            intent.putExtras(bundle);
            setResult(OK,intent);

            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onDismiss() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1f;
        getWindow().setAttributes(params);
    }
}
