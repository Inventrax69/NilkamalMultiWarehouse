package com.inventrax.athome_multiwh.fragments.HU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.inventrax.athome_multiwh.R;
import com.inventrax.athome_multiwh.activities.MainActivity;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.EcomPackingDTO;
import com.inventrax.athome_multiwh.pojos.PrintResponce;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.CustomEditText;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;
import com.inventrax.athome_multiwh.util.ScanValidator;
import com.inventrax.athome_multiwh.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EcomBulkPackingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_ECOM BULK PACKING";
    private View rootView;

    private RelativeLayout rlSelectPrinter, rlBulk;

    private CustomEditText etVlpdNo;
    private SearchableSpinner spinnerRSNPrinter, spinnerFlipkartBulkPrinter, spinnerAmazonPinter, spinnerSONumber;
    private Button btnStart, btnCloseOne, btnBack, btnCloseTwo;
    private String RSNPrinter = "", flipkartBulkPrinter = "", amazonPrinter = "", SOnumber = "";
    private CheckBox cbRSNwithMRP, cbRSNwithoutMRP, cbFlipkartLabel, cbAmazonLabel;
    private CardView cvScanRSN;
    private ImageView ivScanRSN;
    private TextView tvScannedRSN, tvStatusOne, tvStatusTwo, tvStatusThree, tvStatusFour;

    private boolean isRSNwithMRP = false, isRSNwithoutMRP = false, isFlipkartLabel = false, isAmazonLabel = false;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null;
    private String OBDRefNo;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    int typeID;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    List printer;

    private String scannedBarcode = "";

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public EcomBulkPackingFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.ecom_bulkpacking_fragment, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlSelectPrinter = (RelativeLayout) rootView.findViewById(R.id.rlSelectPrinter);
        rlBulk = (RelativeLayout) rootView.findViewById(R.id.rlBulk);

        tvScannedRSN = (TextView) rootView.findViewById(R.id.tvScannedRSN);
        tvStatusOne = (TextView) rootView.findViewById(R.id.tvStatusOne);
        tvStatusTwo = (TextView) rootView.findViewById(R.id.tvStatusTwo);
        tvStatusThree = (TextView) rootView.findViewById(R.id.tvStatusThree);
        tvStatusFour = (TextView) rootView.findViewById(R.id.tvStatusFour);

        etVlpdNo = (CustomEditText) rootView.findViewById(R.id.etVlpdNo);

        printer = new ArrayList();

        spinnerRSNPrinter = (SearchableSpinner) rootView.findViewById(R.id.spinnerRSNPrinter);
        spinnerFlipkartBulkPrinter = (SearchableSpinner) rootView.findViewById(R.id.spinnerFlipkartBulkPrinter);
        spinnerAmazonPinter = (SearchableSpinner) rootView.findViewById(R.id.spinnerAmazonPinter);
        spinnerSONumber = (SearchableSpinner) rootView.findViewById(R.id.spinnerSONumber);

        spinnerRSNPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!spinnerRSNPrinter.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    RSNPrinter = spinnerRSNPrinter.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerFlipkartBulkPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!spinnerFlipkartBulkPrinter.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    flipkartBulkPrinter = spinnerFlipkartBulkPrinter.getSelectedItem().toString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerAmazonPinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!spinnerAmazonPinter.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    amazonPrinter = spinnerAmazonPinter.getSelectedItem().toString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerSONumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerSONumber.getSelectedItem().toString().equalsIgnoreCase("Select")) {
                    SOnumber = spinnerSONumber.getSelectedItem().toString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnStart = (Button) rootView.findViewById(R.id.btnStart);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnBack = (Button) rootView.findViewById(R.id.btnBack);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);

        cbRSNwithMRP = (CheckBox) rootView.findViewById(R.id.cbRSNwithMRP);
        cbRSNwithoutMRP = (CheckBox) rootView.findViewById(R.id.cbRSNwithoutMRP);
        cbFlipkartLabel = (CheckBox) rootView.findViewById(R.id.cbFlipkartLabel);
        cbAmazonLabel = (CheckBox) rootView.findViewById(R.id.cbAmazonLabel);

        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        btnStart.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();


        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        //For Honey well
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {

                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();

                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });


