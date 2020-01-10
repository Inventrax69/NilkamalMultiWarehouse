package com.inventrax.athome_multiwh.fragments.HH;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.inventrax.athome_multiwh.pojos.OutboundDTO;
import com.inventrax.athome_multiwh.pojos.VLPDRequestDTO;
import com.inventrax.athome_multiwh.pojos.VLPDResponseDTO;
import com.inventrax.athome_multiwh.pojos.VlpdDto;
import com.inventrax.athome_multiwh.pojos.WMSCoreMessage;
import com.inventrax.athome_multiwh.pojos.WMSExceptionMessage;
import com.inventrax.athome_multiwh.searchableSpinner.SearchableSpinner;
import com.inventrax.athome_multiwh.services.RestService;
import com.inventrax.athome_multiwh.util.CustomEditText;
import com.inventrax.athome_multiwh.util.DialogUtils;
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

import static com.inventrax.athome_multiwh.common.constants.EndpointConstants.Outbound;


@SuppressLint("ValidFragment")
public class ManualPacking extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_MANUAL PACKING";
    private View rootView;

    private RelativeLayout rlOBDSelect, rlOBD;
    private TextView lblOBDNumber, lblBox, lblScannedSku, lblDesc;
    private CardView cvScanEAN;
    private ImageView ivScanEAN;
    private TextInputLayout txtInputLayoutEAN, txtInputLayoutMRP, txtInputLayoutReqQty,
            txtInputLayoutBalQty, txtInputLayoutScanQty, txtInputLayoutTotalQty;
    private CustomEditText etEAN, etMRP, etReqQty, etBalQty, etScanQty, etTotalQty;
    private SearchableSpinner spinnerSelectOBDref;
    private Button btnGo, btnCloseOne, btnExport, btnCloseTwo, btnAdd;

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

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public ManualPacking() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_manualpacking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlOBDSelect = (RelativeLayout) rootView.findViewById(R.id.rlOBDSelect);
        rlOBD = (RelativeLayout) rootView.findViewById(R.id.rlOBD);

        lblOBDNumber = (TextView) rootView.findViewById(R.id.lblOBDNumber);
        lblBox = (TextView) rootView.findViewById(R.id.lblBox);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);

        cvScanEAN = (CardView) rootView.findViewById(R.id.cvScanEAN);

        ivScanEAN = (ImageView) rootView.findViewById(R.id.ivScanEAN);

        txtInputLayoutEAN = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutEAN);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);
        txtInputLayoutReqQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutReqQty);
        txtInputLayoutBalQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBalQty);
        txtInputLayoutScanQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutScanQty);
        txtInputLayoutTotalQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutTotalQty);

        etEAN = (CustomEditText) rootView.findViewById(R.id.etEAN);
        etMRP = (CustomEditText) rootView.findViewById(R.id.etMRP);
        etReqQty = (CustomEditText) rootView.findViewById(R.id.etReqQty);
        etBalQty = (CustomEditText) rootView.findViewById(R.id.etBalQty);
        etScanQty = (CustomEditText) rootView.findViewById(R.id.etScanQty);
        etTotalQty = (CustomEditText) rootView.findViewById(R.id.etTotalQty);

        spinnerSelectOBDref = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectOBDref);
        spinnerSelectOBDref.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OBDRefNo = spinnerSelectOBDref.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnAdd = (Button) rootView.findViewById(R.id.btnAdd);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        btnGo.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnExport.setOnClickListener(this);

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DialogUtils.showConfirmDialog(getActivity(), "Confirm Update",
                        "Are you sure you want to update new Box?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:

                                        if (!lblOBDNumber.getText().toString().isEmpty()) {
                                            getBoxNumberForOBD();
                                        } else {

                                        }
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //Toast.makeText(getActivity(),"Pressed cancel..!",Toast.LENGTH_LONG).show();

                                        break;
                                }

                            }
                        });
            }
        });


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


        if (getArguments() != null) {

            lblOBDNumber.setText(getArguments().getString("obdNo"));
            lblBox.setText(getArguments().getString("box"));
            lblScannedSku.setText(getArguments().getString("sku"));
            lblDesc.setText(getArguments().getString("desc"));
            etEAN.setText(getArguments().getString("ean"));
            etMRP.setText(getArguments().getString("mrp"));
            etReqQty.setText(getArguments().getString("reqQty"));
            etBalQty.setText(getArguments().getString("balQty"));
            etScanQty.setText(getArguments().getString("scanQty"));
            etTotalQty.setText(getArguments().getString("totalQty"));


            rlOBDSelect.setVisibility(View.GONE);
            rlOBD.setVisibility(View.VISIBLE);

            return;

        }

        getOpenOBDListForManulaPicking();



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
            case R.id.btnGo:

                if (OBDRefNo.toString().isEmpty() || OBDRefNo.toString().equals("Select")) {

                } else {
                    GetBoxNumberForOBD(false);
                }

                break;
            case R.id.btnExport:

                if (!lblOBDNumber.getText().toString().isEmpty()) {
                    goToExport();
                }

                break;

            default:
                break;
        }
    }


    public void goToExport() {


        Bundle bundle = new Bundle();

        bundle.putString("obdNo", lblOBDNumber.getText().toString());
        bundle.putString("box", lblBox.getText().toString());
        bundle.putString("sku", lblScannedSku.getText().toString());
        bundle.putString("desc", lblDesc.getText().toString());
        bundle.putString("ean", etEAN.getText().toString());
        bundle.putString("mrp", etMRP.getText().toString());
        bundle.putString("reqQty", etReqQty.getText().toString());
        bundle.putString("balQty", etBalQty.getText().toString());
        bundle.putString("scanQty", etScanQty.getText().toString());
        bundle.putString("totalQty", etTotalQty.getText().toString());

        PendingManualPackingFragment pendingManualPackingFragment = new PendingManualPackingFragment();
        pendingManualPackingFragment.setArguments(bundle);

        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, pendingManualPackingFragment);

    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {
        if (scannedData != null && !common.isPopupActive()) {

            if (rlOBD.getVisibility() == View.VISIBLE) {
                if (lblBox.getText().toString() != "0" || lblBox.getText().toString().isEmpty()) {
                    // Check for not to scan either RSN OR Location OR Pallet
                    if (!ScanValidator.IsRSNScanned(scannedData) && !ScanValidator.IsPalletScanned(scannedData) && !ScanValidator.IsLocationScanned(scannedData)) {

                        if (scannedData.split(",").length == 2) {
                            try {
                                double scannedQty = Double.parseDouble(scannedData.split(",")[1]);

                                etEAN.setText(scannedData.split(",")[0]);
                                etScanQty.setText(scannedData.split(",")[1]);
                            } catch (Exception ex) {
                                common.showUserDefinedAlertType(errorMessages.EMC_036, getActivity(), getContext(), "Error");

                                return;
                            }
                        } else if (scannedData.split(",").length == 1) {
                            etEAN.setText(scannedData.split(",")[0]);
                            etScanQty.setText("1");
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_036, getActivity(), getContext(), "Error");

                            return;
                        }

                        ProceessOBDBoxPicking();

                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_036, getActivity(), getContext(), "Error");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_037, getActivity(), getContext(), "Error");
                }


            } else {
                common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                return;
            }
        }
    }


    private void getOpenOBDListForManulaPicking() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(Outbound, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setType("1");

            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetOpenOBDListForManulaPicking(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForManulaPicking_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lOutbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<OutboundDTO> lstDto = new ArrayList<OutboundDTO>();
                                List<String> lstOutbound = new ArrayList<>();


                                for (int i = 0; i < _lOutbound.size(); i++) {
                                    OutboundDTO dto = new OutboundDTO(_lOutbound.get(i).entrySet());
                                    lstDto.add(dto);

                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstOutbound.add(lstDto.get(i).getOBDNumber());
                                }

                                ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstOutbound);
                                spinnerSelectOBDref.setAdapter(arrayAdapterStoreRefNo);
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForManulaPicking_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForManulaPicking_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenOBDListForManulaPicking_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    // To increment Box count
    private void getBoxNumberForOBD() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setUserId(userId);
            outboundDTO.setIsNew("1");
            outboundDTO.setOBDNumber(lblOBDNumber.getText().toString());
            message.setEntityObject(outboundDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetBoxNumberForOBD(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_01", getActivity());
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

                                ProgressDialogUtils.closeProgressDialog();

                                List<LinkedTreeMap<?, ?>> _lOBD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOBD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lOBD.size() > 0) {
                                    List<ExecutionResponseDTO> executionResponseDTOS = new ArrayList<ExecutionResponseDTO>();
                                    List<String> lstVLPD = new ArrayList<>();

                                    ExecutionResponseDTO dto = null;
                                    for (int i = 0; i < _lOBD.size(); i++) {
                                        dto = new ExecutionResponseDTO(_lOBD.get(i).entrySet());
                                        executionResponseDTOS.add(dto);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    //getItemToPick();
                                    if (dto.getStatus() == true) {
                                        lblBox.setText(dto.getMessage());

                                    } else {
                                        common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(), "Error");
                                        return;
                                    }
                                }
                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    private void GetPendingOBDItemsForManualPicking() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(Outbound, getContext());
            OutboundDTO outbound = new OutboundDTO();
            outbound.setUserId(userId);
            outbound.setOutboundNumber(OBDRefNo);
            message.setEntityObject(outbound);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetPendingOBDItemsForManualPicking(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lOBD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOBD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto dto = null;
                                for (int i = 0; i < _lOBD.size(); i++) {
                                    dto = new VlpdDto(_lOBD.get(i).entrySet());
                                }

                             /*   if (dto.getResult().toString().equalsIgnoreCase("1")) {
                                    lblBoxValue.Text = responce.Message;
                                    lblOBDValue.Text = cmbOutBoundNumberrs.SelectedItem.ToString();
                                    HandleViewPort("Details");
                                    lblTotalScannedQty.Text = string.Empty;
                                }
                                else {
                                    MessageBox.Show(responce.Message);
                                }*/

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }

    private void GetBoxNumberForOBD(Boolean isNew) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(Outbound, getContext());
            OutboundDTO outbound = new OutboundDTO();
            outbound.setUserId(userId);
            outbound.setOBDNumber(OBDRefNo);
            outbound.setIsNew((isNew ? "1" : "0"));

            message.setEntityObject(outbound);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.GetBoxNumberForOBD(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _OUTnbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _OUTnbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                ExecutionResponseDTO dto = null;
                                for (int i = 0; i < _OUTnbound.size(); i++) {
                                    dto = new ExecutionResponseDTO(_OUTnbound.get(i).entrySet());
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (dto.getStatus()) {

                                    lblBox.setText(dto.getMessage());
                                    lblOBDNumber.setText(OBDRefNo);
                                    rlOBDSelect.setVisibility(View.GONE);
                                    rlOBD.setVisibility(View.VISIBLE);
                                    etTotalQty.setText("");
                                    return;

                                } else {
                                    common.showUserDefinedAlertType(dto.getMessage(), getActivity(), getContext(), "Error");
                                    return;
                                }


                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetBoxNumberForOBD_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }


    }


    public void ClearFields() {
        cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanEAN.setImageResource(R.drawable.fullscreen_img);

        etBalQty.setText("");
        etScanQty.setText("");
        etReqQty.setText("");
        etMRP.setText("");
        etEAN.setText("");
        etTotalQty.setText("");

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


    private void ProceessOBDBoxPicking() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(Outbound, getContext());
            OutboundDTO outbound = new OutboundDTO();
            outbound.setUserId(userId);
            outbound.setEANNumber(etEAN.getText().toString());
            outbound.setTotalScannedQty(etScanQty.getText().toString());
            outbound.setOBDNumber(OBDRefNo);
            outbound.setBoxNO(lblBox.getText().toString());

            message.setEntityObject(outbound);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                call = apiService.CaptureOBDBoxPicking(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureOBDBoxPicking_01", getActivity());
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

                                cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
                                ivScanEAN.setImageResource(R.drawable.invalid_cross);

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _OUTnbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _OUTnbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VLPDResponseDTO dto = null;
                                for (int i = 0; i < _OUTnbound.size(); i++) {
                                    dto = new VLPDResponseDTO(_OUTnbound.get(i).entrySet());
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (!dto.getPreviousPickedItemResponce().get(0).getStatus()) {

                                    cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.scanColor));
                                    ivScanEAN.setImageResource(R.drawable.warning_img);

                                    common.showUserDefinedAlertType(dto.getPreviousPickedItemResponce().get(0).getMessage(), getActivity(), getContext(), "Warning");
                                    return;
                                } else {

                                    cvScanEAN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanEAN.setImageResource(R.drawable.check);

                                    lblScannedSku.setText(dto.getSuggestedItem().get(0).getMcode());
                                    lblDesc.setText(dto.getSuggestedItem().get(0).getDescription());
                                    etReqQty.setText(dto.getSuggestedItem().get(0).getReqQuantity());
                                    etBalQty.setText(dto.getSuggestedItem().get(0).getPendingQuantity());
                                    etTotalQty.setText(dto.getSuggestedItem().get(0).getAvlQuantity());

                                    return;
                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureOBDBoxPicking_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureOBDBoxPicking_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureOBDBoxPicking_04", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_manualpacking));
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