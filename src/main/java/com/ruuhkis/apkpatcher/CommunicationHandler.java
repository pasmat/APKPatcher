package com.ruuhkis.apkpatcher;

import java.io.OutputStream;

/**
 * Created by root on 09/02/2017.
 */
public interface CommunicationHandler {

    public void onInput(String input, OutputStream outputStream);

    public void onError(String error, OutputStream outputStream);

}
