package ru.ppsrk.gwt.server;

import static ru.ppsrk.gwt.server.AuthServiceImpl.*;

import java.util.Date;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.TelemetryService;
import ru.ppsrk.gwt.domain.Telemetry;

public class TelemetryServiceImpl extends RemoteServiceServlet implements TelemetryService {

    /**
     * 
     */
    private static final long serialVersionUID = 5624357483007462126L;

    @Override
    public void sendTelemetry(String message) throws GwtUtilException {
        Telemetry telemetry = new Telemetry();
        telemetry.setUser(requiresAuthUser());
        telemetry.setDate(new Date());
        telemetry.setMessage(message);
        HibernateUtil.saveObject(telemetry);
    }

}
