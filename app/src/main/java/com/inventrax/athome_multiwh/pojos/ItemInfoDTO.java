package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ItemInfoDTO implements Serializable {

    @SerializedName("VlpdAssgnmentId")
    private String vlpdAssgnmentId;
    @SerializedName("MaterialMasterId")
    private String materialMasterId;
    @SerializedName("Mcode")
    private String mcode;
    @SerializedName("Description")
    private String description;
    @SerializedName("Location")
    private String location;
    @SerializedName("LocationId")
    private String locationId;
    @SerializedName("BatchNumber")
    private String batchNumber;
    @SerializedName("MfgDate")
    private String mfgDate;
    @SerializedName("ExpDate")
    private String expDate;
    @SerializedName("HuSize")
    private String HuSize;
    @SerializedName("HuNo")
    private String HuNo;
    @SerializedName("RSN")
    private String RSN;
    @SerializedName("ReqQuantity")
    private String reqQuantity;
    @SerializedName("AvlQuantity")
    private String avlQuantity;
    @SerializedName("UserScannedRSN")
    private String userScannedRSN;
    @SerializedName("UserRequestedQty")
    private String userRequestedQty;
    @SerializedName("EAN")
    private String EAN;
    @SerializedName("PalletNumber")
    private String PalletNumber;
    @SerializedName("RequestType")
    private String requestType;
    @SerializedName("SkipReason")
    private String skipReason;
    @SerializedName("Remarks")
    private String remarks;
    @SerializedName("ClientId")
    private String clientId;
    @SerializedName("Dock")
    private String Dock;
    @SerializedName("RefDoc")
    private String refDoc;
    @SerializedName("VlpdTypeId")
    private String vlpdTypeId;
    @SerializedName("SLocID")
    private String sLocID;
    @SerializedName("Item_SerialNumber")
    private String item_SerialNumber;
    @SerializedName("AvlQuantitySpecified")
    private Boolean avlQuantitySpecified;
    @SerializedName("HuNoSpecified")
    private Boolean huNoSpecified;
    @SerializedName("HuSizeSpecified")
    private Boolean huSizeSpecified;
    @SerializedName("LocationIdSpecified")
    private Boolean locationIdSpecified;
    @SerializedName("MaterialMasterIdSpecified")
    private Boolean materialMasterIdSpecified;
    @SerializedName("ReqQuantitySpecified")
    private Boolean reqQuantitySpecified;
    @SerializedName("UserRequestedQtySpecified")
    private Boolean UserRequestedQtySpecified;
    @SerializedName("PendingQuantity")
    private String pendingQuantity;
    @SerializedName("PendingQtySpecified")
    private String pendingQtySpecified;
    @SerializedName("PickedQuantity")
    private String PickedQuantity;
    @SerializedName("SiteCode")
    private String SiteCode;
    @SerializedName("Result")
    private String result;


    public ItemInfoDTO() {

    }


    public ItemInfoDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "VlpdAssgnmentId":
                    if (entry.getValue() != null) {
                        this.setVlpdAssgnmentId(entry.getValue().toString());
                    }
                    break;
                case "MaterialMasterId":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterId(entry.getValue().toString());
                    }
                    break;
                case "Mcode":
                    if (entry.getValue() != null) {
                        this.setMcode(entry.getValue().toString());
                    }
                    break;
                case "Description":
                    if (entry.getValue() != null) {
                        this.setDescription(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "LocationId":
                    if (entry.getValue() != null) {
                        this.setLocationId(entry.getValue().toString());
                    }
                    break;
                case "BatchNumber":
                    if (entry.getValue() != null) {
                        this.setBatchNumber(entry.getValue().toString());
                    }
                    break;
                case "MfgDate":
                    if (entry.getValue() != null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;

                case "ExpDate":
                    if (entry.getValue() != null) {
                        this.setExpDate(entry.getValue().toString());
                    }
                    break;
                case "HuSize":
                    if (entry.getValue() != null) {
                        this.setHuSize(entry.getValue().toString());
                    }
                    break;
                case "HuNo":
                    if (entry.getValue() != null) {
                        this.setHuNo(entry.getValue().toString());
                    }
                    break;
                case "RSN":
                    if (entry.getValue() != null) {
                        this.setRSN(entry.getValue().toString());
                    }
                    break;
                case "ReqQuantity":
                    if (entry.getValue() != null) {
                        this.setReqQuantity(entry.getValue().toString());
                    }
                    break;
                case "AvlQuantity":
                    if (entry.getValue() != null) {
                        this.setAvlQuantity(entry.getValue().toString());
                    }
                    break;
                case "UserScannedRSN":
                    if (entry.getValue() != null) {
                        this.setUserScannedRSN(entry.getValue().toString());
                    }
                    break;


                case "UserRequestedQty":
                    if (entry.getValue() != null) {
                        this.setUserRequestedQty(entry.getValue().toString());
                    }
                    break;
                case "EAN":
                    if (entry.getValue() != null) {
                        this.setEAN(entry.getValue().toString());
                    }
                    break;
                case "PalletNumber":
                    if (entry.getValue() != null) {
                        this.setPalletNumber(entry.getValue().toString());
                    }
                    break;
                case "RequestType":
                    if (entry.getValue() != null) {
                        this.setRequestType(entry.getValue().toString());
                    }
                    break;
                case "SkipReason":
                    if (entry.getValue() != null) {
                        this.setSkipReason(entry.getValue().toString());
                    }
                    break;
                case "Remarks":
                    if (entry.getValue() != null) {
                        this.setRemarks(entry.getValue().toString());
                    }
                case "ClientId":
                    if (entry.getValue() != null) {
                        this.setClientId(entry.getValue().toString());
                    }
                    break;
                case "Dock":
                    if (entry.getValue() != null) {
                        this.setDock(entry.getValue().toString());
                    }
                    break;
                case "RefDoc":
                    if (entry.getValue() != null) {
                        this.setRefDoc(entry.getValue().toString());
                    }
                case "VlpdTypeId":
                    if (entry.getValue() != null) {
                        this.setVlpdTypeId(entry.getValue().toString());
                    }
                    break;

                case "SLocID":
                    if (entry.getValue() != null) {
                        this.setsLocID(entry.getValue().toString());
                    }
                    break;

                case "Item_SerialNumber":
                    if (entry.getValue() != null) {
                        this.setItem_SerialNumber(entry.getValue().toString());
                    }
                    break;
                case "AvlQuantitySpecified":
                    if (entry.getValue() != null) {
                        this.setAvlQuantitySpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "HuNoSpecified":
                    if (entry.getValue() != null) {
                        this.setHuNoSpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "HuSizeSpecified":
                    if (entry.getValue() != null) {
                        this.setHuNoSpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "LocationIdSpecified":
                    if (entry.getValue() != null) {
                        this.setLocationIdSpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "MaterialMasterIdSpecified":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterIdSpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "ReqQuantitySpecified":
                    if (entry.getValue() != null) {
                        this.setReqQuantitySpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "UserRequestedQtySpecified":
                    if (entry.getValue() != null) {
                        this.setUserRequestedQtySpecified(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "PendingQuantity":
                    if (entry.getValue() != null) {
                        this.setPendingQuantity(entry.getValue().toString());
                    }
                    break;

                case "PendingQtySpecified":
                    if (entry.getValue() != null) {
                        this.setPendingQtySpecified(entry.getValue().toString());
                    }
                    break;

                case "PickedQuantity":
                    if (entry.getValue() != null) {
                        this.setPickedQuantity(entry.getValue().toString());
                    }
                    break;

                case "SiteCode":
                    if (entry.getValue() != null) {
                        this.setSiteCode(entry.getValue().toString());
                    }
                    break;


            }
        }
    }


    public String getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(String pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }

    public String getPendingQtySpecified() {
        return pendingQtySpecified;
    }

    public void setPendingQtySpecified(String pendingQtySpecified) {
        this.pendingQtySpecified = pendingQtySpecified;
    }

    public String getVlpdAssgnmentId() {
        return vlpdAssgnmentId;
    }

    public void setVlpdAssgnmentId(String vlpdAssgnmentId) {
        this.vlpdAssgnmentId = vlpdAssgnmentId;
    }

    public String getMaterialMasterId() {
        return materialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        this.materialMasterId = materialMasterId;
    }

    public String getMcode() {
        return mcode;
    }

    public void setMcode(String mcode) {
        this.mcode = mcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getHuSize() {
        return HuSize;
    }

    public void setHuSize(String huSize) {
        HuSize = huSize;
    }

    public String getHuNo() {
        return HuNo;
    }

    public void setHuNo(String huNo) {
        HuNo = huNo;
    }

    public String getRSN() {
        return RSN;
    }

    public void setRSN(String RSN) {
        this.RSN = RSN;
    }


    public String getUserScannedRSN() {
        return userScannedRSN;
    }

    public void setUserScannedRSN(String userScannedRSN) {
        this.userScannedRSN = userScannedRSN;
    }


    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    public String getPalletNumber() {
        return PalletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        PalletNumber = palletNumber;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDock() {
        return Dock;
    }

    public void setDock(String dock) {
        Dock = dock;
    }

    public String getRefDoc() {
        return refDoc;
    }

    public void setRefDoc(String refDoc) {
        this.refDoc = refDoc;
    }

    public String getVlpdTypeId() {
        return vlpdTypeId;
    }

    public void setVlpdTypeId(String vlpdTypeId) {
        this.vlpdTypeId = vlpdTypeId;
    }

    public String getsLocID() {
        return sLocID;
    }

    public void setsLocID(String sLocID) {
        this.sLocID = sLocID;
    }

    public String getItem_SerialNumber() {
        return item_SerialNumber;
    }

    public void setItem_SerialNumber(String item_SerialNumber) {
        this.item_SerialNumber = item_SerialNumber;
    }

    public Boolean getAvlQuantitySpecified() {
        return avlQuantitySpecified;
    }

    public void setAvlQuantitySpecified(Boolean avlQuantitySpecified) {
        this.avlQuantitySpecified = avlQuantitySpecified;
    }

    public Boolean getHuNoSpecified() {
        return huNoSpecified;
    }

    public void setHuNoSpecified(Boolean huNoSpecified) {
        this.huNoSpecified = huNoSpecified;
    }

    public Boolean getHuSizeSpecified() {
        return huSizeSpecified;
    }

    public void setHuSizeSpecified(Boolean huSizeSpecified) {
        this.huSizeSpecified = huSizeSpecified;
    }

    public Boolean getLocationIdSpecified() {
        return locationIdSpecified;
    }

    public void setLocationIdSpecified(Boolean locationIdSpecified) {
        this.locationIdSpecified = locationIdSpecified;
    }

    public Boolean getMaterialMasterIdSpecified() {
        return materialMasterIdSpecified;
    }

    public void setMaterialMasterIdSpecified(Boolean materialMasterIdSpecified) {
        this.materialMasterIdSpecified = materialMasterIdSpecified;
    }

    public Boolean getReqQuantitySpecified() {
        return reqQuantitySpecified;
    }

    public void setReqQuantitySpecified(Boolean reqQuantitySpecified) {
        this.reqQuantitySpecified = reqQuantitySpecified;
    }

    public Boolean getUserRequestedQtySpecified() {
        return UserRequestedQtySpecified;
    }

    public void setUserRequestedQtySpecified(Boolean userRequestedQtySpecified) {
        UserRequestedQtySpecified = userRequestedQtySpecified;
    }

    public String getReqQuantity() {
        return reqQuantity;
    }

    public void setReqQuantity(String reqQuantity) {
        this.reqQuantity = reqQuantity;
    }

    public String getAvlQuantity() {
        return avlQuantity;
    }

    public void setAvlQuantity(String avlQuantity) {
        this.avlQuantity = avlQuantity;
    }

    public String getUserRequestedQty() {
        return userRequestedQty;
    }

    public void setUserRequestedQty(String userRequestedQty) {
        this.userRequestedQty = userRequestedQty;
    }

    public String getPickedQuantity() {
        return PickedQuantity;
    }

    public void setPickedQuantity(String pickedQuantity) {
        PickedQuantity = pickedQuantity;
    }

    public String getSiteCode() {
        return SiteCode;
    }

    public void setSiteCode(String siteCode) {
        SiteCode = siteCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
