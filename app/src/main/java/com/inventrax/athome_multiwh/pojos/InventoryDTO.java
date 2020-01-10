package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;
import com.inventrax.athome_multiwh.common.constants.EndpointConstants;

import java.util.Map;
import java.util.Set;


public class InventoryDTO {
    @SerializedName("ScanType")
    private EndpointConstants.ScanType scanType;
    @SerializedName("MaterialCode")
    private String materialCode;
    @SerializedName("RSN")
    private String RSN;
    @SerializedName("LocationCode")
    private String locationCode;
    @SerializedName("ContainerCode")
    private String containerCode;
    @SerializedName("ReferenceDocumentNumber")
    private String referenceDocumentNumber;
    @SerializedName("VehicleNumber")
    private String vehicleNumber;
    @SerializedName("OBDNumber")
    private String OBDNumber;
    @SerializedName("OutboundID")
    private int outboundID;
    @SerializedName("MaterialMasterID")
    private int materialMasterID;
    @SerializedName("VehicleID")
    private int vehicleID;
    @SerializedName("ReferenceDocumentID")
    private int referenceDocumentID;
    @SerializedName("ContainerID")
    private int containerID;
    @SerializedName("LocationID")
    private int locationID;
    @SerializedName("MonthOfMfg")
    private String monthOfMfg;
    @SerializedName("YearOfMfg")
    private String yearOfMfg;
    @SerializedName("MaterialTransactionID")
    private int materialTransactionID;
    @SerializedName("Quantity")
    private String Quantity;
    @SerializedName("UserId")
    private String UserId;
    @SerializedName("MOP")
    private String MOP;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("IsFinishedGoods")
    private boolean isFinishedGoods;
    @SerializedName("IsRawMaterial")
    private boolean isRawMaterial;
    @SerializedName("IsConsumables")
    private boolean isConsumables;
    @SerializedName("SLOC")
    private String SLOC;
    @SerializedName("Color")
    private String color;
    @SerializedName("Result")
    private String Result;
    @SerializedName("DocumentProcessedQuantity")
    private String DocumentProcessedQuantity;
    @SerializedName("DocumentQuantity")
    private String DocumentQuantity;
    @SerializedName("MaterialShortDescription")
    private String MaterialShortDescription;
    @SerializedName("IsReceived")
    private boolean IsReceived;
    @SerializedName("UserConfirmedExcessTransaction")
    private boolean UserConfirmedExcessTransaction;
    @SerializedName("UserConfirmReDo")
    private boolean UserConfirmReDo;
    @SerializedName("SuggestionID")
    private String SuggestionID;
    @SerializedName("BatchNumber")
    private String batchNumber;
    @SerializedName("OldMRP")
    private String oldMRP;
    @SerializedName("ToLocationCode")
    private String ToLocationCode;
    @SerializedName("NewRSN")
    private String NewRSN;
    @SerializedName("IsMaterialParent")
    private Boolean isMaterialParent;
    @SerializedName("WareHouseCode")
    private String wareHouseCode;
    @SerializedName("LocationTypeID")
    private String locationTypeID;
    @SerializedName("PalletNumber")
    private String PalletNumber;
    @SerializedName("FromLocation")
    private String fromLocation;
    @SerializedName("FromPallet")
    private String fromPallet;
    @SerializedName("ToPallet")
    private String toPallet;
    @SerializedName("RSNBarcode")
    private String RSNBarcode;
    @SerializedName("EanBarcode")
    private String EanBarcode;
    @SerializedName("TempPallet")
    private String tempPallet;
    @SerializedName("DestPallet")
    private String destPallet;
    @SerializedName("DestBin")
    private String destBin;
    @SerializedName("BoxQty")
    private String boxQty;

    public InventoryDTO() {

    }

