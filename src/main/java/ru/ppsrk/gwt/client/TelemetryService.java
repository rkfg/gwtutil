package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("telemetry")
public interface TelemetryService extends RemoteService {

    void sendTelemetry(String message) throws LogicException, ClientAuthException;

}
