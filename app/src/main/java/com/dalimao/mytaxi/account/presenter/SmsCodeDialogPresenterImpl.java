package com.dalimao.mytaxi.account.presenter;

import com.dalimao.mytaxi.MyTaxiApplication;
import com.dalimao.mytaxi.R;
import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.view.ISmsCodeDialogView;
import com.dalimao.mytaxi.common.databus.RegisterBus;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;

/**
 * Created by Administrator on 2018/4/13 0013.
 */

public class SmsCodeDialogPresenterImpl implements ISmsCodeDialogPresenter {
    private ISmsCodeDialogView view;
    private IAccountManager accountManager;

//    private static class MyHandler extends Handler{
//        WeakReference<SmsCodeDialogPresenterImpl> refContext;
//
//        public MyHandler(SmsCodeDialogPresenterImpl context) {
//            this.refContext = new WeakReference<SmsCodeDialogPresenterImpl>(context);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            SmsCodeDialogPresenterImpl presenter = refContext.get();
//            switch (msg.what){
//                case IAccountManager.SMS_SEND_SUC:
//                    presenter.view.showCountDownTimer();
//                    break;
//                case IAccountManager.SMS_SEND_FAIL:
//                    presenter.view.showError(IAccountManager.SMS_SEND_FAIL, MyTaxiApplication.getInstance().getString(R.string.sms_send_fail));
//                    break;
//                case IAccountManager.SMS_CHECK_SUC:
//                    presenter.view.showSmsCodeCheckState(true);
//                    break;
//                case IAccountManager.SMS_CHECK_FAIL:
//                    presenter.view.showError(IAccountManager.SMS_SEND_FAIL,"");
//                    break;
//                case IAccountManager.USER_EXIST:
//                    presenter.view.showUserExist(true);
//                    break;
//                case IAccountManager.USER_NOT_EXIST:
//                    presenter.view.showUserExist(false);
//                    break;
//            }
//        }
//    }

    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
//        accountManager.setHandler(new MyHandler(this));
    }

    /**
     * 获取验证码
     * @param phone
     */
    @Override
    public void requestSendSmsCode(String phone) {
        accountManager.fetchSMSCode(phone);
    }

    @Override
    public void requestCheckSmsCode(String phone, String smsCode) {
        accountManager.checkSmsCode(phone,smsCode);
    }

    @Override
    public void requestCheckUserExist(String phone) {
        accountManager.checkUserExist(phone);
    }

    @RegisterBus
    public void onSendSmsCodeResponse(BaseBizResponse response){
        switch (response.getCode()){
            case IAccountManager.SMS_SEND_SUC:
                view.showCountDownTimer();
                break;
            case IAccountManager.SMS_SEND_FAIL:
                view.showError(IAccountManager.SMS_SEND_FAIL, MyTaxiApplication.getInstance().getString(R.string.sms_send_fail));
                break;
        }
    }

    @RegisterBus
    public void onCheckSmsCodeResponse(BaseBizResponse response){
        switch (response.getCode()){
            case IAccountManager.SMS_CHECK_SUC:
                view.showSmsCodeCheckState(true);
                break;
            case IAccountManager.SMS_CHECK_FAIL:
                view.showError(IAccountManager.SMS_SEND_FAIL,"");
                break;
        }
    }

    @RegisterBus
    public void onCheckUserExistResponse(BaseBizResponse response){
        switch (response.getCode()){
            case IAccountManager.USER_EXIST:
                view.showUserExist(true);
                break;
            case IAccountManager.USER_NOT_EXIST:
                view.showUserExist(false);
                break;
        }
    }
}