    public InventoryDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "RSN":
                    if (entry.getValue() != null) {
                        this.setRSN(entry.getValue().toString());
                    }
                    break;
                case "YearOfMfg":
                    if (entry.getValue() != null) {
                        this.setYearOfMfg(entry.getValue().toString());
                    }
                    break;
                case "MonthOfMfg":
                    if (entry.getValue() != null) {
                        this.setMonthOfMfg(entry.getValue().toString());
                    }
                    break;
                case "MaterialCode":
                    if (entry.getValue() != null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                case "MOP":
                    if (entry.getValue() != null) {
                        this.setMOP(entry.getValue().toString());
                    }
                    break;
                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "ContainerCode":
                    if (entry.getValue() != null) {
                        this.setContainerCode(entry.getValue().toString());
                    }
                    break;
                case "LocationCode":
                    if (entry.getValue() != null) {
                        this.setLocationCode(entry.getValue().toString());
                    }
                    break;
                case "Quantity":
                    if (entry.getValue() != null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "DocumentProcessedQuantity":
                    if (entry.getValue() != null) {
                        this.setDocumentProcessedQuantity(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "DocumentQuantity":
                    if (entry.getValue() != null) {
                        this.setDocumentQuantity(entry.getValue().toString());
                    }
                    break;
                case "MaterialShortDescription":
                    if (entry.getValue() != null) {
                        this.setMaterialShortDescription(entry.getValue().toString());
                    }
                    break;
                case "IsReceived":
                    if (entry.getValue() != null) {
                        this.setReceived(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "UserConfirmedExcessTransaction":
                    if (entry.getValue() != null) {
                        this.setUserConfirmedExcessTransaction(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "Color":
                    if (entry.getValue() != null) {
                        this.setColor(entry.getValue().toString());
                    }
                    break;
                case "UserConfirmReDo":
                    if (entry.getValue() != null) {
                        this.setUserConfirmReDo(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "BatchNumber":
                    if (entry.getValue() != null) {
                        this.setBatchNumber(entry.getValue().toString());
                    }
                    break;
                case "OBDNumber":
                    if (entry.getValue() != null) {
                        this.setOBDNumber(entry.getValue().toString());
                    }
                    break;

                case "IsConsumed":
                    if (entry.getValue() != null) {
                        this.setConsumables(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "IsRawMaterial":
                    if (entry.getValue() != null) {
                        this.setRawMaterial(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "IsFinishedGoods":
                    if (entry.getValue() != null) {
                        this.setFinishedGoods(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "ReferenceDocumentNumber":
                    if (entry.getValue() != null) {
                        this.setReferenceDocumentNumber(entry.getValue().toString());
                    }
                    break;
                case "SuggestionID":
                    if (entry.getValue() != null) {
                        this.setSuggestionID(entry.getValue().toString());
                    }
                    break;
                case "SLOC":
                    if (entry.getValue() != null) {
                        this.setSLOC(entry.getValue().toString());
                    }
                    break;
                case "OldMRP":
                    if (entry.getValue() != null) {
                        this.setOldMRP(entry.getValue().toString());
                    }
                    break;
                case "ToLocationCode":
                    if (entry.getValue() != null) {
                        this.setToLocationCode(entry.getValue().toString());
                    }
                    break;
                case "NewRSN":
                    if (entry.getValue() != null) {
                        this.setNewRSN(entry.getValue().toString());
                    }
                    break;
                case "IsMaterialParent":
                    if (entry.getValue() != null) {
                        this.setMaterialParent(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "WareHouseCode":
                    if (entry.getValue() != null) {
                        this.setWareHouseCode(entry.getValue().toString());
                    }
                    break;
                case "LocationTypeID":
                    if (entry.getValue() != null) {
                        this.setLocationTypeID(entry.getValue().toString());
                    }
                    break;
                case "PalletNumber":
                    if (entry.getValue() != null) {
                        this.setPalletNumber(entry.getValue().toString());
                    }
                    break;
                case "FromPallet":
                    if (entry.getValue() != null) {
                        this.setFromPallet(entry.getValue().toString());
                    }
                    break;
                case "ToPallet":
                    if (entry.getValue() != null) {
                        this.setToPallet(entry.getValue().toString());
                    }
                    break;
                case "RSNBarcode":
                    if (entry.getValue() != null) {
                        this.setRSNBarcode(entry.getValue().toString());
                    }
                    break;
                case "EanBarcode":
                    if (entry.getValue() != null) {
                        this.setEanBarcode(entry.getValue().toString());
                    }
                    break;
                case "TempPallet":
                    if (entry.getValue() != null) {
                        this.setTempPallet(entry.getValue().toString());
                    }
                    break;
                case "DestPallet":
                    if (entry.getValue() != null) {
                        this.setDestPallet(entry.getValue().toString());
                    }
                    break;
                case "DestBin":
                    if (entry.getValue() != null) {
                        this.setDestBin(entry.getValue().toString());
                    }
                    break;
                case "BoxQty":
                    if (entry.getValue() != null) {
                        this.setBoxQty(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getPalletNumber() {
        return PalletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        PalletNumber = palletNumber;
    }

    public Boolean getMaterialParent() {
        return isMaterialParent;
    }

    public void setMaterialParent(Boolean materialParent) {
        isMaterialParent = materialParent;
    }

    public String getNewRSN() {
        return NewRSN;
    }

    public void setNewRSN(String newRSN) {
        NewRSN = newRSN;
    }

    public String getToLocationCode() {
        return ToLocationCode;
    }

    public void setToLocationCode(String toLocationCode) {
        ToLocationCode = toLocationCode;
    }

    public String getOldMRP() {
        return oldMRP;
    }

    public void setOldMRP(String oldMRP) {
        this.oldMRP = oldMRP;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getSuggestionID() {
        return SuggestionID;
    }

    public void setSuggestionID(String suggestionID) {
        SuggestionID = suggestionID;
    }

    public boolean isUserConfirmReDo() {
        return UserConfirmReDo;
    }

    public void setUserConfirmReDo(boolean userConfirmReDo) {
        UserConfirmReDo = userConfirmReDo;
    }

    public boolean isUserConfirmedExcessTransaction() {
        return UserConfirmedExcessTransaction;
    }

    public void setUserConfirmedExcessTransaction(boolean userConfirmedExcessTransaction) {
        UserConfirmedExcessTransaction = userConfirmedExcessTransaction;
    }

    public boolean isReceived() {
        return IsReceived;
    }

    public void setReceived(boolean received) {
        IsReceived = received;
    }

    public String getMaterialShortDescription() {
        return MaterialShortDescription;
    }

    public void setMaterialShortDescription(String materialShortDescription) {
        MaterialShortDescription = materialShortDescription;
    }

    public String getDocumentQuantity() {
        return DocumentQuantity;
    }

    public void setDocumentQuantity(String documentQuantity) {
        DocumentQuantity = documentQuantity;
    }


    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getMOP() {
        return MOP;
    }

    public void setMOP(String MOP) {
        this.MOP = MOP;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getDocumentProcessedQuantity() {
        return DocumentProcessedQuantity;
    }

    public void setDocumentProcessedQuantity(String documentProcessedQuantity) {
        DocumentProcessedQuantity = documentProcessedQuantity;
    }

    public EndpointConstants.ScanType getScanType() {
        return scanType;
    }

    public void setScanType(EndpointConstants.ScanType scanType) {
        this.scanType = scanType;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getRSN() {
        return RSN;
    }

    public void setRSN(String RSN) {
        this.RSN = RSN;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getContainerCode() {
        return containerCode;
    }

    public void setContainerCode(String containerCode) {
        this.containerCode = containerCode;
    }

    public String getReferenceDocumentNumber() {
        return referenceDocumentNumber;
    }

    public void setReferenceDocumentNumber(String referenceDocumentNumber) {
        this.referenceDocumentNumber = referenceDocumentNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getOBDNumber() {
        return OBDNumber;
    }

    public void setOBDNumber(String OBDNumber) {
        this.OBDNumber = OBDNumber;
    }

    public int getOutboundID() {
        return outboundID;
    }

    public void setOutboundID(int outboundID) {
        this.outboundID = outboundID;
    }

    public int getMaterialMasterID() {
        return materialMasterID;
    }

    public void setMaterialMasterID(int materialMasterID) {
        this.materialMasterID = materialMasterID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getReferenceDocumentID() {
        return referenceDocumentID;
    }

    public void setReferenceDocumentID(int referenceDocumentID) {
        this.referenceDocumentID = referenceDocumentID;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public int getContainerID() {
        return containerID;
    }

    public void setContainerID(int containerID) {
        this.containerID = containerID;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public String getMonthOfMfg() {
        return monthOfMfg;
    }

    public void setMonthOfMfg(String monthOfMfg) {
        this.monthOfMfg = monthOfMfg;
    }

    public String getYearOfMfg() {
        return yearOfMfg;
    }

    public void setYearOfMfg(String yearOfMfg) {
        this.yearOfMfg = yearOfMfg;
    }

    public int getMaterialTransactionID() {
        return materialTransactionID;
    }

    public void setMaterialTransactionID(int materialTransactionID) {
        this.materialTransactionID = materialTransactionID;
    }

    public boolean isFinishedGoods() {
        return isFinishedGoods;
    }

    public void setFinishedGoods(boolean finishedGoods) {
        isFinishedGoods = finishedGoods;
    }

    public boolean isRawMaterial() {
        return isRawMaterial;
    }

    public void setRawMaterial(boolean rawMaterial) {
        isRawMaterial = rawMaterial;
    }

    public boolean isConsumables() {
        return isConsumables;
    }

    public void setConsumables(boolean consumables) {
        isConsumables = consumables;

    }
    public String getSLOC() {
        return SLOC;
    }

    public void setSLOC(String SLOC) {
        this.SLOC = SLOC;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getWareHouseCode() {
        return wareHouseCode;
    }

    public void setWareHouseCode(String wareHouseCode) {
        this.wareHouseCode = wareHouseCode;
    }

    public String getLocationTypeID() {
        return locationTypeID;
    }

    public void setLocationTypeID(String locationTypeID) {
        this.locationTypeID = locationTypeID;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getFromPallet() {
        return fromPallet;
    }

    public void setFromPallet(String fromPallet) {
        this.fromPallet = fromPallet;
    }

    public String getToPallet() {
        return toPallet;
    }

    public void setToPallet(String toPallet) {
        this.toPallet = toPallet;
    }

    public String getRSNBarcode() {
        return RSNBarcode;
    }

    public void setRSNBarcode(String RSNBarcode) {
        this.RSNBarcode = RSNBarcode;
    }

    public String getEanBarcode() {
        return EanBarcode;
    }

    public void setEanBarcode(String eanBarcode) {
        EanBarcode = eanBarcode;
    }

    public String getTempPallet() {
        return tempPallet;
    }

    public void setTempPallet(String tempPallet) {
        this.tempPallet = tempPallet;
    }

    public String getDestPallet() {
        return destPallet;
    }

    public void setDestPallet(String destPallet) {
        this.destPallet = destPallet;
    }

    public String getDestBin() {
        return destBin;
    }

    public void setDestBin(String destBin) {
        this.destBin = destBin;
    }

    public String getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(String boxQty) {
        this.boxQty = boxQty;
    }
}