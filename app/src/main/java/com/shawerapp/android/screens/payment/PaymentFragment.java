package com.shawerapp.android.screens.payment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.google.common.hash.Hashing;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings;
import com.oppwa.mobile.connect.checkout.meta.CheckoutSkipCVVMode;
import com.oppwa.mobile.connect.exception.PaymentError;
import com.oppwa.mobile.connect.provider.Connect;
import com.oppwa.mobile.connect.provider.Transaction;
import com.oppwa.mobile.connect.provider.TransactionType;
import com.shawerapp.android.R;
import com.shawerapp.android.application.ApplicationModel;
import com.shawerapp.android.application.MyPreferenceManager;
import com.shawerapp.android.autovalue.Answer;
import com.shawerapp.android.autovalue.CommercialUser;
import com.shawerapp.android.autovalue.Field;
import com.shawerapp.android.autovalue.IndividualUser;
import com.shawerapp.android.autovalue.Invoice;
import com.shawerapp.android.autovalue.Invoice_;
import com.shawerapp.android.autovalue.LawyerUser;
import com.shawerapp.android.autovalue.SubSubject;
import com.shawerapp.android.backend.base.FileFramework;
import com.shawerapp.android.backend.base.RealTimeDataFramework;
import com.shawerapp.android.base.BaseFragment;
import com.shawerapp.android.base.FragmentLifecycle;
import com.shawerapp.android.custom.views.IndeterminateTransparentProgressDialog;
import com.shawerapp.android.screens.composer.ComposerViewModel;
import com.shawerapp.android.screens.container.ContainerActivity;
import com.shawerapp.android.screens.container.ContainerContract;
import com.shawerapp.android.screens.invoice.InvoiceKey;
import com.shawerapp.android.screens.requestlist.RequestListKey;
import com.shawerapp.android.utils.CommonUtils;
import com.shawerapp.android.utils.LoginUtil;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;
import com.trello.rxlifecycle2.android.FragmentEvent;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Maybe;
import io.reactivex.functions.Action;
import timber.log.Timber;

import static com.shawerapp.android.screens.composer.ComposerKey.QUESTION;


public class PaymentFragment extends BaseFragment implements PaymentContract.View,CheckoutIdRequestListener,PaymentStatusRequestListener {

    Boolean startPayment = false;
    private ProgressDialog progressDialog = null;

    String amount="50";
    String checkoutId;
    protected String resourcePath = null;
    String payment_method;

    private static final String STATE_RESOURCE_PATH = "STATE_RESOURCE_PATH";
    private PaymentStatusRequestListener listener = null;






    public static final String ARG_INVOICE = "invoice";

    public static final String ARG_REQUEST_TYPE = "requestType";

    public static final String ARG_SELECTED_FIELD = "selectedField";

    public static final String ARG_SELECTED_SUBSUBJECT = "selectedSubSubject";

    public static final String ARG_SELECTED_LAWYER = "selectedLawyer";

    public static final String ARG_QUESTION_DESCRIPTION = "questionDescription";

    public static final String ARG_AUDIO_FILE_UPLOAD = "audioFileUpload";

    public static final String ARG_ATTACHMENT_FILE_UPLOAD = "attachmentFileUpload";
    String finalType1;
    String lang="ar";


//    public static final String ARG_QUESTION_TO_RESPOND_TO = "audioFileUpload";


    public static final int REQUEST_ATTACH = 1;


    public static PaymentFragment newInstance() {
        Bundle args = new Bundle();
//        args.putParcelable(ARG_INVOICE, invoice);
        PaymentFragment fragment = new PaymentFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    public static PaymentFragment newInstance(int requestType, Field selectedField,
                                              SubSubject selectedSubSubject, LawyerUser selectedLawyerUser,
                                              String questionDescription, String audioFileUpload,
                                              List<String> attachmentFileUpload, File mRecordedAudioFile,
                                              CharSequence mComposition, ComposerViewModel mComposerViewModel) {

        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_TYPE, requestType);
        args.putParcelable(ARG_SELECTED_FIELD, selectedField);
        args.putParcelable(ARG_SELECTED_SUBSUBJECT, selectedSubSubject);
        args.putParcelable(ARG_SELECTED_LAWYER, selectedLawyerUser);
        args.putString(ARG_QUESTION_DESCRIPTION, questionDescription);
        args.putString(ARG_AUDIO_FILE_UPLOAD, audioFileUpload);
        args.putSerializable(ARG_ATTACHMENT_FILE_UPLOAD, (Serializable) attachmentFileUpload);
        args.putSerializable("mRecordedAudioFile", mRecordedAudioFile);
     // args.putString("mSelectedFilesPaths", mSelectedFilesPaths);
        args.putCharSequence("mComposition", mComposition);

        //args.putSerializable("mComposerViewModel", mComposerViewModel);

        
//        args.putParcelable(ARG_QUESTION_TO_RESPOND_TO, question);

        PaymentFragment fragment = new PaymentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PaymentContract.View mView;

    ComposerViewModel mComposerViewModel;

    @Inject
    ContainerContract.View mContainerContainerView;

    @Inject
    ContainerContract.ViewModel mContainerContainerViewModel;

    @Inject
    PaymentContract.ViewModel viewModel;

    @Inject
    PaymentContract.View mContainerView;

    @Inject
    PaymentViewModel mViewModel;

    @Inject
    LoginUtil mLoginUtil;

    @Inject
    RealTimeDataFramework mRTDataFramework;

    @Inject
    FileFramework mFileFramework;

    @BindView(R.id.ccBtn)
    Button ccBtn;

    @BindView(R.id.mBtn)
    Button mBtn;

    @BindView(R.id.creditCardBtn)
    RadioButton creditCardBtn;

    @BindView(R.id.madaBtn)
    RadioButton madaBtn;

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.submitBtn)
    Button submitBtn;

    @BindView(R.id.creditCardLayout)
    RelativeLayout creditCardLayout;