/*
        etVlpdNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    getSOnum();
                }
            }
        });
*/

        etVlpdNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length()==11) getSOnum();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cbRSNwithMRP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isRSNwithMRP = true;
                } else {
                    isRSNwithMRP = false;
                }
            }
        });

        cbRSNwithoutMRP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isRSNwithoutMRP = true;
                } else {
                    isRSNwithoutMRP = false;
                }
            }
        });

        cbFlipkartLabel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isFlipkartLabel = true;
                } else {
                    isFlipkartLabel = false;
                }
            }
        });

        cbAmazonLabel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAmazonLabel = true;
                } else {
                    isAmazonLabel = false;
                }
            }
        });

        getRSNPrinter();
    }


    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnStart:

                if (!RSNPrinter.equals("Select") && !RSNPrinter.equals("")) {

                    if (!flipkartBulkPrinter.equals("Select") && !flipkartBulkPrinter.equals("")) {

                        if (!amazonPrinter.equals("Select") && !amazonPrinter.equals("")) {

                            if (!etVlpdNo.getText().toString().isEmpty()) {

                                if (!SOnumber.equals("Select") && !SOnumber.equals("")) {

                                    rlSelectPrinter.setVisibility(View.GONE);
                                    rlBulk.setVisibility(View.VISIBLE);

                                } else {
                                    common.showUserDefinedAlertType("Please select SO number", getActivity(), getContext(), "Error");
                                }

                            } else {
                                common.showUserDefinedAlertType("Please enter VLPD number", getActivity(), getContext(), "Error");
                            }
                        } else {
                            common.showUserDefinedAlertType("Please select Amazon printer", getActivity(), getContext(), "Error");
                        }
                    } else {
                        common.showUserDefinedAlertType("Please select Flipkart printer", getActivity(), getContext(), "Error");
                    }


                } else {
                    common.showUserDefinedAlertType("Please select RSN printer.", getActivity(), getContext(), "Error");
                }

                break;

            case R.id.btnBack:

                rlSelectPrinter.setVisibility(View.VISIBLE);
                rlBulk.setVisibility(View.GONE);

                break;


            default:
                break;
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if (isRSNwithMRP || isRSNwithoutMRP || isFlipkartLabel || isAmazonLabel) {

                if (ScanValidator.IsRSNScanned(scannedData.trim())) {

                    scannedBarcode = scannedData.trim();

                    tvScannedRSN.setText(scannedBarcode);
                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanRSN.setImageResource(R.drawable.check);

                    printECOMFurnitureLabel(scannedBarcode);

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                }

            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0051, getActivity(), getContext(), "Error");
            }

        }
    }


    private void getRSNPrinter() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.EcomPackingDTO, getContext());
            EcomPackingDTO ecomPackingDTO = new EcomPackingDTO();
            ecomPackingDTO.setUserID(userId);
            ecomPackingDTO.setResourceType("Label Printer");

            message.setEntityObject(ecomPackingDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetClientResources(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;

                                }
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                printer = (List) core.getEntityObject();

                                ArrayAdapter arrayAdapterPrinter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, printer);
                                spinnerRSNPrinter.setAdapter(arrayAdapterPrinter);
                                spinnerFlipkartBulkPrinter.setAdapter(arrayAdapterPrinter);
                                spinnerAmazonPinter.setAdapter(arrayAdapterPrinter);


                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }


    private void getSOnum() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.EcomPackingDTO, getContext());
            EcomPackingDTO ecomPackingDTO = new EcomPackingDTO();
            ecomPackingDTO.setUserID(userId);
            ecomPackingDTO.setVlpdNumber(etVlpdNo.getText().toString());

            message.setEntityObject(ecomPackingDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetSONumbersByVLPD(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List soNum = new ArrayList();
                                soNum = (List) core.getEntityObject();
                                spinnerSONumber.setAdapter(null);
                                ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, soNum);
                                spinnerSONumber.setAdapter(arrayAdapterStoreRefNo);
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }


    private void printECOMFurnitureLabel(String barcode) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.EcomPackingDTO, getContext());
            EcomPackingDTO ecomPackingDTO = new EcomPackingDTO();
            ecomPackingDTO.setUserID(userId);
            ecomPackingDTO.setRSNPrinter(RSNPrinter);
            ecomPackingDTO.setFlipcartBulkOrderPrinter(flipkartBulkPrinter);
            ecomPackingDTO.setAmazonBulkOrderPrinter(amazonPrinter);
            ecomPackingDTO.setVlpdNumber(etVlpdNo.getText().toString());
            ecomPackingDTO.setSoNumber(SOnumber);
            ecomPackingDTO.setISPrintAmazonAsinStickerRequired(isAmazonLabel);
            ecomPackingDTO.setISPrintRSNWithoutMRPRequired(isRSNwithoutMRP);
            ecomPackingDTO.setPrintRSNWithMRPRequired(isRSNwithMRP);
            ecomPackingDTO.setISPrintBulkOrderShippingLableRequired(isFlipkartLabel);
            ecomPackingDTO.setBarcode(barcode);

            message.setEntityObject(ecomPackingDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.PrintEcomLabelsForFurniture(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List res = new ArrayList<>();
                                res = (List) core.getEntityObject();

                                List<LinkedTreeMap<?, ?>> _lI = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lI = (List<LinkedTreeMap<?, ?>>) res;

                                List<PrintResponce> printResponces = new ArrayList<>();

                                for (int i = 0; i < _lI.size(); i++) {
                                    PrintResponce oInboundDTO = new PrintResponce(_lI.get(i).entrySet());
                                    printResponces.add(oInboundDTO);

                                }

                                List<PrintResponce> list = new ArrayList<>();

                                for (PrintResponce obj : printResponces) {
                                    list.add((PrintResponce) obj);
                                }

                                tvStatusFour.setText("");
                                tvStatusThree.setText("");
                                tvStatusTwo.setText("");
                                tvStatusOne.setText("");

                                for (PrintResponce responce : list) {
                                   /* if (responce.getStatus()) {*/

                                        if (responce.getRequestType().equalsIgnoreCase("RSNWithMRP")) {
                                            tvStatusOne.setText(responce.getMessage());
                                        } else if (responce.getRequestType().equalsIgnoreCase("RSNWithoutMRP")) {
                                            tvStatusTwo.setText(responce.getMessage());
                                        } else if (responce.getRequestType().equalsIgnoreCase("BulkOrderLable")) {
                                            tvStatusThree.setText(responce.getMessage());
                                        } else if (responce.getRequestType().equals("AmazonASIN")) {
                                            tvStatusFour.setText(responce.getMessage());
                                        }
                                    /*}*/
                                }

                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForECOMPacking", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);

            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }


    // sending exception to the database
    public void logException() {

        try {

            String textFromFile = exceptionLoggerUtils.readFromFile(getActivity());

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);


                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_ecom_bulk_packing));
    }

    //Barcode scanner API
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }


}