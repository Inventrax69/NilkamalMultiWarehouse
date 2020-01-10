package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 6/11/2018.
 */

public class InboundDTO {
    @SerializedName("InboundID")
    private String inboundID;
    @SerializedName("StoreRefNo")
    private String storeRefNo;
    @SerializedName("SKU ")
    private String SKU ;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("DockLocation")
    private String dockLocation;
    @SerializedName("PalletNo")
    private String palletNo;
    @SerializedName("UserId")
    private String UserId;
    @SerializedName("PutawayLocation")
    private String PutawayLocation;
    @SerializedName("SLOC ")
    private List<StorageLocationDTO> SLOC ;
    @SerializedName("ClientID")
    private String clientID ;
    @SerializedName("Location")
    private String location ;
    @SerializedName("MaterialType")
    private String materialType ;
    @SerializedName("IsSiteToSiteInward")
    private String isSiteToSiteInward ;
    @SerializedName("UniqueRSN")
    private String uniqueRSN ;
    @SerializedName("HUNumber")
    private String HUNumber ;
    @SerializedName("BoxSerialNumber")
    private String BoxSerialNumber ;
    @SerializedName("BatchNo")
    private String batchNo ;
    @SerializedName("SelectedStorageLocation")
    private String selectedStorageLocation ;
    @SerializedName("MaterialCode")
    private String materialCode ;
    @SerializedName("Mdesc")
    private String mDesc ;
    @SerializedName("PoNumber")
    private String poNumber ;
    @SerializedName("HUsize")
    private String HUsize ;
    @SerializedName("BoxQuantity")
    private String boxQuantity ;
    @SerializedName("dimensions")
    private List<DimensionsDTO> dimensionsDTO ;
    @SerializedName("palletinfo")
    private List<PalletInfoDTO> palletInfoDTO ;
    @SerializedName("StackCount")
    private String stackCount ;
    @SerializedName("PrinyQTy")
    private String prinyQty ;
    @SerializedName("IpAddress")
    private String ipAddress ;
    @SerializedName("StackCountSpecified")
    private Boolean stackCountSpecified ;
    @SerializedName("PrinyQTySpecified")
    private Boolean PrinyQtySpecified ;
    @SerializedName("EANNumber")
    private String EANNumber ;
    @SerializedName("InvoiceQuantity")
    private String invoiceQuantity ;
    @SerializedName("PendingQty")
    private String pendingQty ;
    @SerializedName("ScannedInput")
    private String ScannedInput;

    @SerializedName("InputType")
    private String InputType;

    @SerializedName("SuggestedLocation")
    private String SuggestedLocation;

    @SerializedName("BarcodeType")
    private String barcodeType;

    @SerializedName("BoxQty")
    private String boxQty;

    @SerializedName("Result")
    private String result;
    @SerializedName("IsValidStorefno")
    private Boolean isValidStorefno;
    @SerializedName("PalletType")
    private String palletType ;
    @SerializedName("MaterialMasterId")
    private String materialMasterId;
    @SerializedName("ReceivedQty")
    private String receivedQty;
    @SerializedName("ItemSerialNo")
    private String itemSerialNo;

    @SerializedName("Message")
    private String Message;



    @SerializedName("Status")
    private Boolean Status;



    public  InboundDTO()
    {

    }