    @BindView(R.id.madaLayout)
    RelativeLayout madaLayout;

    @BindView(R.id.mCCreditCardNumber)
    EditText mCCreditCardNumber;

    @BindView(R.id.maCCreditCardNumber)
    EditText maCCreditCardNumber;

    @BindView(R.id.mCCreditCardExpireMonth)
    EditText mCCreditCardExpireMonth;

    @BindView(R.id.maCCreditCardExpireMonth)
    EditText maCCreditCardExpireMonth;

    @BindView(R.id.mCCreditCardExpireYear)
    EditText mCCreditCardExpireYear;

    @BindView(R.id.maCCreditCardExpireYear)
    EditText maCCreditCardExpireYear;

    @BindView(R.id.mCCreditCardCCV)
    EditText mCCreditCardCCV;

    @BindView(R.id.maCCreditCardCCV)
    EditText maCCreditCardCCV;

    @BindView(R.id.mCCreditCardHolderName)
    EditText mCCreditCardHolderName;

    @BindView(R.id.maCCreditCardHolderName)
    EditText maCCreditCardHolderName;

    @BindView(R.id.contentView)
    TextView contentView;

    Invoice invoice;
    String transActionID;
    String transActionDate;
    String hash;

    private long mPracticeRequestCost = 20;

    private IndeterminateTransparentProgressDialog mIndeterminateTransparentProgressDialog;

    @Override
    protected FragmentLifecycle.ViewModel getViewModel() {
        return viewModel;
    }

    private FirebaseFirestore db;


    @Override
    public void onAttach(Context context) {
        DaggerPaymentComponent.builder()
                .containerComponent(((ContainerActivity) context).getContainerComponent())
                .paymentModule(new PaymentModule(this, this))
                .build()
                .inject(this);

        super.onAttach(context);
    }

    @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void initBindings() {

        mContainerContainerView.hideRightToolbarButton();
        mContainerContainerViewModel.hideRightToolbarButton();

        subscribeErrorTextWatcher(mCCreditCardNumber);
        subscribeErrorTextWatcher(mCCreditCardExpireMonth);
        subscribeErrorTextWatcher(mCCreditCardExpireYear);
        subscribeErrorTextWatcher(mCCreditCardCCV);
        subscribeErrorTextWatcher(mCCreditCardHolderName);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creditCardLayout.setVisibility(View.GONE);
                madaLayout.setVisibility(View.VISIBLE);
                madaBtn.setChecked(true);
                creditCardBtn.setChecked(false);
            }
        });

        ccBtn.setOnClickListener(v -> {
            creditCardLayout.setVisibility(View.VISIBLE);
            madaLayout.setVisibility(View.GONE);
            creditCardBtn.setChecked(true);
            madaBtn.setChecked(false);
        });

//        creditCardBtn.setOnCheckedChangeListener((buttonView, isChecked) -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
////            if (isChecked) {
//                creditCardLayout.setVisibility(View.VISIBLE);
//                madaLayout.setVisibility(View.GONE);
////                creditCardBtn.setChecked(true);
////                madaBtn.setChecked(false);
////            } else {
////                creditCardLayout.setVisibility(View.INVISIBLE);
////                creditCardBtn.setChecked(false);
////            }
//
//            //callPayment();
//
//        }));
//
//
//        madaBtn.setOnCheckedChangeListener((buttonView, isChecked) -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
////            if (isChecked) {
//                creditCardLayout.setVisibility(View.GONE);
//                madaLayout.setVisibility(View.VISIBLE);
////                madaBtn.setChecked(true);
////                creditCardBtn.setChecked(false);
////            } else {
////                creditCardLayout.setVisibility(View.INVISIBLE);
////                creditCardBtn.setChecked(false);
////            }
//
//            //callPayment();
//
//        }));



        submitBtn.setOnClickListener(v -> {
            //requestCheckoutId(getString(R.string.checkout_ui_callback_scheme));

           /* if (creditCardLayout.getVisibility() == View.VISIBLE) {
                if (CommonUtils.isNotEmpty(mCCreditCardNumber.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    mCCreditCardNumber.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(mCCreditCardExpireMonth.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    mCCreditCardExpireMonth.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(mCCreditCardExpireYear.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    mCCreditCardExpireYear.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(mCCreditCardCCV.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    mCCreditCardCCV.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(mCCreditCardHolderName.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    mCCreditCardHolderName.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }
            } else {
                if (CommonUtils.isNotEmpty(maCCreditCardNumber.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    maCCreditCardNumber.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(maCCreditCardExpireMonth.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    maCCreditCardExpireMonth.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(maCCreditCardExpireYear.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    maCCreditCardExpireYear.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(maCCreditCardCCV.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    maCCreditCardCCV.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }

                if (CommonUtils.isNotEmpty(maCCreditCardHolderName.getText())) {
//                return mCCreditCardNumber.getText().toString();
                } else {
                    maCCreditCardHolderName.setBackgroundResource(R.drawable.input_field_bg_error);
                    return;
                }
            }*/

            callPayment();
        });

        mContainerContainerView.hideRightToolbarButton();
        mContainerContainerViewModel.hideRightToolbarButton();


        mContainerContainerView.clearToolbarSubtitle();
        mContainerContainerView.setToolbarTitle(getContext().getString(R.string.payment));
