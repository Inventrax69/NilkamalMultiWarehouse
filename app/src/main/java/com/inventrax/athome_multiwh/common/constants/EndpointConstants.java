package com.inventrax.athome_multiwh.common.constants;

/**
 * Created by Prasanna.ch on 05/31/2018.
 */

public enum EndpointConstants {
    // None, LoginUserDTO, ProfileDTO,Inbound,Inventory,Exception,CycleCount,Outbound, DenestingDTO,VLPDDTO,VLPDResponceDTO,VLPDRequestDTO,InternalTransferDTO,ExecutionresponseDTO;
    None, LoginUserDTO, ProfileDTO, InboundDTO, Inbound, Inventory, Exception, CycleCount, Outbound,
    DenestingDTO, VLPDDTO, PutAwayDTO, VLPDResponceDTO, PickPendingItemsDTO, ItemInfoDTO, InventoryDTO,
    OutboundDTO, InternalTransferDTO, VLPDRequestDTO, VLPDLoadingDTO, ExecutionresponseDTO,StockCountDTO,EcomPackingDTO;

    public enum ScanType {Unloading, Putaway, Picking, Loading, DeNesting, Assortment}
    public enum EcomRequestType {EcomLable,RSNWithMRP,RSNWithoutMRP,BulkOrderLable,SAPInvoice,ParentRequest,AmazonASIN,ConfirmLoading}
}