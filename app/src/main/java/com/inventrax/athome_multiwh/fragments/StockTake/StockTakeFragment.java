package com.inventrax.athome_multiwh.fragments.StockTake;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.inventrax.athome_multiwh.adapters.PendingStockTakeListAdapter;
import com.inventrax.athome_multiwh.common.Common;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;
import com.inventrax.athome_multiwh.common.constants.ErrorMessages;
import com.inventrax.athome_multiwh.fragments.HomeFragment;
import com.inventrax.athome_multiwh.interfaces.ApiInterface;
import com.inventrax.athome_multiwh.pojos.StockCountDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.ExceptionLoggerUtils;
import com.inventrax.athome_multiwh.util.FragmentUtils;
import com.inventrax.athome_multiwh.util.ProgressDialogUtils;
import com.inventrax.athome_multiwh.util.ScanValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockTakeFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_012";
    private View rootView;
    TextView txt_Desc;
    private IntentFilter filter;
    private Gson gson;
    String userId = null;
    private Common common;
    private WMSCoreMessage core;
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    EditText et_Qty, etsku, etEAN, etLocation, et_binCount;
    Button btnClear, btnExport, btnCloseBin, btnUpdate, btnClose, btnClose_list;
    String storageLoc = "";
    boolean isValidLocation = false;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String scanner = null;
    String getScanner = null;
    List<String> lstSpinnerView;

    CardView cvScanLocation, cvScanRSN;
    ImageView ivScanLocation, ivScanRSN;
    SearchableSpinner spinnerStrorageLoc;
    private RecyclerView rvStockTakePendingList;
    private LinearLayoutManager linearLayoutManager;
    RelativeLayout rl_view, rl_list;
    private int masterbarcodeQty;


    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public StockTakeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stocktake, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {
        lstSpinnerView = new ArrayList<>();
        lstSpinnerView.clear();
        lstSpinnerView.add("Select storage loc");
        lstSpinnerView.add("0001");
        lstSpinnerView.add("0002");
        lstSpinnerView.add("003");
        lstSpinnerView.add("ZRTV");
        spinnerStrorageLoc = (SearchableSpinner) rootView.findViewById(R.id.spinnerStrorageLoc);

        ArrayAdapter arrayAdapterType = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstSpinnerView);
        spinnerStrorageLoc.setAdapter(arrayAdapterType);
        spinnerStrorageLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                storageLoc = spinnerStrorageLoc.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txt_Desc = (TextView) rootView.findViewById(R.id.txt_Desc);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        et_Qty = (EditText) rootView.findViewById(R.id.et_Qty);
        etEAN = (EditText) rootView.findViewById(R.id.etEAN);
        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etsku = (EditText) rootView.findViewById(R.id.etsku);
        et_binCount = (EditText) rootView.findViewById(R.id.et_binCount);

        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnCloseBin = (Button) rootView.findViewById(R.id.btnCloseBin);
        btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnClose_list = (Button) rootView.findViewById(R.id.btnClose_list);

        btnClear.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnCloseBin.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnClose_list.setOnClickListener(this);

        rl_view = (RelativeLayout) rootView.findViewById(R.id.rl_view);
        rl_list = (RelativeLayout) rootView.findViewById(R.id.rl_list);

        rvStockTakePendingList = (RecyclerView) rootView.findViewById(R.id.rvStocktakePendingList);
        rvStockTakePendingList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        // use a linear layout manager
        rvStockTakePendingList.setLayoutManager(linearLayoutManager);
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        //For Honeywell
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                ClearFields();
                break;

            case R.id.btnExport:
                if (!etLocation.getText().toString().isEmpty()) {
                    GetStockDetailsHH();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Warning");
                }
                break;

            case R.id.btnCloseBin:
                if (!etLocation.getText().toString().isEmpty()) {
                    CloseHHBin();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Warning");
                }
                break;

            case R.id.btnUpdate:

                if (et_Qty.getText().toString().isEmpty() || et_Qty.getText().toString().equalsIgnoreCase("0")) {
                    common.showUserDefinedAlertType(errorMessages.EMC_080, getActivity(), getContext(), "Warning");
                } else {

                    if(Integer.parseInt(et_Qty.getText().toString()) > masterbarcodeQty){

                        common.showUserDefinedAlertType(errorMessages.EMC_082,getActivity(),getContext(),"Warning");
                        return;

                    }else {
                        UpsertEANDetails();
                    }
                }
                break;

            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnClose_list:
                rl_list.setVisibility(View.GONE);
                rl_view.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
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

    //Honeywell Barcode reader Properties
    public void HoneyWellBarcodeListeners()
    {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null)
        {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try
            {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e)
            {
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
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, true);
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


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData)
    {

         if (scannedData != null && !common.isPopupActive())
        {
            if (storageLoc.equalsIgnoreCase("Select storage loc") || storageLoc.equalsIgnoreCase(""))
            {
                common.showUserDefinedAlertType(errorMessages.EMC_076, getActivity(), getContext(), "Warning");
                return;
            } else {
                if (!ScanValidator.IsRSNScanned(scannedData) && !ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData))
                {
                    if (isValidLocation)
                    {
                        if (scannedData.split("[,]").length == 2)
                        {
                            try {
                                // double scannedQty = Double.parseDouble(scannedData.split(",")[1]);
                                etEAN.setText(scannedData.split("[,]")[0]);
                                if (Integer.parseInt(scannedData.split("[,]")[1].trim()) > 1)
                                {
                                    //if qty more than 1 enable Update button and Qty filed
                                    et_Qty.setText(scannedData.split("[,]")[1].trim());

                                    masterbarcodeQty = Integer.parseInt(scannedData.split("[,]")[1].trim());

                                    GetHHDetails(true);
                                } else {
                                    //if qty equals  1 enable  disable update button and Qty field and auto call UpsertEANDetails();
                                    GetHHDetails(false);
                                }
                            } catch (Exception ex)
                            {
                                common.showUserDefinedAlertType(errorMessages.EMC_036, getActivity(), getContext(), "Warning");
                                return;
                            }
                        } else if (scannedData.split("[,]").length == 1)
                        {
                            etEAN.setText(scannedData);
                            GetHHDetails(false);
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_036, getActivity(), getContext(), "Warning");
                            return;
                        }
                    } else
                        {
                        common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Warning");
                    }
                } else if (scanValidator.IsLocationScanned(scannedData))
                {
                    etLocation.setText(scannedData);
                    GetLocationStatusForHH();

                }
            }
        }
    }

    private void GetStockDetailsHH() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.StockCountDTO, getContext());
            StockCountDTO inboundDTO = new StockCountDTO();
            inboundDTO.setLocation(etLocation.getText().toString());
            message.setEntityObject(inboundDTO);
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetStockDetailsHH(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetStockDetailsHH_01", getActivity());
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
                                List<LinkedTreeMap<?, ?>> _lVLPDLoading = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPDLoading = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                StockCountDTO dto = null;
                                List<StockCountDTO> itemInfoDTOLst = new ArrayList<StockCountDTO>();
                                for (int i = 0; i < _lVLPDLoading.size(); i++) {
                                    dto = new StockCountDTO(_lVLPDLoading.get(i).entrySet());
                                    itemInfoDTOLst.add(dto);
                                }
                                rl_view.setVisibility(View.GONE);
                                rl_list.setVisibility(View.VISIBLE);
                                PendingStockTakeListAdapter pendingLoadingListAdapter = new PendingStockTakeListAdapter(getActivity(), itemInfoDTOLst);
                                rvStockTakePendingList.setAdapter(pendingLoadingListAdapter);
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetStockDetailsHH_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetStockDetailsHH_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetStockDetailsHH_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    private void CloseHHBin() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.StockCountDTO, getContext());
            StockCountDTO inboundDTO = new StockCountDTO();
            inboundDTO.setLocation(etLocation.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.CloseHHBin(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseHHBin_01", getActivity());
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
                                List<LinkedTreeMap<?, ?>> _lstTransferDto = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstTransferDto = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                StockCountDTO dto = null;
                                for (int i = 0; i < _lstTransferDto.size(); i++) {
                                    dto = new StockCountDTO(_lstTransferDto.get(i).entrySet());
                                }
                                ClearFields();
                                common.showUserDefinedAlertType(errorMessages.EMC_078, getActivity(), getContext(), "Success");
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseHHBin_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseHHBin_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CloseHHBin_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    private void GetHHDetails(final boolean value) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.StockCountDTO, getContext());
            StockCountDTO inboundDTO = new StockCountDTO();
            inboundDTO.setEANnumber(etEAN.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetHHDetails(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetHHDetails_01", getActivity());
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
                                etEAN.setText("");
                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanRSN.setImageResource(R.drawable.invalid_cross);

                            } else {

                                List<LinkedTreeMap<?, ?>> _lstTransferDto = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstTransferDto = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                StockCountDTO dto = null;
                                for (int i = 0; i < _lstTransferDto.size(); i++) {
                                    dto = new StockCountDTO(_lstTransferDto.get(i).entrySet());
                                }

                                etsku.setText(dto.getSKU());
                                txt_Desc.setText(dto.getPartdesc());

                                if (value) {
                                    et_Qty.setEnabled(true);

                                    common.showUserDefinedAlertType(errorMessages.EMC_079, getActivity(), getContext(), "Warning");

                                    btnUpdate.setEnabled(true);
                                    btnUpdate.setTextColor(getResources().getColor(R.color.white));
                                    btnUpdate.setBackgroundResource(R.drawable.button_shape);

                                } else
                                    {
                                    et_Qty.setText("1");
                                    et_Qty.setEnabled(false);
                                    btnUpdate.setEnabled(false);
                                    UpsertEANDetails();
                                }
                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanRSN.setImageResource(R.drawable.check);
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetHHDetails_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetHHDetails_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetHHDetails_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    private void UpsertEANDetails() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.StockCountDTO, getContext());
            StockCountDTO inboundDTO = new StockCountDTO();
            inboundDTO.setUserid(userId);
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setEANnumber(etEAN.getText().toString());
            inboundDTO.setSKU(etsku.getText().toString());
            inboundDTO.setPartdesc(txt_Desc.getText().toString());
            inboundDTO.setStorageLocation(storageLoc);
            inboundDTO.setEANnumber(etEAN.getText().toString());
            inboundDTO.setBinCount(et_binCount.getText().toString());
            inboundDTO.setBoxQty(et_Qty.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.UpsertEANDetails(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex)
            {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpsertEANDetails_01", getActivity());
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
                                for (int i = 0; i < _lExceptions.size(); i++)
                                {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstTransferDto = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstTransferDto = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                StockCountDTO dto = null;
                                for (int i = 0; i < _lstTransferDto.size(); i++) {
                                    dto = new StockCountDTO(_lstTransferDto.get(i).entrySet());
                                }

                                if (!dto.getID().equalsIgnoreCase("0"))
                                {

                                    et_binCount.setText(dto.getBinCount());
                                    et_Qty.setText("");
                                    if(btnUpdate.isEnabled())
                                    {
                                        common.showUserDefinedAlertType(errorMessages.EMC_077, getActivity(), getContext(), "Success");
                                    }
                                    btnUpdate.setEnabled(false);
                                    btnUpdate.setTextColor(getResources().getColor(R.color.black));
                                    btnUpdate.setBackgroundResource(R.drawable.button_hide);

                                    //common.showUserDefinedAlertType(errorMessages.EMC_077, getActivity(), getContext(), "Success");
                                } else
                                    {
                                    common.showUserDefinedAlertType(errorMessages.EMC_075, getActivity(), getContext(), "Error");
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpsertEANDetails_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpsertEANDetails_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpsertEANDetails_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    private void ClearFields() {
        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

        btnExport.setEnabled(false);
        btnExport.setTextColor(getResources().getColor(R.color.black));
        btnExport.setBackgroundResource(R.drawable.button_hide);

        btnCloseBin.setEnabled(false);
        btnCloseBin.setTextColor(getResources().getColor(R.color.black));
        btnCloseBin.setBackgroundResource(R.drawable.button_hide);


        btnUpdate.setEnabled(false);
        btnUpdate.setTextColor(getResources().getColor(R.color.black));
        btnUpdate.setBackgroundResource(R.drawable.button_hide);


        isValidLocation = false;
        etEAN.setText("");
        etsku.setText("");
        etLocation.setText("");
        et_Qty.setText("");
        et_binCount.setText("");
        txt_Desc.setText("");

        et_Qty.setEnabled(false);
        btnUpdate.setEnabled(false);

        ArrayAdapter arrayAdapterType = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstSpinnerView);
        spinnerStrorageLoc.setAdapter(arrayAdapterType);
        int spinnerPosition = arrayAdapterType.getPosition("Select");
        spinnerStrorageLoc.setEnabled(true);
        //set the default according to value
        spinnerStrorageLoc.setSelection(spinnerPosition);

    }


    private void GetLocationStatusForHH() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.StockCountDTO, getContext());
            StockCountDTO inboundDTO = new StockCountDTO();
            inboundDTO.setLocation(etLocation.getText().toString());
            inboundDTO.setUserid(userId);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetLocationStatusForHH(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationStatusForHH_01", getActivity());
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
                                etLocation.setText("");
                                isValidLocation = false;
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.invalid_cross);
                                //common.showAlertType(owmsExceptionMessage.getWMSMessage(), getActivity(), getContext());
                                common.showUserDefinedAlertType(owmsExceptionMessage.getWMSMessage(), getActivity(), getContext(), "Error");

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstTransferDto = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstTransferDto = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                StockCountDTO dto = null;
                                for (int i = 0; i < _lstTransferDto.size(); i++) {
                                    dto = new StockCountDTO(_lstTransferDto.get(i).entrySet());
                                }

                                et_binCount.setText(dto.getBinCount());
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.check);
                                isValidLocation = true;
                                spinnerStrorageLoc.setEnabled(false);
                                btnExport.setEnabled(true);
                                btnExport.setTextColor(getResources().getColor(R.color.white));
                                btnExport.setBackgroundResource(R.drawable.button_shape);

                                btnCloseBin.setEnabled(true);
                                btnCloseBin.setTextColor(getResources().getColor(R.color.white));
                                btnCloseBin.setBackgroundResource(R.drawable.button_shape);

                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationStatusForHH_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationStatusForHH_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetLocationStatusForHH_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
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

                            // if any Exception throws
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
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ProgressDialogUtils.closeProgressDialog();
                            //Log.d("Message", core.getEntityObject().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_stocktake));
    }


    @Override
    public void onDestroyView() {
        // Honeywell onDestroyView
        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }
        // Cipher onDestroyView
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }
}
