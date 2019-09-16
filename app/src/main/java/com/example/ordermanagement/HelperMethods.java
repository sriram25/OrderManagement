package com.example.ordermanagement;

import android.content.Context;
import android.widget.Toast;

class HelperMethods {

    // handles toast messages
    void handleToasts(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // validates fields
    Boolean validateEditText(String str) {
        return !str.isEmpty();
    }

}
