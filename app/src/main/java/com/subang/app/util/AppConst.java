package com.subang.app.util;

/**
 * Created by Qiang on 2015/11/7.
 */
public interface AppConst {

    String APP_ID="wxe52147e45bfaa563";
    String LOG_TAG = "subang";

    //用于标识handler所发message的类型
    int WHAT_NETWORK_ERR = 0;
    int WHAT_SUCC_LOAD = -1;
    int WHAT_SUCC_SUBMIT = -2;
    int WHAT_INFO = -3;

    //用于标识CellnumActivity和PasswordActivity的类型
    int TYPE_CHANGE = 1;    //用于改变用户信息
    int TYPE_SIGNIN = 2;    //用于注册
}
