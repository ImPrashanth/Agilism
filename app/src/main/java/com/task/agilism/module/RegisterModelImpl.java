package com.task.agilism.module;

import com.task.agilism.Utils;

/**
 * Created by Prashanth on 23/10/19.
 */

public class RegisterModelImpl implements RegisterModel {

    private AddTaskModelListener mAddTaskModelListener;

    private RegisterModelImpl(AddTaskModelListener addTaskModelListener) {
        mAddTaskModelListener = addTaskModelListener;
    }

    public static RegisterModel getInstance(AddTaskModelListener addTaskModelListener) {
        return new RegisterModelImpl(addTaskModelListener);
    }

    @Override
    public boolean checkValidName(String fname) {
        return Utils.isValidName(fname);
    }

    @Override
    public boolean checkValidEmail(String email) {
        return Utils.isValidMailId(email);
    }


    public interface AddTaskModelListener {

    }

}
