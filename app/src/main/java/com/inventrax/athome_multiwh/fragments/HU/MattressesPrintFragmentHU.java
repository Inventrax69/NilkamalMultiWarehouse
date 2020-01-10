package com.inventrax.athome_multiwh.fragments.HU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.inventrax.athome_multiwh.pojos.ExecutionResponseDTO;
import com.inventrax.athome_multiwh.pojos.ItemInfoDTO;
import com.inventrax.athome_multiwh.pojos.OutboundDTO;
import com.inventrax.athome_multiwh.pojos.VLPDResponseDTO;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
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

public class MattressesPrintFragmentHU extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_MATTERESS BUNDLE";
    private View rootView;

    private RelativeLayout rlMattress, rlVLPD, rlPrint;
    private TextView lblVLPDNumber, lblScannedSku, lblCount, lblBundle;
    private CardView cvScanBarcode;
    private ImageView ivScanBarcode;
    private TextInputLayout txtInputLayoutVLPD;
    private CustomEditText etVLPD, et_vlpd, etBundlePrint, etPrinterIP;
    private Button btnOk, btnCloseVLPD, btnNew, btnClear, btnPrint, btnprint_final, btnClosefinal, btnClosePrint;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private boolean _isPrintWindowRequired = false;
    String userId = null, materialType = null, vlpdId = null, vlpdTypeId = null, SkipReason = null, vlpdNo = null;

    private boolean isPrintWindowRequired = false;
    private String ipAddress = null, printerIPAddress = null, bundleNo = null;
    private boolean IsRSNScanned = false;

    VLPDResponseDTO vlpdresponseobj = null;
    ItemInfoDTO vlpdItem = null;
    private SoundUtils soundUtils;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };


    public MattressesPrintFragmentHU() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_mattresses_print, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlVLPD = (RelativeLayout) rootView.findViewById(R.id.rlVLPD);
        rlMattress = (RelativeLayout) rootView.findViewById(R.id.rlMattress);
        rlPrint = (RelativeLayout) rootView.findViewById(R.id.rlPrint);

        lblVLPDNumber = (TextView) rootView.findViewById(R.id.lblVLPDNumber);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblCount = (TextView) rootView.findViewById(R.id.lblCount);
        lblBundle = (TextView) rootView.findViewById(R.id.lblBundle);

        cvScanBarcode = (CardView) rootView.findViewById(R.id.cvScanBarcode);

        ivScanBarcode = (ImageView) rootView.findViewById(R.id.ivScanBarcode);

        vlpdresponseobj = new VLPDResponseDTO();

        txtInputLayoutVLPD = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutVLPD);

        etVLPD = (CustomEditText) rootView.findViewById(R.id.etVLPD);
        et_vlpd = (CustomEditText) rootView.findViewById(R.id.et_vlpd);
        etBundlePrint = (CustomEditText) rootView.findViewById(R.id.etBundlePrint);
        etPrinterIP = (CustomEditText) rootView.findViewById(R.id.etPrinterIP);

        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnCloseVLPD = (Button) rootView.findViewById(R.id.btnCloseVLPD);
        btnNew = (Button) rootView.findViewById(R.id.btnNew);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnprint_final = (Button) rootView.findViewById(R.id.btnprint_final);
        btnClosefinal = (Button) rootView.findViewById(R.id.btnClosefinal);
        btnClosePrint = (Button) rootView.findViewById(R.id.btnClosePrint);

        btnOk.setOnClickListener(this);
        btnCloseVLPD.setOnClickListener(this);
        btnNew.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        btnprint_final.setOnClickListener(this);
        btnClosefinal.setOnClickListener(this);
        btnClosePrint.setOnClickListener(this);


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        soundUtils = new SoundUtils();

        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        ipAddress = spPrinterIP.getString("printerIP", "");
        if (ipAddress != null) {
            etPrinterIP.setText(ipAddress);
        }

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

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseVLPD:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnClosePrint:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnOk:
                if (!etVLPD.getText().toString().isEmpty()) {

                    getBundleNumberForMatress("0");

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0039, getActivity(), getContext(), "Warning");
                }
                break;
            case R.id.btnClosefinal:

                rlPrint.setVisibility(View.GONE);
                rlVLPD.setVisibility(View.GONE);
                rlMattress.setVisibility(View.VISIBLE);

                break;

            case R.id.btnNew:

                if (!lblBundle.getText().toString().isEmpty()) {
                    getBundleNumberForMatress("1");
                }

                break;


            case R.id.btnClear:

                ClearFields();

                break;


            case R.id.btnPrint:

                printValidations();
                break;

            case R.id.btnprint_final:
                printeMatressBundle();
                break;


            default:
                break;
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if(!ProgressDialogUtils.isProgressActive()) {

                if (ScanValidator.IsRSNScanned(scannedData) && !(lblBundle.getText().toString().isEmpty())) {

                    lblScannedSku.setText(scannedData);

                    captureMatressBundlePacking();

                }
            }else {
                if(!common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(),getContext());

            }

        }

    }

    public void captureMatressBundlePacking() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setUserId(userId);
            outboundDTO.setBoxNO(lblBundle.getText().toString());
            outboundDTO.setEANNumber(lblScannedSku.getText().toString());
            outboundDTO.setTotalScannedQty("1");
            outboundDTO.setOBDNumber(lblVLPDNumber.getText().toString());

            message.setEntityObject(outboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.CaptureMatressBundlePacking(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lCore = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCore = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<VLPDResponseDTO> lstDto = new ArrayList<>();
                                VLPDResponseDTO vlpdResponseDTO = null;
                                for (int i = 0; i < _lCore.size(); i++) {

                                    vlpdResponseDTO = new VLPDResponseDTO(_lCore.get(i).entrySet());
                                    lstDto.add(vlpdResponseDTO);

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (vlpdResponseDTO.getPreviousPickedItemResponce().get(0).getStatus()) {

                                    cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanBarcode.setImageResource(R.drawable.check);

                                    lblCount.setText(vlpdResponseDTO.getPreviousPickedItemResponce().get(0).getMessage());

                                    return;
                                } else {

                                    cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanBarcode.setImageResource(R.drawable.invalid_cross);

                                    common.showUserDefinedAlertType(vlpdResponseDTO.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Error");
                                    return;
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void printValidations() {


        if (!lblCount.getText().toString().isEmpty()) {

            if (ipAddress != null) {
                printerIPAddress = ipAddress;
            } else {

                common.showUserDefinedAlertType(errorMessages.EMC_0046, getActivity(), getContext(), "Error");
                return;

            }

            rlVLPD.setVisibility(View.GONE);
            rlMattress.setVisibility(View.GONE);
            rlPrint.setVisibility(View.VISIBLE);

            et_vlpd.setText(lblVLPDNumber.getText().toString());
            etBundlePrint.setText(bundleNo);
            etPrinterIP.setText(ipAddress);


        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_034, getActivity(), getContext(), "Error");
            return;
        }

    }

    public void ClearFields() {
        cvScanBarcode.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
        ivScanBarcode.setImageResource(R.drawable.fullscreen_img);

        lblScannedSku.setText("");
        lblCount.setText("");

    }

    public void getBundleNumberForMatress(String isNew) {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setUserId(userId);
            outboundDTO.setIsNew(isNew);
            outboundDTO.setOBDNumber(etVLPD.getText().toString());

            message.setEntityObject(outboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetBundleNumberForMatress(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBundleNumberForMatress_01", getActivity());
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
                                }
                             else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lCore = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCore = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<ExecutionResponseDTO> lstDto = new ArrayList<>();
                                ExecutionResponseDTO oExecutionResponseDto = null;
                                for (int i = 0; i < _lCore.size(); i++) {

                                    oExecutionResponseDto = new ExecutionResponseDTO(_lCore.get(i).entrySet());
                                    lstDto.add(oExecutionResponseDto);

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (oExecutionResponseDto.getStatus()) {

                                    rlVLPD.setVisibility(View.GONE);
                                    rlMattress.setVisibility(View.VISIBLE);

                                    lblVLPDNumber.setText(etVLPD.getText().toString());
                                    lblBundle.setText(oExecutionResponseDto.getMessage());

                                    bundleNo = oExecutionResponseDto.getMessage();

                                    return;
                                } else {
                                    common.showUserDefinedAlertType(oExecutionResponseDto.getMessage(), getActivity(), getContext(), "Error");
                                    return;
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBundleNumberForMatress_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBundleNumberForMatress_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBundleNumberForMatress_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void printeMatressBundle() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setUserId(userId);
            outboundDTO.setIpAddress(etPrinterIP.getText().toString());
            outboundDTO.setBoxNO(etBundlePrint.getText().toString());
            outboundDTO.setOBDNumber(lblVLPDNumber.getText().toString());
            message.setEntityObject(outboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.PrinteMatressBundle(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_01", getActivity());
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
                                }
                             else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lCore = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCore = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<ExecutionResponseDTO> lstDto = new ArrayList<>();
                                ExecutionResponseDTO oExecutionResponseDto = null;
                                for (int i = 0; i < _lCore.size(); i++) {

                                    oExecutionResponseDto = new ExecutionResponseDTO(_lCore.get(i).entrySet());
                                    lstDto.add(oExecutionResponseDto);

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                common.showUserDefinedAlertType(errorMessages.EMC_0049, getActivity(), getContext(), "Success");
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_04", getActivity());
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
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_mattress));
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