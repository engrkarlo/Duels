/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.protocol.a;

import com.mysql.cj.protocol.ServerSessionStateController;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class NativeServerSessionStateController
implements ServerSessionStateController {
    private NativeServerSessionStateChanges sessionStateChanges;
    private List<WeakReference<ServerSessionStateController.SessionStateChangesListener>> listeners;

    @Override
    public void setSessionStateChanges(ServerSessionStateController.ServerSessionStateChanges changes) {
        this.sessionStateChanges = (NativeServerSessionStateChanges)changes;
        if (this.listeners != null) {
            for (WeakReference<ServerSessionStateController.SessionStateChangesListener> wr : this.listeners) {
                ServerSessionStateController.SessionStateChangesListener l = (ServerSessionStateController.SessionStateChangesListener)wr.get();
                if (l != null) {
                    l.handleSessionStateChanges(changes);
                    continue;
                }
                this.listeners.remove(wr);
            }
        }
    }

    @Override
    public NativeServerSessionStateChanges getSessionStateChanges() {
        return this.sessionStateChanges;
    }

    @Override
    public void addSessionStateChangesListener(ServerSessionStateController.SessionStateChangesListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<WeakReference<ServerSessionStateController.SessionStateChangesListener>>();
        }
        for (WeakReference<ServerSessionStateController.SessionStateChangesListener> wr : this.listeners) {
            if (!l.equals(wr.get())) continue;
            return;
        }
        this.listeners.add(new WeakReference<ServerSessionStateController.SessionStateChangesListener>(l));
    }

    @Override
    public void removeSessionStateChangesListener(ServerSessionStateController.SessionStateChangesListener listener) {
        if (this.listeners != null) {
            for (WeakReference<ServerSessionStateController.SessionStateChangesListener> wr : this.listeners) {
                ServerSessionStateController.SessionStateChangesListener l = (ServerSessionStateController.SessionStateChangesListener)wr.get();
                if (l != null && !l.equals(listener)) continue;
                this.listeners.remove(wr);
                break;
            }
        }
    }

    public static class NativeServerSessionStateChanges
    implements ServerSessionStateController.ServerSessionStateChanges {
        private List<ServerSessionStateController.SessionStateChange> sessionStateChanges = new ArrayList<ServerSessionStateController.SessionStateChange>();

        @Override
        public List<ServerSessionStateController.SessionStateChange> getSessionStateChangesList() {
            return this.sessionStateChanges;
        }

        public NativeServerSessionStateChanges init(NativePacketPayload buf, String encoding) {
            int totalLen = (int)buf.readInteger(NativeConstants.IntegerDataType.INT_LENENC);
            int start = buf.getPosition();
            int end = start + totalLen;
            while (totalLen > 0 && end > start) {
                int type = (int)buf.readInteger(NativeConstants.IntegerDataType.INT1);
                NativePacketPayload b = new NativePacketPayload(buf.readBytes(NativeConstants.StringSelfDataType.STRING_LENENC));
                switch (type) {
                    case 0: {
                        this.sessionStateChanges.add(new ServerSessionStateController.SessionStateChange(type).addValue(b.readString(NativeConstants.StringSelfDataType.STRING_LENENC, encoding)).addValue(b.readString(NativeConstants.StringSelfDataType.STRING_LENENC, encoding)));
                        break;
                    }
                    case 1: 
                    case 4: 
                    case 5: {
                        this.sessionStateChanges.add(new ServerSessionStateController.SessionStateChange(type).addValue(b.readString(NativeConstants.StringSelfDataType.STRING_LENENC, encoding)));
                        break;
                    }
                    case 3: {
                        b.readInteger(NativeConstants.IntegerDataType.INT1);
                        this.sessionStateChanges.add(new ServerSessionStateController.SessionStateChange(type).addValue(b.readString(NativeConstants.StringSelfDataType.STRING_LENENC, encoding)));
                        break;
                    }
                    default: {
                        this.sessionStateChanges.add(new ServerSessionStateController.SessionStateChange(type).addValue(b.readString(NativeConstants.StringLengthDataType.STRING_FIXED, encoding, b.getPayloadLength())));
                    }
                }
                start = buf.getPosition();
            }
            return this;
        }
    }
}

