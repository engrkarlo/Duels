/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.callback;

import com.mysql.cj.callback.MysqlCallback;
import java.util.ArrayList;
import java.util.List;

public class WebAuthnAuthenticationCallback
implements MysqlCallback {
    private byte[] clientDataHash;
    private String relyingPartyId;
    private byte[] credentialId;
    private boolean supportsCredentialManagement;
    private List<byte[]> authenticatorDataEntries;
    private List<byte[]> signatureEntries;

    public WebAuthnAuthenticationCallback(byte[] clientDataHash, String relyingPartyId, byte[] credentialId) {
        this.clientDataHash = clientDataHash;
        this.relyingPartyId = relyingPartyId;
        this.credentialId = credentialId;
        this.authenticatorDataEntries = new ArrayList<byte[]>();
        this.signatureEntries = new ArrayList<byte[]>();
    }

    public byte[] getClientDataHash() {
        return this.clientDataHash;
    }

    public String getRelyingPartyId() {
        return this.relyingPartyId;
    }

    public byte[] getCredentialId() {
        return this.credentialId;
    }

    public void setSupportsCredentialManagement(boolean supportsCredMan) {
        this.supportsCredentialManagement = supportsCredMan;
    }

    public boolean getSupportsCredentialManagement() {
        return this.supportsCredentialManagement;
    }

    public void addAuthenticatorData(byte[] authenticatorData) {
        this.authenticatorDataEntries.add(authenticatorData);
    }

    public byte[] getAuthenticatorData(int idx) {
        if (idx >= this.authenticatorDataEntries.size()) {
            return null;
        }
        return this.authenticatorDataEntries.get(idx);
    }

    public void addSignature(byte[] signature) {
        this.signatureEntries.add(signature);
    }

    public byte[] getSignature(int idx) {
        if (idx >= this.signatureEntries.size()) {
            return null;
        }
        return this.signatureEntries.get(idx);
    }

    public int getAssertCount() {
        return Math.min(this.authenticatorDataEntries.size(), this.signatureEntries.size());
    }
}

