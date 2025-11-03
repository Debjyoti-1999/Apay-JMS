package com.amazon.apayjiramanagementservice.handler.alarmHandler;

import com.amazon.apayjiramanagementservice.constants.CloudwatchAlarms.AlarmProperties;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmHandler.ServiceAlarmHandler;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl.FetchBillHighLatencyHandler;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl.HerdWorkStorageOrderPendingSILHandler;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl.OrderSuccessRateHandler;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl.ValidateBillPaymentLatencyHandler;
import java.util.HashMap;
import java.util.Map;

public final class AlarmHandlerFactory {

    private static final Map<AlarmProperties, ServiceAlarmHandler> ALARM_HANDLERS = new HashMap<>();

    private static AlarmHandlerFactory instance;

    private AlarmHandlerFactory() {

    }

    public static AlarmHandlerFactory getInstance() {
        synchronized (AlarmHandlerFactory.class) {
            if (instance == null) {
                instance = new AlarmHandlerFactory();
            }
        }
        return instance;
    }


    public ServiceAlarmHandler getServiceAlarmHandler(AlarmProperties alarmProperties) {
        return ALARM_HANDLERS.computeIfAbsent(alarmProperties, this::registerHandler);
    }


    public ServiceAlarmHandler registerHandler(AlarmProperties alarmProperties) {
        if (alarmProperties.equals(AlarmProperties.CygnusService_ValidateBillPayment_Latency_Alarm)) {
            return ValidateBillPaymentLatencyHandler.getInstance();
        }
        if (alarmProperties.equals(AlarmProperties.RechargeCygnus_FetchBill_HighLatency)) {
            return FetchBillHighLatencyHandler.getInstance();
        }
        if (alarmProperties.equals(AlarmProperties.HerdWorkStorage_OrderPendingSIL_Alarm)) {
            return HerdWorkStorageOrderPendingSILHandler.getInstance();
        }
        if (alarmProperties.equals(AlarmProperties.Order_Success_Rate_Composite_Alarm)) {
            return OrderSuccessRateHandler.getInstance();
        }
        return null;
    }


}
