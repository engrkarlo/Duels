/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.Messages;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.callback.UsernameCallback;
import com.mysql.cj.callback.WebAuthnAuthenticationCallback;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.Util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class AuthenticationWebAuthnClient
implements AuthenticationPlugin<NativePacketPayload> {
    public static String PLUGIN_NAME = "authentication_webauthn_client";
    private static final String CLIENT_DATA_JSON = "{\"type\":\"webauthn.get\",\"challenge\":\"%s\",\"origin\":\"https://%s\",\"crossOrigin\":false }";
    private String sourceOfAuthData = PLUGIN_NAME;
    private AuthStage stage = AuthStage.INITIAL_DATA;
    private byte[] challenge = null;
    private String relyingPartyId = null;
    private String clientDataJson = null;
    private byte[] clientDataHash = null;
    private byte[] credentialId = null;
    private MysqlCallbackHandler usernameCallbackHandler = null;
    private MysqlCallbackHandler webAuthnAuthenticationCallbackHandler = null;
    private WebAuthnAuthenticationCallback webAuthnAuthCallback = null;

    @Override
    public void init(Protocol<NativePacketPayload> protocol, MysqlCallbackHandler callbackHandler) {
        this.usernameCallbackHandler = callbackHandler;
        String webAuthnCallbackHandlerClassName = protocol.getPropertySet().getStringProperty(PropertyKey.authenticationWebAuthnCallbackHandler).getValue();
        if (webAuthnCallbackHandlerClassName == null) {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.MissingCallbackHandler"));
        }
        this.webAuthnAuthenticationCallbackHandler = Util.getInstance(MysqlCallbackHandler.class, webAuthnCallbackHandlerClassName, null, null, protocol.getExceptionInterceptor());
    }

    @Override
    public void reset() {
        this.stage = AuthStage.INITIAL_DATA;
        this.challenge = null;
        this.relyingPartyId = null;
        this.clientDataJson = null;
        this.clientDataHash = null;
        this.credentialId = null;
    }

    @Override
    public void destroy() {
        this.reset();
        this.usernameCallbackHandler = null;
        this.webAuthnAuthenticationCallbackHandler = null;
        this.webAuthnAuthCallback = null;
    }

    @Override
    public String getProtocolPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean requiresConfidentiality() {
        return false;
    }

    @Override
    public boolean isReusable() {
        return false;
    }

    @Override
    public void setAuthenticationParameters(String user, String password) {
        if (user == null && this.usernameCallbackHandler != null) {
            this.usernameCallbackHandler.handle(new UsernameCallback(System.getProperty("user.name")));
        }
    }

    @Override
    public void setSourceOfAuthData(String sourceOfAuthData) {
        this.sourceOfAuthData = sourceOfAuthData;
    }

    @Override
    public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
        toServer.clear();
        if (!this.sourceOfAuthData.equals(PLUGIN_NAME)) {
            return true;
        }
        switch (this.stage) {
            case INITIAL_DATA: {
                MessageDigest digest;
                if (fromServer.getPayloadLength() == 0) {
                    throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.IncompleteRegistration"));
                }
                fromServer.readInteger(NativeConstants.IntegerDataType.INT1);
                this.challenge = fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC);
                this.relyingPartyId = fromServer.readString(NativeConstants.StringSelfDataType.STRING_LENENC, "UTF-8");
                Base64.Encoder b64Encoder = Base64.getUrlEncoder().withoutPadding();
                String b64EncodedChallenge = b64Encoder.encodeToString(this.challenge);
                this.clientDataJson = String.format(CLIENT_DATA_JSON, b64EncodedChallenge, this.relyingPartyId);
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                }
                catch (NoSuchAlgorithmException e) {
                    throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.FaileMessageDigestSha256"), e);
                }
                this.clientDataHash = digest.digest(this.clientDataJson.getBytes(StandardCharsets.UTF_8));
                NativePacketPayload packet = new NativePacketPayload(new byte[]{1});
                toServer.add(packet);
                this.stage = AuthStage.CREDENTIAL_ID;
                break;
            }
            case CREDENTIAL_ID: {
                this.credentialId = fromServer.getPayloadLength() > 0 ? fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC) : new byte[]{};
                this.webAuthnAuthCallback = new WebAuthnAuthenticationCallback(this.clientDataHash, this.relyingPartyId, this.credentialId);
                this.webAuthnAuthenticationCallbackHandler.handle(this.webAuthnAuthCallback);
                int assertionsCount = this.webAuthnAuthCallback.getAssertCount();
                int authenticatorDataLength = 0;
                int signaturesLength = 0;
                for (int i = 0; i < assertionsCount; ++i) {
                    authenticatorDataLength += this.webAuthnAuthCallback.getAuthenticatorData(i).length;
                    signaturesLength += this.webAuthnAuthCallback.getSignature(i).length;
                }
                int packetLen = 1;
                ++packetLen;
                packetLen += authenticatorDataLength + signaturesLength + 2 * assertionsCount;
                NativePacketPayload packet = new NativePacketPayload(packetLen += this.clientDataJson.length() + 1);
                packet.writeInteger(NativeConstants.IntegerDataType.INT1, 2L);
                packet.writeInteger(NativeConstants.IntegerDataType.INT_LENENC, assertionsCount);
                for (int i = 0; i < assertionsCount; ++i) {
                    byte[] authenticatorData = this.webAuthnAuthCallback.getAuthenticatorData(i);
                    if (authenticatorData == null || authenticatorData.length == 0) {
                        throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.InvalidAuthenticatorData"));
                    }
                    packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, authenticatorData);
                    byte[] signature = this.webAuthnAuthCallback.getSignature(i);
                    if (signature == null || signature.length == 0) {
                        throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.InvalidSignature"));
                    }
                    packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, signature);
                }
                packet.writeBytes(NativeConstants.StringSelfDataType.STRING_LENENC, this.clientDataJson.getBytes(StandardCharsets.UTF_8));
                toServer.add(packet);
                this.stage = AuthStage.FINISHED;
                break;
            }
            case FINISHED: {
                throw ExceptionFactory.createException(Messages.getString("AuthenticationWebAuthnClientPlugin.AuthenticationFactorComplete"));
            }
        }
        return true;
    }

    private static enum AuthStage {
        INITIAL_DATA,
        CREDENTIAL_ID,
        FINISHED;

    }
}

