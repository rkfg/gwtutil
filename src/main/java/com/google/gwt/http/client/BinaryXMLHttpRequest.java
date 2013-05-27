package com.google.gwt.http.client;

import com.google.gwt.xhr.client.XMLHttpRequest;

public class BinaryXMLHttpRequest extends XMLHttpRequest {

    public final native void sendAsBinary(String requestData) /*-{
        function byteValue(x) {
            return x.charCodeAt(0) & 0xff;
        }
        var ords = Array.prototype.map.call(requestData, byteValue);
        var ui8a = new Uint8Array(ords);
        this.send(ui8a);
    }-*/;

    protected BinaryXMLHttpRequest() {
        super();
    }

}
