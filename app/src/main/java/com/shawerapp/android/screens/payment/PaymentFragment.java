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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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


public class PaymentFragment extends BaseFragment implements PaymentContract.View,
        CheckoutIdRequestListener,PaymentStatusRequestListener,InterfaceName {

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
        Log.d("questionDescription", "questionDescription: "+questionDescription);
        args.putString(ARG_AUDIO_FILE_UPLOAD, audioFileUpload);
        args.putSerializable(ARG_ATTACHMENT_FILE_UPLOAD, (Serializable) attachmentFileUpload);
        args.putSerializable("mRecordedAudioFile", mRecordedAudioFile);
        args.putCharSequence("mComposition", mComposition);
        Log.d("compositionfr", "compositionfr: "+mComposition);
        GlobalData.mCompositionchar=mComposition;

      //  GlobalData.mComposition=mComposition;

        //Log.d("mComposition", "mComposition: "+mComposition.toString());


        args.putSerializable("mComposerViewModel", mComposerViewModel);


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
    private InterfaceName interfaceName;


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
        mViewModel.setPaidStatus(true,
                mContainerView, this, viewModel, transActionID);

        mViewModel.onSubmitComposition();
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

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.paymentdialagg);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        ImageView imageView=dialog.findViewById(R.id.imageviewsucc);
        //imageView.setImageDrawable(getActivity().getDrawable(R.drawable.sucess));
        text.setText(successMessage);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setText(getString(R.string.thankyou));


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    onConfirm.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    Timber.e(CommonUtils.getExceptionString(e));

                }

            }
        });

        dialog.show();


        dialog.show();
       /* AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        builder.setMessage(successMessage);
        builder.setPositiveButton(R.string.ok_caps, (dialog, which) -> {
            dialog.dismiss();
            try {
                onConfirm.run();
            } catch (Exception e) {
                Timber.e(CommonUtils.getExceptionString(e));
            }
        });
        builder.show();*/



    }


    public void showDialog(Activity activity, String msg,boolean paied) {


        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.paymentdialagg);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        ImageView imageView=dialog.findViewById(R.id.imageviewsucc);
        imageView.setImageDrawable(getActivity().getDrawable(R.drawable.sucess));
        text.setText(msg);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setText(getString(R.string.thankyou));


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

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


        if (savedInstanceState != null) {
            resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH);
        }
        requestCheckoutId(getString(R.string.checkout_ui_callback_scheme));

        transActionID = "" + System.currentTimeMillis();



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
                    //Toast.makeText(getActivity(), "" + description, Toast.LENGTH_SHORT).show();
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


        if (requestCode == CheckoutActivity.REQUEST_CODE_CHECKOUT)
            if (resultCode == CheckoutActivity.RESULT_OK) {

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
                getActivity().getFragmentManager().popBackStack();

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

        addTransactionToShared();
        Log.e("checkout", "chechout");
        new CheckoutIdRequestAsyncTask(amount, "ar", transActionID, this);

    }

    protected boolean hasCallbackScheme(Intent intent) {
        String scheme = intent.getScheme();

        return getString(R.string.checkout_ui_callback_scheme).equals(scheme) ||
                getString(R.string.payment_button_callback_scheme).equals(scheme) ||
                getString(R.string.custom_ui_callback_scheme).equals(scheme);
    }

    @Override
    public void onNewIntent(Intent intent) {

        /* Check if the intent contains the callback scheme. */
        if (resourcePath != null && hasCallbackScheme(intent)) {
            requestPaymentStatus(resourcePath, "ar", this);
        }

    }


    public  void openMain(){

        Intent intent=new Intent(getActivity(),ContainerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK
                |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }



}
