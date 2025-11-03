package com.amazon.apayjiramanagementservice.constants.CloudwatchAlarms;

import lombok.Getter;

@Getter
public enum AlarmProperties {

    CygnusService_ValidateBillPayment_Latency_Alarm("CygnusService_ValidateBillPayment_Latency_Alarm"),
    RechargeCygnus_FetchBill_HighLatency("HFC.RechargeCygnus.FetchBill-HighLatency"),
    HerdWorkStorage_OrderPendingSIL_Alarm("HerdWorkStorage_OrderPendingSIL_Alarm"),
    Order_Success_Rate_Composite_Alarm("Order_Success_Rate_Composite_Alarm");

    private final String name;

    AlarmProperties(String name) {
        this.name = name;
    }
}
