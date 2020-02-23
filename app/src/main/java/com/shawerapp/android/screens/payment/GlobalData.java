package com.shawerapp.android.screens.payment;

import androidx.collection.ArraySet;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;

public class GlobalData {

    public static String REGISTER_PHONE="REGISTER_PHONE";
    public static  String ARG_QUESTION_DESCRIPTION = "ARG_QUESTION_DESCRIPTION";
    public static Maybe<String> questionDescription;
    public static Maybe<String> mComposition;
    public static  CharSequence mCompositionchar;
    public  static List<String> attachmentFileUpload;
    public  static ArraySet<String> mSelectedFilesPaths ;



}
