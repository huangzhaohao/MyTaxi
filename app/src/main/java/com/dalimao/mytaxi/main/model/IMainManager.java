package com.dalimao.mytaxi.main.model;

import android.os.Handler;

/**
 * Created by Administrator on 2018/4/14 0014.
 */

public interface IMainManager {

    //登录成功
    static final int LOGIN_SUC = 5;
    //登录失败
    static final int TOKEN_INVALID = -6;
    //服务器错误
    static final int SERVER_FAIL = -999;

    void setHandler(Handler handler);

    void loginByToken();
}
