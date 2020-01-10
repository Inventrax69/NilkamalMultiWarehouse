package com.inventrax.athome_multiwh.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VLPDResponseDTO {

    @SerializedName("SuggestedItem")
    private List<ItemInfoDTO> suggestedItem ;
    /*  @SerializedName("PreviousPickedItemResponce")
      private List<ExecutionResponseDTO> previousPickedItemResponce ;*/
    @SerializedName("PreviousPickedItemResponce")
    private List<ExecutionResponseDTO> previousPickedItemResponce ;
    @SerializedName("IsNotSuggested")
    private Boolean isSuggested;
    @SerializedName("VlpdID")
    private String VlpdID ;


    public VLPDResponseDTO(){

    }


    public VLPDResponseDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SuggestedItem":
                    if(entry.getValue()!=null) {

                        List<LinkedTreeMap<?,?>> suggestedItemList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ItemInfoDTO> lstSuggestedItem=new ArrayList<ItemInfoDTO>();
                        for(int i=0;i<suggestedItemList.size();i++)
                        {
                            ItemInfoDTO dto=new ItemInfoDTO(suggestedItemList.get(i).entrySet());
                            lstSuggestedItem.add(dto);
                        }

                        this.setSuggestedItem(lstSuggestedItem);
                    }
                    break;

                case "VlpdID":
                    if(entry.getValue()!=null) {
                        this.setVlpdID(entry.getValue().toString());
                    }
                    break;
                case "PreviousPickedItemResponce":
                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?,?>> previousPickedItemResponceList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ExecutionResponseDTO> lstPreviousPickedItemResponse=new ArrayList<ExecutionResponseDTO>();
                        ExecutionResponseDTO dto=null;
                        for(int i=0;i<previousPickedItemResponceList.size();i++)
                        {
                            dto=new ExecutionResponseDTO(previousPickedItemResponceList.get(i).entrySet());
                            lstPreviousPickedItemResponse.add(dto);
                        }

                        this.setPreviousPickedItemResponce(lstPreviousPickedItemResponse);
                    }
                    break;
                case "IsNotSuggested":
                    if (entry.getValue() != null) {
                        this.setSuggested(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
               /* case "PreviousPickedItemResponce":
                    if(entry.getValue()!=null) {


                        this.setPreviousPickedItemResponce(entry.getValue(.toString()));
                    }
                    break;*/
            }
        }
    }

    public Boolean getSuggested() {
        return isSuggested;
    }

    public void setSuggested(Boolean suggested) {
        isSuggested = suggested;
    }

    public List<ItemInfoDTO> getSuggestedItem() {
        return suggestedItem;
    }

    public void setSuggestedItem(List<ItemInfoDTO> suggestedItem) {
        this.suggestedItem = suggestedItem;
    }

    public List<ExecutionResponseDTO> getPreviousPickedItemResponce() {
        return previousPickedItemResponce;
    }

    public void setPreviousPickedItemResponce( List<ExecutionResponseDTO> previousPickedItemResponce) {
        this.previousPickedItemResponce = previousPickedItemResponce;
    }

    public String getVlpdID() {
        return VlpdID;
    }

    public void setVlpdID(String vlpdID) {
        VlpdID = vlpdID;
    }
}