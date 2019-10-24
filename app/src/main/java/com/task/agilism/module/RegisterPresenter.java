package com.task.agilism.module;

/**
 * Created by Prashanth on 23/10/19.
 */

public interface RegisterPresenter {

    void onClickRegister();

    boolean isValidName(String fname);

    boolean isValidEmail(String email);
}
