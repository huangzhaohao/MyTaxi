package com.dalimao.mytaxi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dalimao.mytaxi.MyTaxiApplication;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.account.response.Account;
import com.dalimao.mytaxi.account.response.LoginResponse;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.http.impl.BaseRequest;
import com.dalimao.mytaxi.common.http.impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.util.ToastUtils;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;

/**
 * Created by Administrator on 2018/4/13 0013.
 */

public class LoginDialog extends Dialog {

    private static final String TAG = "LoginDialog";
    private static final int LOGIN_SUC = 1;
    private static final int SERVER_FAIL = 2;
    private static final int PW_ERR = 4;
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private IHttpClient mHttpClient;
    private MyHandler mMyHandler;

    static class MyHandler extends Handler{
        SoftReference<LoginDialog> dialogRef;
        public MyHandler(LoginDialog dialog){
            dialogRef = new SoftReference<LoginDialog>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoginDialog dialog = dialogRef.get();
            if (dialog == null){
                return;
            }
            //处理UI变化
            switch (msg.what){
                case LOGIN_SUC:
                    dialog.showLoginSuc();
                    break;
                case PW_ERR:
                    dialog.showPasswordError();
                    break;
                case SERVER_FAIL:
                    dialog.showServerError();
                    break;
            }

        }
    }


    public LoginDialog(@NonNull Context context,String phone) {
        this(context,R.style.Dialog);
        mPhoneStr = phone;
        mHttpClient = new OkHttpClientImpl();
        mMyHandler = new MyHandler(this);
    }

    public LoginDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_login_input,null);
        setContentView(root);
        initViews();
    }

    private void initViews() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.password);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = (TextView) findViewById(R.id.tips);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        mPhone.setText(mPhoneStr);
    }

    /**
     * 提交登录
     */
    private void submit() {

        //网络请求登录
        new Thread(){
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone",mPhoneStr);
                String password = mPw.getText().toString();
                request.setBody("password",password);

                IResponse response = mHttpClient.post(request,false);
                Log.d(TAG,response.getData());
                if (response.getCode() == BaseBizResponse.STATE_OK){
                    LoginResponse bizRes = new Gson().fromJson(response.getData(),LoginResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK){
                        //保存登录信息
                        Account account = bizRes.getData();
                        //todo: 加密存储

                        SharedPreferencesDao dao =
                                new SharedPreferencesDao(MyTaxiApplication.getInstance(),SharedPreferencesDao.FILE_ACCOUNT);
                        dao.save(SharedPreferencesDao.KEY_ACCOUNT,account);

                        //通知UI
                        mMyHandler.sendEmptyMessage(LOGIN_SUC);
                    }else if (bizRes.getCode() == BaseBizResponse.STATE_PW_ERR){
                        mMyHandler.sendEmptyMessage(PW_ERR);
                    }
                    else {
                        mMyHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                }else{
                    mMyHandler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();
    }

    /**
     * 显示／隐藏 loading
     * @param show
     */
    private void showOrHideLoading(boolean show) {
        if (show) {
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        } else {
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 处理登录成功 UI
     */
    public void showLoginSuc() {
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        ToastUtils.show(getContext(), getContext().getString(R.string.login_suc));
        dismiss();

    }

    /**
     *  显示服务器出错
     */
    private void showServerError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }


    /**
     * 密码错误
     */
    private void showPasswordError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.password_error));
    }

}