//        mContainerContainerView.setLeftToolbarButtonImageResource(-1);
        mContainerContainerView.setRightToolbarButtonImageResource(-1);


    }

    void addTransactionToShared() {
        MyPreferenceManager SP = MyPreferenceManager.getInstance(Objects.requireNonNull(getActivity()).getApplicationContext());
//                if(SP.contains("TRANSACTIONS")){
        String TRANSACTIONS = SP.getString("TRANSACTIONS", "");
        Date date = Calendar.getInstance().getTime();

        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);

        if (TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
            TRANSACTIONS = "" + transActionID + ",";
            SP.putString("TRANSACTIONS", TRANSACTIONS);

            transActionDate = strDate;

            SP.putString("TRANSACTIONSDATES", strDate + ",");
        } else if (!TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
            TRANSACTIONS = "," + transActionID;
            SP.putString("TRANSACTIONS", TRANSACTIONS);
            transActionDate = strDate;

            SP.putString("TRANSACTIONSDATES", "," + strDate);
        } else {
            TRANSACTIONS = transActionID;
            SP.putString("TRANSACTIONS", TRANSACTIONS);
            transActionDate = strDate;

            SP.putString("TRANSACTIONSDATES", strDate);
        }
    }

    private void checkTransActions() {
        MyPreferenceManager SP = MyPreferenceManager.getInstance(Objects.requireNonNull(getActivity()).getApplicationContext());
        if (SP.contains("TRANSACTIONS")) {
            String TRANSACTIONS = SP.getString("TRANSACTIONS", "");
            String TRANSACTIONSDATES = SP.getString("TRANSACTIONSDATES", "");
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

            Date currentDate = Calendar.getInstance().getTime();
            Calendar today = Calendar.getInstance ();
            today.add(Calendar.DAY_OF_YEAR, 0);
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, -6 );
            today.set(Calendar.SECOND, 0);


            String[] TRANSACTIONSArry = TRANSACTIONS.split(",");
            String[] TRANSACTIONSDATESArry = TRANSACTIONSDATES.split(",");
            for (int i = 0; i < TRANSACTIONSArry.length - 1; i++) {
                if (TRANSACTIONSArry[i].equals(transActionID)) {

                    String transActions = SP.getString("TRANSACTIONS", "");
                    String transActionsDates = SP.getString("TRANSACTIONSDATES", "");

                    String ss = removeWords(transActions, transActionID);
                    String sss = removeWords(transActionsDates, transActionDate);

                    SP.putString("TRANSACTIONS", ss);
                    SP.putString("TRANSACTIONSDATES", sss);
                }
            }


        }

    }

    public static String removeWords(String word ,String remove) {
        return word.replace(remove,"");
    }


    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    void callPayment() {

        transActionID = "" + System.currentTimeMillis();

        String cardNumber = "";
        String cardExpireMonth = "";
        String cardExpireYear = "";
        String cardCCV = "";
        String holderName = "";

        if (creditCardLayout.getVisibility() == View.VISIBLE) {
            cardNumber = mCCreditCardNumber.getText().toString();
            cardExpireMonth = mCCreditCardExpireMonth.getText().toString();
            cardExpireYear = mCCreditCardExpireYear.getText().toString();
            cardCCV = mCCreditCardCCV.getText().toString();
            holderName = mCCreditCardHolderName.getText().toString();
        } else {
            cardNumber = maCCreditCardNumber.getText().toString();
            cardExpireMonth = maCCreditCardExpireMonth.getText().toString();
            cardExpireYear = maCCreditCardExpireYear.getText().toString();
            cardCCV = maCCreditCardCCV.getText().toString();
            holderName = maCCreditCardHolderName.getText().toString();
        }


        long questionServiceFee;
        if (mViewModel.mRequestType == QUESTION) {
            if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
                questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
            } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
                questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedSubSubject.uid());
            } else {
                questionServiceFee = 0L;
            }
        } else {
            questionServiceFee = 20L;
        }

        String type = "";
        if (mViewModel.mRequestType == QUESTION) {
            type = "Esteshara Fee";
        } else {
            type = "Coordinate fees with lawyer office";
        }


         amount = "" + (questionServiceFee * 100);

        String post = "MTg2NWEzYjViNGI1NzNmZDMzODNmODY0";
        post += amount;
        post += "682";
        post += "328";
        post += "en";
        post += "0100000205";
        post += "1";
        post += "1";
        post += "https://testurl.view.murabba.com/api/get_response";
        post += transActionID;
        post += "2.0";
        getHash(post);

        String bodyData = "MTg2NWEzYjViNGI1NzNmZDMzODNmODY0&Amount=" + amount +
                "&CurrencyISOCode=682&ItemID=328&Language=en&MerchantID=0100000205&MessageID=1&PaymentMethod=1&ResponseBackURL=https://testurl.view.murabba.com/api/get_response&TransactionID=" +
                transActionID +
                "&Version=2.0&CardNumber=" + cardNumber + "&ExpiryDateYear=" + cardExpireYear + "&ExpiryDateMonth=" + cardExpireMonth +
                "&SecurityCode=" + cardCCV + "&CardHolderName=" + holderName + "&SecureHash=" + hash;

        try {
            String query = URLEncoder.encode(bodyData, "utf-8");
            String url = "https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRPayMsgHandler";
            final String[] url2 = {""};
            webView.postUrl(
                    url,
                    EncodingUtils.getBytes(bodyData, "utf-8"));
            showLoadingIndicator();

            startPayment = true;

            addTransactionToShared();

            String finalType2 = type;
            class MyJavaScriptInterface {
                private TextView contentView;

                public MyJavaScriptInterface(TextView aContentView) {
                    contentView = aContentView;
                }

                @SuppressWarnings("unused")
                public void processContent(String aContent) {
                    final String content = aContent;
                    contentView.post(() -> {
                        contentView.setText(content);
//                        if (content.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
////                            Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
//                            createQuestionPractise();
//                        } else if (content.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
////                            Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
////                            addNotPaidInvoice(finalType2);
//                            hideLoadingIndicator();
//                        } else if (content.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
//                                content.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
////                            Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
////                            addNotPaidInvoice(finalType2);
//                            hideLoadingIndicator();
//                        } else if (content.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
//                                content.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
////                            Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
//                            hideLoadingIndicator();
//                        } else if (content.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
//                                content.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
////                            Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
//                            hideLoadingIndicator();
//                        } else if (content.contains("Duplicate transaction ID")) {
////                            Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
//                            hideLoadingIndicator();
//                        }
//                        else {
//                            Toast.makeText(getContext(), getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
////                            addNotPaidInvoice(finalType2);
////                            hideLoadingIndicator();
//                            Exception e = new Exception();
//                            Crashlytics.logException(e);
//                        }
                    });
                }
            }
            webView.getSettings().setJavaScriptEnabled(true);
            webView.addJavascriptInterface(new MyJavaScriptInterface(contentView), "INTERFACE");
            String finalType1 = type;
            webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    // do your stuff here
                    if (webView.getVisibility() == View.VISIBLE) {
                        webView.setVisibility(View.GONE);
                    }
                    url2[0] = url;
                    if (url.equals("https://testurl.view.murabba.com/api/get_response")) {
                        view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                        webView.evaluateJavascript(
                                "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                html -> {
                                    if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                        createQuestionPractise();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                        addNotPaidInvoice(finalType1);
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                             html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                        addNotPaidInvoice(finalType1);
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                            html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                            html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    } else if (html.contains("Duplicate transaction ID")) {
                                        Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                                        addNotPaidInvoice(finalType1);
                                        hideLoadingIndicator();
                                        checkTransActions();
                                        Exception e = new Exception();
                                        Crashlytics.logException(e);
                                    }

                                });
                    } else if (url.contains("https://pit.3dsecure.net/")) {
                        hideLoadingIndicator();
                        webView.setVisibility(View.VISIBLE);
                    } else {
                        view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
                        webView.evaluateJavascript(
                                "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                html -> {
                                    if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                        createQuestionPractise();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                        addNotPaidInvoice(finalType1);
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                            html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                        addNotPaidInvoice(finalType1);
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                            html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                            html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                        Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    } else if (html.contains("Duplicate transaction ID")) {
                                        Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                        checkTransActions();
                                        hideLoadingIndicator();
                                    }

                                });
                    }

                }
            });

            String finalType = type;
            webView.evaluateJavascript(
                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                    html -> {
                        if (url2[0].equals("https://testurl.view.murabba.com/api/get_response")) {
                            if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                createQuestionPractise();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                addNotPaidInvoice(finalType);
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                    html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                addNotPaidInvoice(finalType);
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                    html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                checkTransActions();
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                    html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                checkTransActions();
                                hideLoadingIndicator();
                            } else if (html.contains("Duplicate transaction ID")) {
                                Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                checkTransActions();
                                hideLoadingIndicator();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                                addNotPaidInvoice(finalType);
                                hideLoadingIndicator();
                                checkTransActions();
                                Exception e = new Exception();
                                Crashlytics.logException(e);
                            }
                        }  else if (url2[0].contains("https://pit.3dsecure.net/")) {
                            hideLoadingIndicator();
                            webView.setVisibility(View.VISIBLE);
                        } else {
                            if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                createQuestionPractise();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                addNotPaidInvoice(finalType);
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                    html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                addNotPaidInvoice(finalType);
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                    html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                checkTransActions();
                                Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                hideLoadingIndicator();
                            } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                    html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                checkTransActions();
                                hideLoadingIndicator();
                            } else if (html.contains("Duplicate transaction ID")) {
                                Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                checkTransActions();
                                hideLoadingIndicator();
                            }
                        }

                        webView.evaluateJavascript(
                                "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                html1 -> {
                                    if (url2[0].equals("https://testurl.view.murabba.com/api/get_response")) {
                                        if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                            createQuestionPractise();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                            addNotPaidInvoice(finalType);
                                            hideLoadingIndicator();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                                html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                            addNotPaidInvoice(finalType);
                                            hideLoadingIndicator();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                                html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                            checkTransActions();
                                            hideLoadingIndicator();
                                        } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                                html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                            hideLoadingIndicator();
                                            checkTransActions();
                                        } else if (html.contains("Duplicate transaction ID")) {
                                            Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                            hideLoadingIndicator();
                                            checkTransActions();
                                        } else {
                                            Toast.makeText(getContext(), getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                                            addNotPaidInvoice(finalType);
                                            hideLoadingIndicator();
                                            checkTransActions();
                                            Exception e = new Exception();
                                            Crashlytics.logException(e);
                                        }
                                    }  else if (url2[0].contains("https://pit.3dsecure.net/")) {
                                        hideLoadingIndicator();
                                        webView.setVisibility(View.VISIBLE);
                                    } else {
                                        if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
                                            createQuestionPractise();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                            addNotPaidInvoice(finalType);
                                            hideLoadingIndicator();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
                                                html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
                                            addNotPaidInvoice(finalType);
                                            hideLoadingIndicator();
                                        } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
                                                html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
                                            hideLoadingIndicator();
                                            checkTransActions();
                                        } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
                                                html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
                                            Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
                                            hideLoadingIndicator();
                                            checkTransActions();
                                        } else if (html.contains("Duplicate transaction ID")) {
                                            Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
                                            hideLoadingIndicator();
                                            checkTransActions();
                                        }
                                    }
