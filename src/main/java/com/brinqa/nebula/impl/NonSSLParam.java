package com.brinqa.nebula.impl;

import com.vesoft.nebula.client.graph.data.SSLParam;

public class NonSSLParam extends SSLParam {

    public NonSSLParam() {
        super(SignMode.NONE);
    }
}
