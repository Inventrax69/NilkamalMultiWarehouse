package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class PickPendingItemDTO {

    @SerializedName("Mcode")
    private String mCode;
    @SerializedName("Description")
    private String description;
    @SerializedName("HUSize")
    private String HUSize;
    @SerializedName("HUNo")
    private String HUNo;
    @SerializedName("Quantity")
    private String quantity;
    @SerializedName("Location")
    private String location;
    @SerializedName("BatchNumber")
    private String batchNumber;
    @SerializedName("DispatchNumber")
    private String dispatchNumber;
    @SerializedName("VlpdId")
    private String vlpdId;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("IsNew")
    private String isNew;



    public PickPendingItemDTO(){

    }

    public PickPendingItemDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "Mcode":
                    if (entry.getValue() != null) {
                        this.setmCode(entry.getValue().toString());
                    }
                    break;
                case "Description":
                    if (entry.getValue() != null) {
                        this.setDescription(entry.getValue().toString());
                    }
                    break;
                case "HUSize":
                    if (entry.getValue() != null) {
                        this.setHUSize(entry.getValue().toString());
                    }
                    break;
                case "HUNo":
                    if (entry.getValue() != null) {
                        this.setHUNo(entry.getValue().toString());
                    }
                    break;
                case "Quantity":
                    if (entry.getValue() != null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "BatchNumber":
                    if (entry.getValue() != null) {
                        this.setBatchNumber(entry.getValue().toString());
                    }
                    break;
                case "DispatchNumber":
                    if (entry.getValue() != null) {
                        this.setDispatchNumber(entry.getValue().toString());
                    }
                    break;
                case "VlpdId":
                    if (entry.getValue() != null) {
                        this.setVlpdId(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "IsNew":
                    if (entry.getValue() != null) {
                        this.setIsNew(entry.getValue().toString());
                    }
                    break;

            }
        }
    }


    public String getmCode() {
        return mCode;
    }

    public void setmCode(String mCode) {
        this.mCode = mCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHUSize() {
        return HUSize;
    }

    public void setHUSize(String HUSize) {
        this.HUSize = HUSize;
    }

    public String getHUNo() {
        return HUNo;
    }

    public void setHUNo(String HUNo) {
        this.HUNo = HUNo;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getDispatchNumber() {
        return dispatchNumber;
    }

    public void setDispatchNumber(String dispatchNumber) {
        this.dispatchNumber = dispatchNumber;
    }

    public String getVlpdId() {
        return vlpdId;
    }

    public void setVlpdId(String vlpdId) {
        this.vlpdId = vlpdId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }
}
