package com.shen.accountbook3.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shen.accountbook3.R;
import com.shen.accountbook3.Utils.LogUtils;
import com.shen.accountbook3.Utils.MyOkHttpUtils;
import com.shen.accountbook3.Utils.ToastUtil;
import com.shen.accountbook3.config.Constant;
import com.shen.accountbook3.db.biz.TableEx;
import com.shen.accountbook3.domain.UserInfo;
import com.shen.accountbook3.global.AccountBookApplication;
import com.shen.loadingdialog.View.SpotsDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by shen on 11/3 0003.
 */
public class ChangePasswordActivity extends Activity implements View.OnClickListener{

    private Context mContext;

    private Handler mHandler;

    private TextView tvTitle;
    private ImageButton btnMenu;
    private ImageButton btnBack;

    private EditText etOldPassWord;
    private EditText etNewPassWord;
    private EditText etNewPassWordConfirm;

    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mContext = this;

        mHandler = AccountBookApplication.getHandler();
        initView();
        initListener();
        initData();
    }

    private void initView(){
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnMenu = (ImageButton) findViewById(R.id.btn_menu);
        btnBack = (ImageButton) findViewById(R.id.btn_back);

        etOldPassWord = (EditText) findViewById(R.id.et_old_password);
        etNewPassWord = (EditText) findViewById(R.id.et_new_password);
        etNewPassWordConfirm = (EditText) findViewById(R.id.et_new_password_confirm);

        btnConfirm = (Button) findViewById(R.id.btn_confirm);

    }

    private void initListener(){
        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    private void initData(){
        tvTitle.setText("修改密码");
        btnMenu.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
    }

    /**
     * 修改密码
     */
    private void changePassword(){
        if(!TextUtils.isEmpty(etOldPassWord.getText().toString())){
            if(AccountBookApplication.getUserInfo().getPassWord().equals(etOldPassWord.getText().toString())){
                if(!TextUtils.isEmpty(etNewPassWord.getText())){
                    if(etNewPassWord.getText().toString().length() >= 6){
                        if(!TextUtils.isEmpty(etNewPassWordConfirm.getText())){
                            if(etNewPassWord.getText().toString().equals(etNewPassWordConfirm.getText().toString())){

                                final UserInfo userInfo = AccountBookApplication.getUserInfo();
                                userInfo.setPassWord(etNewPassWord.getText().toString());

                                // 将用户的数据生成 json
                                Gson gson = new Gson();
                                String json = gson.toJson(userInfo);
                                LogUtils.i(json);

                                final SpotsDialog checkDialog = new SpotsDialog(mContext,"正在修改密码");
                                checkDialog.show();

                                // 将数据传递到网站，正确插入后，回应true，将数据插入本地
                                MyOkHttpUtils.getInstence().requestPostAsyn(Constant.UPDATE_USER_INFO_URL,
                                        MyOkHttpUtils.JSON, json, new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        checkDialog.dismiss();
                                                        ToastUtil.show("链接失败");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if(response.body().string().equals("true")){
                                                    TableEx tableEx = new TableEx(mContext);
                                                    ContentValues values = new ContentValues();
                                                    values.put("password", userInfo.getPassWord());

                                                    final int num = tableEx.Update(Constant.TABLE_USER, values, "_id=? and password=?", new String[]{userInfo.getId()+"", etOldPassWord.getText().toString()});
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

                            }else{
                                ToastUtil.show("两次输入的新密码不同");
                            }
                        }else {
                            ToastUtil.show("请再次输入新密码");
                        }
                    }else{
                        ToastUtil.show("输入的密码不能小于6位");
                    }
                }else {
                    ToastUtil.show("请输入新密码");
                }
            }else
                ToastUtil.show("您输入的旧密码有误");
        }else{
            ToastUtil.show("请输入旧密码");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:                                         // 退出本Activity
                finish();
                break;

            case R.id.btn_confirm:                               // 确认
                changePassword();
                break;
        }
    }
}