//                                    if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"0000\\\"")) {
//                                        Toast.makeText(getContext(), getString(R.string.Paymentprocessedsuccessfully), Toast.LENGTH_LONG).show();
//                                        createQuestionPractise();
//                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"3000\\\"")) {
//                                        Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
//                                        addNotPaidInvoice(finalType);
//                                        hideLoadingIndicator();
//                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"10001\\\"") ||
//                                            html.contains("\\\"Response_StatusCode\\\":\\\"10001\\\"")) {
//                                        Toast.makeText(getContext(), getString(R.string.Paymentfailed), Toast.LENGTH_LONG).show();
//                                        addNotPaidInvoice(finalType);
//                                        hideLoadingIndicator();
//
//                                    } else if (html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00040\\\"") ||
//                                            html.contains("\\\"Response_StatusCode\\\":\\\"00040\\\"")) {
//                                        Toast.makeText(getContext(), getString(R.string.InvalidSecurityCode), Toast.LENGTH_LONG).show();
//                                        hideLoadingIndicator();
//                                    } else if (html.contains("\\\"Response_StatusCode\\\":\\\"00038\\\"") ||
//                                            html.contains("\\\"Response_GatewayStatusCode\\\":\\\"00038\\\"")) {
//                                        Toast.makeText(getContext(), getString(R.string.InvalidCardNumber), Toast.LENGTH_LONG).show();
//                                        hideLoadingIndicator();
//                                    } else if (html.contains("Duplicate transaction ID")) {
//                                        Toast.makeText(getContext(), getString(R.string.DuplicatetransactionID), Toast.LENGTH_LONG).show();
//                                        hideLoadingIndicator();
//                                    } else {
//                                        Toast.makeText(getContext(), getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
//                                        addNotPaidInvoice(finalType);
//                                        hideLoadingIndicator();
//                                        Exception e = new Exception();
//                                        Crashlytics.logException(e);
//                                    }
                                });
                    });


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    void addInvoice(String type) {
        long questionServiceFee;
        if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
            questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
        } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
            questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedSubSubject.uid());
        } else {
            questionServiceFee = 0L;
        }

        CollectionReference dbInvoices = db.collection("invoices");

        Invoice_ invoice_ = new Invoice_("0", "0.0",
                String.valueOf(questionServiceFee), type,
                String.valueOf(questionServiceFee), transActionID, "invoices",
                mLoginUtil.getUserID(), mViewModel.mSelectedLawyer.uid(), new Date());

        dbInvoices.add(invoice_)
                .addOnSuccessListener(documentReference -> {
                    Log.i(" documentReference", " Added" + documentReference.toString());
                })
                .addOnFailureListener(e -> {
                    Log.i(" e", e.getLocalizedMessage());
                });

    }

    void createQuestionPractise() {
        checkTransActions();
        mViewModel.mComposerViewModel.setPaidStatus(true, mContainerView, this, viewModel, transActionID);
        mViewModel.mComposerViewModel.onSubmitComposition();
        db = FirebaseFirestore.getInstance();
        String type = "";
        if (mViewModel.mRequestType == QUESTION) {
            type = "Esteshara Fee";
        } else {
            type = "Coordinate fees with lawyer office";
        }


//        addInvoice(type);

    }

    @SuppressLint("CheckResult")
    public void addNotPaidInvoice(String type) {
        checkTransActions();
        long questionServiceFee;
        if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
            questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
        } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
            questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedSubSubject.uid());
        } else {
            questionServiceFee = 0L;
        }

        Invoice invoice_ = Invoice.builder()
                .UserUid(mLoginUtil.getUserID())
                .collection("invoices")
                .LawyerUid(mViewModel.mSelectedLawyer.uid())
                .orderDate(new Date())
                .orderRequestNumber(transActionID)
                .orderSubTotal(String.valueOf(questionServiceFee))
                .orderType(type)
                .orderTypePrice(String.valueOf(questionServiceFee))
                .orderVat("0.0%")
                .orderVatPrice("0")
                .paid("false")
                .build();

        mContainerView.showSuccessDialog(
                this.getString(R.string.PaymentprocessednotSuccessfully),
                () -> mContainerContainerViewModel
                        .newTop(InvoiceKey.builder()
                                .invoice(invoice_)
                                .build())
                        .doFinally(this::hideLoadingIndicator)
                        .subscribe(mContainerContainerViewModel.navigationObserver()));


        mRTDataFramework.addInvoice(invoice_)
                .compose(this.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .doFinally(this::hideLoadingIndicator)
                .subscribe();

        ;
    }

    @SuppressLint("CheckResult")
    public void subscribeErrorTextWatcher(EditText editText) {
        RxTextView.afterTextChangeEvents(editText)
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(textChangeEvent -> {
                    if (CommonUtils.isNotEmpty(textChangeEvent.editable())) {
                        editText.setBackgroundResource(R.drawable.input_field_bg);
                    }
                });
    }

    @Override
    public void addItem(Invoice invoice) {

    }





    @Override
    public void showSuccessDialog(String successMessage, Action onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        builder.setMessage(successMessage);
        builder.setPositiveButton(R.string.ok_caps, (dialog, which) -> {
            dialog.dismiss();
            try {
                onConfirm.run();
            } catch (Exception e) {
                Timber.e(CommonUtils.getExceptionString(e));
            }
        });
        builder.show();
    }

    @Override
    public void showMessage(String message, boolean isError) {
        if (isError) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
            builder.setTitle(R.string.title_error);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.ok_caps, (dialog, which) -> dialog.dismiss());
            builder.show();
        } else {
            showMessage(message);
        }
    }

    public void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok_caps, (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public BaseFragment getCurrentFragmentInFrame() {
        return (BaseFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.container);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sts_payment_activity, container, false);

        mUnbinder = ButterKnife.bind(this, view);
        transActionID = "" + System.currentTimeMillis();

        if (savedInstanceState != null) {
            resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH);
        }
        requestCheckoutId(getString(R.string.checkout_ui_callback_scheme));


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        invoice = getArguments().getParcelable(ARG_INVOICE);

    }



    protected void showProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        progressDialog.setMessage(getString(R.string.progress_message_payment_status));
        progressDialog.show();
    }


    public void requestPaymentStatus(String resourcePath,
                                     final String lang, PaymentStatusRequestListener listener) {
        showProgressDialog();
        this.listener = listener;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, AppConstants.requestPaymentStatus + checkoutId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {




                try {
                    Log.e("RequestPaymentStatus", "RequestPaymentStatus " + response);

                    hideProgressDialog();
                    JSONObject r = new JSONObject(response);
                    Log.e("RequestPaymentStatus", "RequestPaymentStatus " + r);

                    JSONObject result = r.getJSONObject("result");
                    String description = result.getString("description");
                    Toast.makeText(getActivity(), "" + description, Toast.LENGTH_SHORT).show();
                    String code = result.getString("code");
                    if (code.matches("000.000.000") || code.matches("000.000.100") ||

                            code.matches("000.100.110") || code.matches("000.100.111") ||

                            code.matches("000.100.112") || code.matches("000.400.000") ||

                            code.matches("000.400.020") || code.matches("000.400.010") || code.matches("000.400.100") || code.matches("000.400.020")) {
                        createQuestionPractise();

                        //showDialog(PaymentActivity.this, getString(R.string.SuccessPayed),true);
                        // Toast.makeText(PaymentActivity.this, "" + getString(R.string.SuccessPayed), Toast.LENGTH_SHORT).show();



                        /*
                        (String uid,String orderVatPrice, String orderVat,
                      String orderTypePrice, String orderType, String orderSubTotal,
                      String orderRequestNumber,Date orderDate, String collection, String userUid,
                      String lawyerUid, String paid)
                         */

                        addTransactionToShared();


                    } else {

                        addNotPaidInvoice(finalType1);


                        Toast.makeText(getActivity(), "" + getString(R.string.failedpay), Toast.LENGTH_SHORT).show();
//addInvoice();
                        //showFailedDialog(PaymentActivity.this, getString(R.string.failedpay));




                       /* if (counter == 3) {

                            Toast.makeText(PaymentActivity.this, "" + getString(R.string.failedpay), Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PaymentActivity.this,ContainerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK
                       |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                              sharedPreferences.edit().remove("counter").commit();


                        } else {
                             Toast.makeText(PaymentActivity.this, "" + description, Toast.LENGTH_SHORT).show();
                            Toast.makeText(PaymentActivity.this, "" + getString(R.string.failedpayagain), Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(PaymentActivity.this,ContainerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK
                                    |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }*/


                    }
                    Log.e("description", "description " + description);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("KHH", "JSONException " + e);

                    hideProgressDialog();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Log.d("KHH", "VolleyError " + error);


            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap();
                map.put("id", checkoutId + "");
                map.put("type", "paymentStatus");


                return map;

            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("lang", lang);


                return headers;
            }

        };

        ApplicationModel.getInstance().addToRequestQueue(stringRequest);


    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onActivityResult(requestCode, resultCode, data);
    }*/



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        viewModel.onActivityResult(requestCode, resultCode, data);

        // Toast.makeText(this, "result", Toast.LENGTH_SHORT).show();


      /*  long questionServiceFee;
        if (mViewModel.mRequestType == QUESTION) {
            if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
                questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
            } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
                questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedSubSubject.uid());
            } else {
                questionServiceFee = 0L;
            }
        } else {
            questionServiceFee = 20L;
        }

        String type = "";
        if (mViewModel.mRequestType == QUESTION) {
            type = "Esteshara Fee";
        } else {
            type = "Coordinate fees with lawyer office";
        }

        String amount = "" + (questionServiceFee * 100);
        addTransactionToShared();
        createQuestionPractise();*/
        /* Override onActivityResult to get notified when the checkout process is done. */


        if (requestCode == CheckoutActivity.REQUEST_CODE_CHECKOUT)
            if (resultCode == CheckoutActivity.RESULT_OK) {
                long questionServiceFee;
                if (mViewModel.mRequestType == QUESTION) {
                    if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
                        questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
                    } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
                        questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedSubSubject.uid());
                    } else {
                        questionServiceFee = 0L;
                    }
                } else {
                    questionServiceFee = 20L;
                }

                String type = "";
                if (mViewModel.mRequestType == QUESTION) {
                    type = "Esteshara Fee";
                } else {
                    type = "Coordinate fees with lawyer office";
                }

                 finalType1 = type;



                amount = "" + (questionServiceFee * 100);

                Log.e("resourcePath", "resourcePath");
                /* Transaction completed. */
                Transaction transaction = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_TRANSACTION);
                resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH);
                payment_method = transaction.getPaymentParams().getPaymentBrand();
                Log.e("payment_method", "payment_method" + payment_method);
                assert resourcePath != null;
                Log.e("resourcePath", resourcePath);
                Log.e("transaction", transaction.getTransactionType().toString());
                /* Check the transaction type. */
                assert (transaction != null);
                if (transaction.getTransactionType() == TransactionType.ASYNC) {
                    /* Check the status of synchronous transaction. */


                    requestPaymentStatus(resourcePath, lang, this);
                    hideProgressDialog();


                }

                /* else {
                    /* Asynchronous transaction is processed in the onNewIntent(). */
                //onNewIntent(data);
                //requestPaymentStatus(resourcePath,lang,token, this);

                    /*hideProgressDialog();
                }*/
            } else if (resultCode == CheckoutActivity.RESULT_CANCELED) {
                hideProgressDialog();
                getActivity().finish();
//                openMain();
            } else if (resultCode == CheckoutActivity.RESULT_ERROR) {
                hideProgressDialog();
                PaymentError error = data.getParcelableExtra(CheckoutActivity.CHECKOUT_RESULT_ERROR);
                assert error != null;
                Log.e("resourcePath", error.getErrorMessage());
                showAlertDialog(R.string.error_message);
            }
    }


    @Override
    public boolean onItemClick(View view, int position) {

        return false;
    }


    public void showLoadingIndicator() {
        if (mIndeterminateTransparentProgressDialog == null ||
                !mIndeterminateTransparentProgressDialog.isShowing()) {
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> mIndeterminateTransparentProgressDialog = IndeterminateTransparentProgressDialog.show(getContext(), true, false));
        }
    }

    public void hideLoadingIndicator() {
        if (mIndeterminateTransparentProgressDialog != null || mIndeterminateTransparentProgressDialog.isShowing()) {
            mIndeterminateTransparentProgressDialog.dismiss();
        }
    }

    void getHash(String str) {
        hash = Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
    }


    @SuppressLint("CheckResult")
    public void addNewQuestion(Maybe<String> audioFileUpload, Maybe<List<String>> attachmentFileUpload, Maybe<String> questionDescription) {
        mViewModel.onSubmitComposition(mViewModel.mRequestType, mViewModel.mRecordedAudioFile, mViewModel.mSelectedFilesPaths, mViewModel.mComposition);
//        long questionServiceFee;
//        if (mLoginUtil.getUserRole().equals(IndividualUser.ROLE_VALUE)) {
//            questionServiceFee = mViewModel.mSelectedLawyer.individualFees().get(mViewModel.mSelectedSubSubject.uid());
//        } else if (mLoginUtil.getUserRole().equals(CommercialUser.ROLE_VALUE)) {
//            questionServiceFee = mViewModel.mSelectedLawyer.commercialFees().get(mViewModel.mSelectedField.uid());
//        } else {
//            questionServiceFee = 0L;
//        }
//
//
////        mRTDataFramework.addQuestion(mViewModel.mSelectedField, mViewModel.mSelectedSubSubject, mViewModel.mSelectedLawyer, answers, transActionID);
//
//        mRTDataFramework
//                .retrieveUserCoins()
//                .doOnSubscribe(disposable -> showLoadingIndicator())
//                .flatMap(Maybe::just)
//                .flatMap(currentCoins -> Maybe.zip(audioFileUpload, attachmentFileUpload, questionDescription,
//                        (recordedAudioUrl, attachmentUrls, description) -> Answer.builder()
//                                .audioRecordingUrl(recordedAudioUrl)
//                                .fileAttachments(attachmentUrls)
//                                .questionDescription(description)
//                                .build()))
//                .flatMapCompletable(answer -> mRTDataFramework
//                        .addQuestion(
//                                mViewModel.mSelectedField,
//                                mViewModel.mSelectedSubSubject,
//                                mViewModel.mSelectedLawyer,
//                                answer,
//                                "" + System.currentTimeMillis())
//                        .compose(bindUntilEvent(FragmentEvent.DESTROY)))
//                .doFinally(this::hideLoadingIndicator)
//                .subscribe(
//                        () -> mView.showSuccessDialog(
//                                getString(R.string.success_new_question, questionServiceFee),
//                                () -> mViewModel
//                                        .newTop(AnswerListKey.create())
//                                        .subscribe(mViewModel.navigationObserver())),
//                        throwable -> {
//                            Log.i(" throwable", "throwable: " + throwable.getLocalizedMessage());
//                            if (throwable.getMessage().equalsIgnoreCase(getString(R.string.error_not_enough_coins))) {
////                                showNotEnoughCoinsPopup();
//                            } else {
//
////                                mContainerViewModel.catchErrorThrowable().accept(throwable);
//                            }
//                        });

//        mRTDataFramework
//                .retrieveUserCoins()
//                .flatMap(Maybe::just)
//                .flatMap(currentCoins ->
//                        Maybe.zip(audioFileUpload, attachmentFileUpload, questionDescription,
//                        (recordedAudioUrl, attachmentUrls, description) -> Answer.builder()
//                                .audioRecordingUrl(recordedAudioUrl)
//                                .fileAttachments(attachmentUrls)
//                                .questionDescription(description)
//                                .build()))
//                .flatMapCompletable(answer -> mRTDataFramework
//                        .addQuestion(
//                                mViewModel.mSelectedField,
//                                mViewModel.mSelectedSubSubject,
//                                mViewModel.mSelectedLawyer,
//                                answer,
//                                transActionID)
//                        .compose(bindUntilEvent(FragmentEvent.DESTROY)))
//                .doFinally(this::hideLoadingIndicator)
//                .subscribe(
//                        () -> mView.showSuccessDialog(
//                                getString(R.string.success_new_question, questionServiceFee),
//                                () -> mViewModel
//                                        .newTop(AnswerListKey.create())
//                                        .subscribe(mViewModel.navigationObserver())),
//                        throwable -> {
//                            Log.i(" throwable", "throwable: " + throwable.getLocalizedMessage());
//                            if (throwable.getMessage().equalsIgnoreCase(getString(R.string.error_not_enough_coins))) {
////                                showNotEnoughCoinsPopup();
//                            } else {
////                                mContainerViewModel.catchErrorThrowable().accept(throwable);
//                            }
//                        });
    }

    public void addNewPracticeRequest(Maybe<String> audioFileUpload, Maybe<List<String>> attachmentFileUpload, Maybe<String> questionDescription) {
        mRTDataFramework
                .retrieveUserCoins()
                .flatMap(currentCoins -> {
                    return Maybe.just(currentCoins);
                })
                .flatMap(currentCoins -> Maybe.zip(audioFileUpload, attachmentFileUpload, questionDescription,
                        (recordedAudioUrl, attachmentUrls, description) -> Answer.builder()
                                .audioRecordingUrl(recordedAudioUrl)
                                .fileAttachments(attachmentUrls)
                                .questionDescription(description)
                                .build()))
                .flatMapCompletable(answer -> mRTDataFramework
                        .addPracticeRequest(
                                mViewModel.mSelectedField,
                                mViewModel.mSelectedSubSubject,
                                answer.audioRecordingUrl(),
                                answer.fileAttachments(),
                                answer.questionDescription(),
                                mPracticeRequestCost)
                        .compose(bindUntilEvent(FragmentEvent.DESTROY)))
                .doFinally(this::hideLoadingIndicator)
                .subscribe(
                        () -> mView.showSuccessDialog(
                                getString(R.string.success_new_practice_request, mPracticeRequestCost),
                                () -> mViewModel
                                        .newTop(RequestListKey.create())
                                        .subscribe(mViewModel.navigationObserver())),
                        throwable -> {
                            if (throwable.getMessage().equalsIgnoreCase(getString(R.string.error_not_enough_coins))) {
//                                showNotEnoughCoinsPopup();
                            } else {
                                mViewModel.catchErrorThrowable().accept(throwable);
                            }
                        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mContainerContainerView.hideRightToolbarButton();
        mContainerContainerView.hideRightText_();
        mContainerContainerViewModel.hideRightToolbarButton();

        mContainerContainerViewModel.hideRightToolbarButton();
        mContainerContainerViewModel.hideRightToolbarButton();
        mContainerContainerViewModel.hideRightToolbarButton();
        mContainerContainerView.hideRightToolbarButton();

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        Objects.requireNonNull(getActivity()).registerReceiver(networkChangeReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
//        Objects.requireNonNull(getActivity()).unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onCheckoutIdReceived(String checkoutId, String url) {
        this.checkoutId = checkoutId;
        Log.e("checkoutId", checkoutId);
        Log.e("callBack", url);
        hideProgressDialog();
        openCheckoutUI(checkoutId);

    }

    private void openCheckoutUI(String checkoutId) {
        CheckoutSettings checkoutSettings = createCheckoutSettings(checkoutId,
                getString(R.string.checkout_ui_callback_scheme));

        /* Set componentName if you want to receive callbacks from the checkout */
        ComponentName componentName = new ComponentName(
                getActivity().getPackageName(), CheckoutBroadcastReceiver.class.getName());

        /* Set up the Intent and start the checkout activity. */
        Intent intent = checkoutSettings.createCheckoutActivityIntent(getActivity(), componentName);

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT);
    }


    protected CheckoutSettings createCheckoutSettings(String checkoutId, String callbackScheme) {


        return new CheckoutSettings(checkoutId, Constants.Config.PAYMENT_BRANDS,
                Connect.ProviderMode.TEST).setSkipCVVMode(CheckoutSkipCVVMode.FOR_STORED_CARDS)
//                .setShopperResultUrl(callbackScheme);
                .setShopperResultUrl("checkoutui://result");
        //                .setGooglePayPaymentDataRequest(getGooglePayRequest())
    }

    protected void hideProgressDialog() {
        if (progressDialog == null) {
            return;
        }

        progressDialog.dismiss();
    }

    protected void showAlertDialog(String message) {
        new AlertDialog.Builder(getContext()).
                setMessage(message).setPositiveButton(R.string.button_ok, (dialog, which) -> {
//            openMain();
        })
                .setCancelable(false).show();
    }

    protected void showAlertDialog(int messageId) {
        showAlertDialog(getString(messageId));
    }

    @Override
    public void onErrorOccurred() {
        hideProgressDialog();
        showAlertDialog(R.string.error_message);

    }

    @Override
    public void onPaymentStatusReceived(String paymentStatus) {
        hideProgressDialog();

        if ("true".equals(paymentStatus)) {
            showAlertDialog(R.string.message_successful_payment);
            return;
        }


        showAlertDialog(R.string.message_unsuccessful_payment);
    }



    public void requestCheckoutId(String callbackScheme) {

        //CheckoutIdRequestAsyncTask


        Log.e("checkout", "chechout");
        new CheckoutIdRequestAsyncTask(amount, "ar", transActionID, this);

    }

    protected boolean hasCallbackScheme(Intent intent) {
        String scheme = intent.getScheme();

        return getString(R.string.checkout_ui_callback_scheme).equals(scheme) ||
                getString(R.string.payment_button_callback_scheme).equals(scheme) ||
                getString(R.string.custom_ui_callback_scheme).equals(scheme);
    }


//    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            Log.d("app","Network connectivity change");
//
//            Log.d("app","Network connectivity change");
//            Log.d("app", "Network connectivity change" + intent.getFlags());
//
//            if (startPayment) {
//                //Save Transaction id and date
//                hideLoadingIndicator();
//
//                MyPreferenceManager SP = MyPreferenceManager.getInstance(Objects.requireNonNull(getActivity()).getApplicationContext());
////                if(SP.contains("TRANSACTIONS")){
//                String TRANSACTIONS = SP.getString("TRANSACTIONS", "");
//                Date date = Calendar.getInstance().getTime();
//                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
//                String strDate = dateFormat.format(date);
//
//                if (TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
//                    TRANSACTIONS = "" + transActionID + ",";
//                    SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                    SP.putString("TRANSACTIONSDATES", strDate + ",");
//                } else if (!TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
//                    TRANSACTIONS = "," + transActionID;
//                    SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                    SP.putString("TRANSACTIONSDATES", "," + strDate);
//                } else {
//                    TRANSACTIONS = transActionID;
//                    SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                    SP.putString("TRANSACTIONSDATES", strDate);
//                }
//
//                Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.internetDisconnected), Toast.LENGTH_LONG).show();
////                }
//
//            }
//
//        }
//    };

//    @Override
//    public void onInternetConnectivityChanged(boolean isConnected) {
//        if (isConnected) {
//
//        }
//        else {
//            Log.d("app","Network disconnected");
//            if (startPayment) {
//                //Save Transaction id and date
//
//                MyPreferenceManager SP = MyPreferenceManager.getInstance(Objects.requireNonNull(getActivity()).getApplicationContext());
//                if(SP.contains("TRANSACTIONS")){
//                    String TRANSACTIONS = SP.getString("TRANSACTIONS", "");
//                    Date date = Calendar.getInstance().getTime();
//                    @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
//                    String strDate = dateFormat.format(date);
//
//                    if (TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
//                        TRANSACTIONS = "" + transActionID + ",";
//                        SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                        SP.putString("TRANSACTIONSDATES", strDate + ",");
//                    } else if (!TRANSACTIONS.contains(",") && TRANSACTIONS.length() > 0) {
//                        TRANSACTIONS = "," + transActionID;
//                        SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                        SP.putString("TRANSACTIONSDATES", "," + strDate);
//                    } else {
//                        TRANSACTIONS = transActionID;
//                        SP.putString("TRANSACTIONS", TRANSACTIONS);
//
//                        SP.putString("TRANSACTIONSDATES", strDate);
//                    }
//
//                    Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.internetDisconnected), Toast.LENGTH_LONG).show();
//                }
//
//            }
//        }
//    }

}
