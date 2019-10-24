package com.task.agilism.module;

/**
 * Created by Prashanth on 23/10/19.
 */

public class RegisterPresenterImpl implements RegisterPresenter, RegisterModelImpl.AddTaskModelListener {

    private RegisterView mAddTaskView;
    private RegisterModel mAddTaskModel;

    private RegisterPresenterImpl(RegisterView addTaskView) {
        mAddTaskView = addTaskView;
        mAddTaskModel = RegisterModelImpl.getInstance(this);
    }

    public static RegisterPresenter getInstance(RegisterView addTaskView) {
        return new RegisterPresenterImpl(addTaskView);
    }


    @Override
    public void onClickRegister() {

    }

    @Override
    public boolean isValidName(String fname) {
        return mAddTaskModel.checkValidName(fname);
    }

    @Override
    public boolean isValidEmail(String email) {
        return mAddTaskModel.checkValidEmail(email);
    }
}
