/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a.authentication;

import com.mysql.cj.Messages;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.callback.UsernameCallback;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.util.StringUtils;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

public class AuthenticationKerberosClient
implements AuthenticationPlugin<NativePacketPayload> {
    public static String PLUGIN_NAME = "authentication_kerberos_client";
    private static final String LOGIN_CONFIG_ENTRY = "MySQLConnectorJ";
    private static final String AUTHENTICATION_MECHANISM = "GSSAPI";
    private String sourceOfAuthData = PLUGIN_NAME;
    private MysqlCallbackHandler usernameCallbackHandler = null;
    private String user = null;
    private String password = null;
    private String userPrincipalName = null;
    private Subject subject = null;
    private String cachedPrincipalName = null;
    private CallbackHandler credentialsCallbackHandler = cbs -> {
        for (Callback cb : cbs) {
            if (NameCallback.class.isAssignableFrom(cb.getClass())) {
                ((NameCallback)cb).setName(this.userPrincipalName);
                continue;
            }
            if (PasswordCallback.class.isAssignableFrom(cb.getClass())) {
                ((PasswordCallback)cb).setPassword(this.password == null ? new char[]{} : this.password.toCharArray());
                continue;
            }
            throw new UnsupportedCallbackException(cb, cb.getClass().getName());
        }
    };
    private SaslClient saslClient = null;

    @Override
    public void init(Protocol<NativePacketPayload> prot, MysqlCallbackHandler cbh) {
        this.usernameCallbackHandler = cbh;
    }

    @Override
    public void reset() {
        if (this.saslClient != null) {
            try {
                this.saslClient.dispose();
            }
            catch (SaslException saslException) {
                // empty catch block
            }
        }
        this.user = null;
        this.password = null;
        this.saslClient = null;
    }

    @Override
    public void destroy() {
        this.reset();
        this.usernameCallbackHandler = null;
        this.userPrincipalName = null;
        this.subject = null;
        this.cachedPrincipalName = null;
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
        this.user = user;
        this.password = password;
        if (this.user == null) {
            try {
                this.initializeAuthentication();
                int pos = this.cachedPrincipalName.indexOf(64);
                this.user = pos >= 0 ? this.cachedPrincipalName.substring(0, pos) : this.cachedPrincipalName;
            }
            catch (CJException e) {
                this.user = System.getProperty("user.name");
            }
            if (this.usernameCallbackHandler != null) {
                this.usernameCallbackHandler.handle(new UsernameCallback(this.user));
            }
        }
    }

    @Override
    public void setSourceOfAuthData(String sourceOfAuthData) {
        this.sourceOfAuthData = sourceOfAuthData;
    }

    @Override
    public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
        toServer.clear();
        if (!this.sourceOfAuthData.equals(PLUGIN_NAME) || fromServer.getPayloadLength() == 0) {
            return true;
        }
        if (this.saslClient == null) {
            try {
                int posSlash;
                int servicePrincipalNameLength = (int)fromServer.readInteger(NativeConstants.IntegerDataType.INT2);
                String servicePrincipalName = fromServer.readString(NativeConstants.StringLengthDataType.STRING_VAR, "ASCII", servicePrincipalNameLength);
                String primary = "";
                String instance = "";
                int posAt = servicePrincipalName.indexOf(64);
                if (posAt < 0) {
                    posAt = servicePrincipalName.length();
                }
                if ((posSlash = servicePrincipalName.lastIndexOf(47, posAt)) >= 0) {
                    primary = servicePrincipalName.substring(0, posSlash);
                    instance = servicePrincipalName.substring(posSlash + 1, posAt);
                } else {
                    primary = servicePrincipalName.substring(0, posAt);
                }
                int userPrincipalRealmLength = (int)fromServer.readInteger(NativeConstants.IntegerDataType.INT2);
                String userPrincipalRealm = fromServer.readString(NativeConstants.StringLengthDataType.STRING_VAR, "ASCII", userPrincipalRealmLength);
                this.userPrincipalName = this.user + "@" + userPrincipalRealm;
                this.initializeAuthentication();
                try {
                    String localPrimary = primary;
                    String localInstance = instance;
                    this.saslClient = Subject.doAs(this.subject, () -> Sasl.createSaslClient(new String[]{AUTHENTICATION_MECHANISM}, null, localPrimary, localInstance, null, null));
                }
                catch (PrivilegedActionException e) {
                    throw (SaslException)e.getException();
                }
            }
            catch (SaslException e) {
                throw ExceptionFactory.createException(Messages.getString("AuthenticationKerberosClientPlugin.FailCreateSaslClient", new Object[]{AUTHENTICATION_MECHANISM}), e);
            }
            if (this.saslClient == null) {
                throw ExceptionFactory.createException(Messages.getString("AuthenticationKerberosClientPlugin.FailCreateSaslClient", new Object[]{AUTHENTICATION_MECHANISM}));
            }
        }
        if (!this.saslClient.isComplete()) {
            try {
                Subject.doAs(this.subject, () -> {
                    byte[] response = this.saslClient.evaluateChallenge(fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_EOF));
                    if (response != null) {
                        NativePacketPayload packet = new NativePacketPayload(response);
                        packet.setPosition(0);
                        toServer.add(packet);
                    }
                    return null;
                });
            }
            catch (PrivilegedActionException e) {
                throw ExceptionFactory.createException(Messages.getString("AuthenticationKerberosClientPlugin.ErrProcessingAuthIter", new Object[]{AUTHENTICATION_MECHANISM}), e.getException());
            }
        }
        return true;
    }

    private void initializeAuthentication() {
        if (this.subject != null && this.cachedPrincipalName != null && this.cachedPrincipalName.equals(this.userPrincipalName)) {
            return;
        }
        String loginConfigFile = System.getProperty("java.security.auth.login.config");
        Configuration loginConfig = null;
        if (StringUtils.isNullOrEmpty(loginConfigFile)) {
            final String localUser = this.userPrincipalName;
            final boolean debug = Boolean.getBoolean("sun.security.jgss.debug");
            loginConfig = new Configuration(){

                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    HashMap<String, String> options = new HashMap<String, String>();
                    options.put("useTicketCache", "true");
                    options.put("renewTGT", "false");
                    if (localUser != null) {
                        options.put("principal", localUser);
                    }
                    options.put("debug", Boolean.toString(debug));
                    return new AppConfigurationEntry[]{new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
                }
            };
        }
        try {
            LoginContext loginContext = new LoginContext(LOGIN_CONFIG_ENTRY, null, this.credentialsCallbackHandler, loginConfig);
            loginContext.login();
            this.subject = loginContext.getSubject();
            this.cachedPrincipalName = this.subject.getPrincipals().iterator().next().getName();
        }
        catch (LoginException e) {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationKerberosClientPlugin.FailAuthenticateUser"), e);
        }
    }
}

