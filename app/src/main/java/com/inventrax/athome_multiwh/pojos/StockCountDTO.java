package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StockCountDTO {


    @SerializedName("ID")
    private String ID;

    @SerializedName("Uniqueserialno")
    private String Uniqueserialno;

    @SerializedName("Location")
    private String Location;

    @SerializedName("Partdesc")
    private String Partdesc;

    @SerializedName("Batchno")
    private String Batchno;


    @SerializedName("BoxQty")
    private String BoxQty;
/*
    @SerializedName("HUSize")
    private int HUSize;*/

    @SerializedName("UoM")
    private String UoM;

  /*  @SerializedName("Length_MM")
    private decimal Length_MM;

    @SerializedName("Breadth_MM")
    private decimal Breadth_MM;

    @SerializedName("Height_MM")
    private decimal Height_MM;

    @SerializedName("Weigth_KG")
    private decimal Weigth_KG;*/

    @SerializedName("SKU")
    private String SKU;

    @SerializedName("ItemType")
    private String ItemType;

    @SerializedName("PalletNumber")
    private String PalletNumber;

    @SerializedName("StorageLocation")
    private String StorageLocation;

   /* @SerializedName("IsFirstTime")
    private bool IsFirstTime;*/

    @SerializedName("Client")
    private String Client;

 /*   @SerializedName("ClientCode")
    private int ClientCode;*/

    @SerializedName("setqty")
    private String setqty;

    /*@SerializedName("IsPalletavailale")
    private bool IsPalletavailale;*/

    @SerializedName("StorageLocations")
    private List<String> StorageLocations;

    @SerializedName("responceInfo")
    private String responceInfo;

  /*  @SerializedName("BoxCount")
    private decimal BoxCount;*/


    @SerializedName("Userid")
    private String Userid;

    @SerializedName("EANnumber")
    private String EANnumber;


    @SerializedName("BinCount")
    private String BinCount;

    @SerializedName("Username")
    private String Username;

    @SerializedName("GreenHouseUID")
    private String GreenHouseUID;


    public StockCountDTO() {

    }

    public StockCountDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ID":
                    if (entry.getValue() != null) {
                        this.setID(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "Batchno":
                    if (entry.getValue() != null) {
                        this.setBatchno(entry.getValue().toString());
                    }
                    break;

                case "BinCount":
                    if (entry.getValue() != null) {
                        this.setBinCount(entry.getValue().toString());
                    }
                    break;

                case "UoM":
                    if (entry.getValue() != null) {
                        this.setUoM(entry.getValue().toString());
                    }
                    break;

                case "Partdesc":
                    if (entry.getValue() != null) {
                        this.setPartdesc(entry.getValue().toString());
                    }
                    break;


                case "SKU":
                    if (entry.getValue() != null) {
                        this.setSKU(entry.getValue().toString());
                    }
                    break;


                case "StorageLocation":
                    if (entry.getValue() != null) {
                        this.setStorageLocation(entry.getValue().toString());
                    }
                    break;

                case "Userid":
                    if (entry.getValue() != null) {
                        this.setUserid(entry.getValue().toString());
                    }
                    break;

                case "EANnumber":
                    if (entry.getValue() != null) {
                        this.setEANnumber(entry.getValue().toString());
                    }
                    break;

                    case "BoxQty":
                    if (entry.getValue() != null) {
                        this.setBoxQty(entry.getValue().toString());
                    }
                    break;


            }
        }
    }


    public String getBoxQty() {
        return BoxQty;
    }

    public void setBoxQty(String boxQty) {
        BoxQty = boxQty;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUniqueserialno() {
        return Uniqueserialno;
    }

    public void setUniqueserialno(String uniqueserialno) {
        Uniqueserialno = uniqueserialno;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getPartdesc() {
        return Partdesc;
    }

    public void setPartdesc(String partdesc) {
        Partdesc = partdesc;
    }

    public String getBatchno() {
        return Batchno;
    }

    public void setBatchno(String batchno) {
        Batchno = batchno;
    }


    public String getUoM() {
        return UoM;
    }

    public void setUoM(String uoM) {
        UoM = uoM;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getItemType() {
        return ItemType;
    }

    public void setItemType(String itemType) {
        ItemType = itemType;
    }

    public String getPalletNumber() {
        return PalletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        PalletNumber = palletNumber;
    }

    public String getStorageLocation() {
        return StorageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        StorageLocation = storageLocation;
    }

    public String getClient() {
        return Client;
    }

    public void setClient(String client) {
        Client = client;
    }


    public String getSetqty() {
        return setqty;
    }

    public void setSetqty(String setqty) {
        this.setqty = setqty;
    }

    public List<String> getStorageLocations() {
        return StorageLocations;
    }

    public void setStorageLocations(List<String> storageLocations) {
        StorageLocations = storageLocations;
    }

    public String getResponceInfo() {
        return responceInfo;
    }

    public void setResponceInfo(String responceInfo) {
        this.responceInfo = responceInfo;
    }

    public String getUserid() {
        return Userid;
    }

    public void setUserid(String userid) {
        Userid = userid;
    }

    public String getEANnumber() {
        return EANnumber;
    }

    public void setEANnumber(String EANnumber) {
        this.EANnumber = EANnumber;
    }

    public String getBinCount() {
        return BinCount;
    }

    public void setBinCount(String binCount) {
        BinCount = binCount;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getGreenHouseUID() {
        return GreenHouseUID;
    }

    public void setGreenHouseUID(String greenHouseUID) {
        GreenHouseUID = greenHouseUID;
    }
}