    public InboundDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "InboundID":
                    if(entry.getValue()!=null) {
                        this.setInboundID(entry.getValue().toString());
                    }
                    break;
                case "Message":
                    if(entry.getValue()!=null) {
                        this.setMessage(entry.getValue().toString());
                    }
                    break;
                case "Status":
                    if(entry.getValue()!=null) {
                        this.setStatus(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "StoreRefNo":
                    if(entry.getValue()!=null) {
                        this.setStoreRefNo(entry.getValue().toString());
                    }
                    break;
                case "SKU":
                    if(entry.getValue()!=null) {
                        this.setSKU(entry.getValue().toString());
                    }
                    break;
                case "SerialNo":
                    if(entry.getValue()!=null) {
                        this.setSerialNo(entry.getValue().toString());
                    }
                    break;
                case "MfgDate":
                    if(entry.getValue()!=null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;
                case "DockLocation":
                    if(entry.getValue()!=null) {
                        this.setDockLocation(entry.getValue().toString());
                    }
                    break;
                case "PalletNo":
                    if(entry.getValue()!=null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if(entry.getValue()!=null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;

                case  "SLOC" :
                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?,?>> SloctreemapList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<StorageLocationDTO> lstSLOC=new ArrayList<StorageLocationDTO>();
                        for(int i=0;i<SloctreemapList.size();i++)
                        {
                            StorageLocationDTO dto=new StorageLocationDTO(SloctreemapList.get(i).entrySet());
                            lstSLOC.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());


                        }

                        this.setSLOC(lstSLOC);
                    }
                    break;

                case   "ClientID":
                    if(entry.getValue()!=null) {
                        this.setClientID(entry.getValue().toString());
                    }
                    break;
                case   "Location":
                    if(entry.getValue()!=null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case   "MaterialType":
                    if(entry.getValue()!=null) {
                        this.setMaterialType(entry.getValue().toString());
                    }
                    break;
                case   "IsSiteToSiteInward":
                    if(entry.getValue()!=null) {
                        this.setIsSiteToSiteInward(entry.getValue().toString());
                    }
                    break;
                case   "UniqueRSN":
                    if(entry.getValue()!=null) {
                        this.setUniqueRSN(entry.getValue().toString());
                    }
                    break;
                case   "HUNumber":
                    if(entry.getValue()!=null) {
                        this.setHUNumber(entry.getValue().toString());
                    }
                    break;
                case   "BoxSerialNumber":
                    if(entry.getValue()!=null) {
                        this.setBoxSerialNumber(entry.getValue().toString());
                    }
                    break;

                case "dimensions":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> dimensionsList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<DimensionsDTO> lstDimensions = new ArrayList<DimensionsDTO>();
                        for (int i = 0; i < dimensionsList.size(); i++) {
                            DimensionsDTO dto = new DimensionsDTO(dimensionsList.get(i).entrySet());
                            lstDimensions.add(dto);
                        }
                        this.setDimensionsDTO(lstDimensions);
                    }
                    break;

                case "palletinfo":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> palletinfoList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<PalletInfoDTO> lstPalletinfo = new ArrayList<PalletInfoDTO>();
                        for (int i = 0; i < palletinfoList.size(); i++) {
                            PalletInfoDTO dto = new PalletInfoDTO(palletinfoList.get(i).entrySet());
                            lstPalletinfo.add(dto);
                        }
                        this.setPalletInfoDTO(lstPalletinfo);
                    }
                    break;
                case   "BatchNo":
                    if(entry.getValue()!=null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case   "SelectedStorageLocation":
                    if(entry.getValue()!=null) {
                        this.setSelectedStorageLocation(entry.getValue().toString());
                    }
                    break;
                case   "MaterialCode":
                    if(entry.getValue()!=null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                case   "Mdesc":
                    if(entry.getValue()!=null) {
                        this.setmDesc(entry.getValue().toString());
                    }
                    break;
                case   "PoNumber":
                    if(entry.getValue()!=null) {
                        this.setPoNumber(entry.getValue().toString());
                    }
                    break;
                case   "HUsize":
                    if(entry.getValue()!=null) {
                        this.setHUsize(entry.getValue().toString());
                    }
                case   "BoxQuantity":
                    if(entry.getValue()!=null) {
                        this.setBoxQuantity(entry.getValue().toString());
                    }
                    break;
                case   "StackCount":
                    if(entry.getValue()!=null) {
                        this.setStackCount(entry.getValue().toString());
                    }
                    break;
                case   "PrinyQTy":
                    if(entry.getValue()!=null) {
                        this.setPrinyQty(entry.getValue().toString());
                    }
                case   "IpAddress":
                    if(entry.getValue()!=null) {
                        this.setIpAddress(entry.getValue().toString());
                    }
                    break;

                case   "StackCountSpecified":
                    if(entry.getValue()!=null) {
                        this.setStackCountSpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case   "PrinyQTySpecified":
                    if(entry.getValue()!=null) {
                        this.setPrinyQtySpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case   "EANNumber":
                    if(entry.getValue()!=null) {
                        this.setEANNumber(entry.getValue().toString());
                    }
                    break;

                case   "InvoiceQuantity":
                    if(entry.getValue()!=null) {
                        this.setInvoiceQuantity(entry.getValue().toString());
                    }
                    break;
                case   "PendingQty":
                    if(entry.getValue()!=null) {
                        this.setPendingQty(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;


                case "ScannedInput":
                    if (entry.getValue() != null) {
                        this.setScannedInput(entry.getValue().toString());
                    }
                    break;

                case "InputType":
                    if (entry.getValue() != null) {
                        this.setInputType(entry.getValue().toString());
                    }
                    break;

                case "SuggestedLocation":
                    if (entry.getValue() != null) {
                        this.setSuggestedLocation(entry.getValue().toString());
                    }
                    break;

                case "BarcodeType":
                    if (entry.getValue() != null) {
                        this.setBarcodeType(entry.getValue().toString());
                    }
                    break;

                case "BoxQty":
                    if (entry.getValue() != null) {
                        this.setBoxQty(entry.getValue().toString());
                    }
                    break;
                case "IsValidStorefno":
                    if (entry.getValue() != null) {
                        this.setValidStorefno(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case   "PalletType":
                    if(entry.getValue()!=null) {
                        this.setPalletType(entry.getValue().toString());
                    }
                    break;
                case "MaterialMasterId":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterId(entry.getValue().toString());
                    }
                    break;
                case "ReceivedQty":
                    if (entry.getValue() != null) {
                        this.setReceivedQty(entry.getValue().toString());
                    }
                    break;
                case "ItemSerialNo":
                    if (entry.getValue() != null) {
                        this.setItemSerialNo(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getItemSerialNo() {
        return itemSerialNo;
    }

    public void setItemSerialNo(String itemSerialNo) {
        this.itemSerialNo = itemSerialNo;
    }

    public String getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(String receivedQty) {
        this.receivedQty = receivedQty;
    }

    public String getMaterialMasterId() {
        return materialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        this.materialMasterId = materialMasterId;
    }

    public String getPalletType() {
        return palletType;
    }

    public void setPalletType(String palletType) {
        this.palletType = palletType;
    }

    public Boolean getValidStorefno() {
        return isValidStorefno;
    }

    public void setValidStorefno(Boolean validStorefno) {
        isValidStorefno = validStorefno;
    }

    public String getScannedInput() {
        return ScannedInput;
    }

    public void setScannedInput(String scannedInput) {
        ScannedInput = scannedInput;
    }

    public String getInputType() {
        return InputType;
    }

    public void setInputType(String inputType) {
        InputType = inputType;
    }

    public String getSuggestedLocation() {
        return SuggestedLocation;
    }

    public void setSuggestedLocation(String suggestedLocation) {
        SuggestedLocation = suggestedLocation;
    }

    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public String getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(String boxQty) {
        this.boxQty = boxQty;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEANNumber() {
        return EANNumber;
    }

    public void setEANNumber(String EANNumber) {
        this.EANNumber = EANNumber;
    }

    public String getInvoiceQuantity() {
        return invoiceQuantity;
    }

    public void setInvoiceQuantity(String invoiceQuantity) {
        this.invoiceQuantity = invoiceQuantity;
    }

    public String getPendingQty() {
        return pendingQty;
    }

    public void setPendingQty(String pendingQty) {
        this.pendingQty = pendingQty;
    }

    public String getStackCount() {
        return stackCount;
    }

    public void setStackCount(String stackCount) {
        this.stackCount = stackCount;
    }

    public String getPrinyQty() {
        return prinyQty;
    }

    public void setPrinyQty(String prinyQty) {
        this.prinyQty = prinyQty;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Boolean getStackCountSpecified() {
        return stackCountSpecified;
    }

    public void setStackCountSpecified(Boolean stackCountSpecified) {
        this.stackCountSpecified = stackCountSpecified;
    }

    public Boolean getPrinyQtySpecified() {
        return PrinyQtySpecified;
    }

    public void setPrinyQtySpecified(Boolean prinyQtySpecified) {
        PrinyQtySpecified = prinyQtySpecified;
    }

    public String getInboundID() {
        return inboundID;
    }

    public void setInboundID(String inboundID) {
        this.inboundID = inboundID;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPutawayLocation() {
        return PutawayLocation;
    }

    public void setPutawayLocation(String putawayLocation) {
        PutawayLocation = putawayLocation;
    }

    public String getStoreRefNo() {
        return storeRefNo;
    }

    public void setStoreRefNo(String storeRefNo) {
        this.storeRefNo = storeRefNo;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getDockLocation() {
        return dockLocation;
    }

    public void setDockLocation(String dockLocation) {
        this.dockLocation = dockLocation;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public List<StorageLocationDTO> getSLOC() {
        return SLOC;
    }

    public void setSLOC(List<StorageLocationDTO> SLOC) {
        this.SLOC = SLOC;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getIsSiteToSiteInward() {
        return isSiteToSiteInward;
    }

    public void setIsSiteToSiteInward(String isSiteToSiteInward) {
        this.isSiteToSiteInward = isSiteToSiteInward;
    }

    public String getUniqueRSN() {
        return uniqueRSN;
    }

    public void setUniqueRSN(String uniqueRSN) {
        this.uniqueRSN = uniqueRSN;
    }

    public String getHUNumber() {
        return HUNumber;
    }

    public void setHUNumber(String HUNumber) {
        this.HUNumber = HUNumber;
    }

    public String getBoxSerialNumber() {
        return BoxSerialNumber;
    }

    public void setBoxSerialNumber(String boxSerialNumber) {
        BoxSerialNumber = boxSerialNumber;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getSelectedStorageLocation() {
        return selectedStorageLocation;
    }

    public void setSelectedStorageLocation(String selectedStorageLocation) {
        this.selectedStorageLocation = selectedStorageLocation;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getmDesc() {
        return mDesc;
    }

    public void setmDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getHUsize() {
        return HUsize;
    }

    public void setHUsize(String HUsize) {
        this.HUsize = HUsize;
    }

    public String getBoxQuantity() {
        return boxQuantity;
    }

    public void setBoxQuantity(String boxQuantity) {
        this.boxQuantity = boxQuantity;
    }

    public List<DimensionsDTO> getDimensionsDTO() {
        return dimensionsDTO;
    }

    public void setDimensionsDTO(List<DimensionsDTO> dimensionsDTO) {
        this.dimensionsDTO = dimensionsDTO;
    }

    public List<PalletInfoDTO> getPalletInfoDTO() {
        return palletInfoDTO;
    }

    public void setPalletInfoDTO(List<PalletInfoDTO> palletInfoDTO) {
        this.palletInfoDTO = palletInfoDTO;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }
}