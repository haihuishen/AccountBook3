package com.shen.accountbook3.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shen.accountbook3.R;
import com.shen.accountbook3.Utils.LogUtils;
import com.shen.accountbook3.Utils.MyOkHttpUtils;
import com.shen.accountbook3.Utils.ToastUtil;
import com.shen.accountbook3.config.Constant;
import com.shen.accountbook3.db.biz.TableEx;
import com.shen.accountbook3.domain.RegisterResultJSONBean;
import com.shen.accountbook3.global.AccountBookApplication;
import com.shen.loadingdialog.View.SpotsDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by shen on 9/1 0001.
 */
public class RegisterActivity extends Activity{

    private Context mContext;

    private ImageButton mMeun;
    private ImageButton mBack;
    private TextView mTitle;

    private EditText mUsename;
    private EditText mPassword;
    private RadioGroup mSex;
    private int Sex;

    TableEx tableEx;
    private Button mRegister;

    private Handler mHandler = AccountBookApplication.getHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext = this;

        initView();
        initListend();
    }


    private void initView(){
        mBack = (ImageButton) findViewById(R.id.btn_back);
        mMeun = (ImageButton) findViewById(R.id.btn_menu);
        mTitle = (TextView) findViewById(R.id.tv_title);

        mUsename = (EditText) findViewById(R.id.register_et_username);
        mPassword = (EditText) findViewById(R.id.register_et_password);
        mSex = (RadioGroup) findViewById(R.id.register_rg_sex);

        mRegister = (Button) findViewById(R.id.register_btn_register);
    }

    private void initListend(){
        mMeun.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mTitle.setText("注册界面");

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.register_rb_man:
                        Sex = 1;
                        System.out.println(Sex);
                        break;
                    case R.id.register_rb_woman:
                        Sex = 0;
                        System.out.println(Sex);
                        break;
                }
            }
        });

        // 拿到选中的 RadioButton
        if(mSex.getCheckedRadioButtonId() == R.id.register_rb_man)
            Sex = 1;
        else
            Sex = 0;

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(mUsename.getText().toString()) ||
                        TextUtils.isEmpty(mPassword.getText().toString())) {
                    Toast.makeText(getBaseContext(), "用户和密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    String name = mUsename.getText().toString();
                    String password = mPassword.getText().toString();
                    String sex = Sex + "";


                    tableEx = new TableEx(AccountBookApplication.getContext());
                    Cursor cursor = tableEx.Query(Constant.TABLE_USER, new String[]{"name"}, "name=?",
                            new String[]{name}, null, null, null);
                    String c_name = "";
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        c_name = cursor.getString(0);
                    }

                    if (!(c_name.equals(mUsename.getText().toString()))) {
                        upDate(name, password, sex);
                    } else{
                        ToastUtil.show("此用户已存在!");
                    }
                }
            }
        });
    }

    /**
     * 将注册信息上传到网站，根据回传的信息是否回传成功
     */
    private void upDate(final String name, final String password, final String sex){
        final SpotsDialog checkDialog = new SpotsDialog(mContext,"正在注册");
        checkDialog.show();


        List<MyOkHttpUtils.Param> params = new ArrayList<MyOkHttpUtils.Param>();

        params.add(new MyOkHttpUtils.Param("name", name));
        params.add(new MyOkHttpUtils.Param("password", password));
        params.add(new MyOkHttpUtils.Param("sex", sex));

        MyOkHttpUtils.getInstence().requestPostAsyn(Constant.REGISTER_URL, params, new Callback(){

            @Override
            public void onFailure(Call call, final IOException e) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkDialog.dismiss();
                        LogUtils.i("失败了:" + e.getMessage());
                        ToastUtil.show("注册失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                RegisterResultJSONBean jsonBean = (RegisterResultJSONBean) MyOkHttpUtils.fromJson(response, RegisterResultJSONBean.class);

                LogUtils.i("jsonBean:\n" + jsonBean.toString());

                if(jsonBean.getResult().equals("success") && jsonBean.getId() != 0) {

                    try {
                        ContentValues values = new ContentValues();
                        values.put("_id", jsonBean.getId());
                        values.put("name", name);                        // 字段  ： 值
                        values.put("password", password);
                        values.put("sex", sex);
                        tableEx.Add(Constant.TABLE_USER, values);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            checkDialog.dismiss();
                            finish();
                            ToastUtil.show("用户:" + name + "——注册成功");
                        }
                    });

                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            checkDialog.dismiss();
                            ToastUtil.show("注册失败!");
                        }
                    });
                }
            }
        });

    }

}
