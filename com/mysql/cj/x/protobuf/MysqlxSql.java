/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.cj.x.protobuf;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.RepeatedFieldBuilderV3;
import com.google.protobuf.UninitializedMessageException;
import com.google.protobuf.UnknownFieldSet;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MysqlxSql {
    private static final Descriptors.Descriptor internal_static_Mysqlx_Sql_StmtExecute_descriptor;
    private static final GeneratedMessageV3.FieldAccessorTable internal_static_Mysqlx_Sql_StmtExecute_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor;
    private static final GeneratedMessageV3.FieldAccessorTable internal_static_Mysqlx_Sql_StmtExecuteOk_fieldAccessorTable;
    private static Descriptors.FileDescriptor descriptor;

    private MysqlxSql() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(ExtensionRegistry registry) {
        MysqlxSql.registerAllExtensions((ExtensionRegistryLite)registry);
    }

    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    static {
        String[] descriptorData = new String[]{"\n\u0010mysqlx_sql.proto\u0012\nMysqlx.Sql\u001a\fmysqlx.proto\u001a\u0016mysqlx_datatypes.proto\"\u007f\n\u000bStmtExecute\u0012\u0016\n\tnamespace\u0018\u0003 \u0001(\t:\u0003sql\u0012\f\n\u0004stmt\u0018\u0001 \u0002(\f\u0012#\n\u0004args\u0018\u0002 \u0003(\u000b2\u0015.Mysqlx.Datatypes.Any\u0012\u001f\n\u0010compact_metadata\u0018\u0004 \u0001(\b:\u0005false:\u0004\u0088\u00ea0\f\"\u0015\n\rStmtExecuteOk:\u0004\u0090\u00ea0\u0011B\u0019\n\u0017com.mysql.cj.x.protobuf"};
        descriptor = Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[]{Mysqlx.getDescriptor(), MysqlxDatatypes.getDescriptor()});
        internal_static_Mysqlx_Sql_StmtExecute_descriptor = MysqlxSql.getDescriptor().getMessageTypes().get(0);
        internal_static_Mysqlx_Sql_StmtExecute_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(internal_static_Mysqlx_Sql_StmtExecute_descriptor, new String[]{"Namespace", "Stmt", "Args", "CompactMetadata"});
        internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor = MysqlxSql.getDescriptor().getMessageTypes().get(1);
        internal_static_Mysqlx_Sql_StmtExecuteOk_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor, new String[0]);
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(Mysqlx.clientMessageId);
        registry.add(Mysqlx.serverMessageId);
        Descriptors.FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
        Mysqlx.getDescriptor();
        MysqlxDatatypes.getDescriptor();
    }

    public static final class StmtExecuteOk
    extends GeneratedMessageV3
    implements StmtExecuteOkOrBuilder {
        private static final long serialVersionUID = 0L;
        private byte memoizedIsInitialized = (byte)-1;
        private static final StmtExecuteOk DEFAULT_INSTANCE = new StmtExecuteOk();
        @Deprecated
        public static final Parser<StmtExecuteOk> PARSER = new AbstractParser<StmtExecuteOk>(){

            @Override
            public StmtExecuteOk parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                Builder builder = StmtExecuteOk.newBuilder();
                try {
                    builder.mergeFrom(input, extensionRegistry);
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(builder.buildPartial());
                }
                catch (UninitializedMessageException e) {
                    throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                }
                catch (IOException e) {
                    throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                }
                return builder.buildPartial();
            }
        };

        private StmtExecuteOk(GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private StmtExecuteOk() {
        }

        @Override
        protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
            return new StmtExecuteOk();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor;
        }

        @Override
        protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_Mysqlx_Sql_StmtExecuteOk_fieldAccessorTable.ensureFieldAccessorsInitialized(StmtExecuteOk.class, Builder.class);
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof StmtExecuteOk)) {
                return super.equals(obj);
            }
            StmtExecuteOk other = (StmtExecuteOk)obj;
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + StmtExecuteOk.getDescriptor().hashCode();
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static StmtExecuteOk parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecuteOk parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecuteOk parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecuteOk parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecuteOk parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecuteOk parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecuteOk parseFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static StmtExecuteOk parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static StmtExecuteOk parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
        }

        public static StmtExecuteOk parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static StmtExecuteOk parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static StmtExecuteOk parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return StmtExecuteOk.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(StmtExecuteOk prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static StmtExecuteOk getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<StmtExecuteOk> parser() {
            return PARSER;
        }

        public Parser<StmtExecuteOk> getParserForType() {
            return PARSER;
        }

        @Override
        public StmtExecuteOk getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        public static final class Builder
        extends GeneratedMessageV3.Builder<Builder>
        implements StmtExecuteOkOrBuilder {
            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor;
            }

            @Override
            protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_Mysqlx_Sql_StmtExecuteOk_fieldAccessorTable.ensureFieldAccessorsInitialized(StmtExecuteOk.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(GeneratedMessageV3.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_Mysqlx_Sql_StmtExecuteOk_descriptor;
            }

            @Override
            public StmtExecuteOk getDefaultInstanceForType() {
                return StmtExecuteOk.getDefaultInstance();
            }

            @Override
            public StmtExecuteOk build() {
                StmtExecuteOk result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public StmtExecuteOk buildPartial() {
                StmtExecuteOk result = new StmtExecuteOk(this);
                this.onBuilt();
                return result;
            }

            @Override
            public Builder clone() {
                return (Builder)super.clone();
            }

            @Override
            public Builder setField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder)super.setField(field, value);
            }

            @Override
            public Builder clearField(Descriptors.FieldDescriptor field) {
                return (Builder)super.clearField(field);
            }

            @Override
            public Builder clearOneof(Descriptors.OneofDescriptor oneof) {
                return (Builder)super.clearOneof(oneof);
            }

            @Override
            public Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
                return (Builder)super.setRepeatedField(field, index, value);
            }

            @Override
            public Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder)super.addRepeatedField(field, value);
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof StmtExecuteOk) {
                    return this.mergeFrom((StmtExecuteOk)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(StmtExecuteOk other) {
                if (other == StmtExecuteOk.getDefaultInstance()) {
                    return this;
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block8: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block8;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public final Builder setUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder)super.setUnknownFields(unknownFields);
            }

            @Override
            public final Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder)super.mergeUnknownFields(unknownFields);
            }
        }
    }

    public static interface StmtExecuteOkOrBuilder
    extends MessageOrBuilder {
    }

    public static final class StmtExecute
    extends GeneratedMessageV3
    implements StmtExecuteOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int NAMESPACE_FIELD_NUMBER = 3;
        private volatile Object namespace_ = "sql";
        public static final int STMT_FIELD_NUMBER = 1;
        private ByteString stmt_ = ByteString.EMPTY;
        public static final int ARGS_FIELD_NUMBER = 2;
        private List<MysqlxDatatypes.Any> args_;
        public static final int COMPACT_METADATA_FIELD_NUMBER = 4;
        private boolean compactMetadata_ = false;
        private byte memoizedIsInitialized = (byte)-1;
        private static final StmtExecute DEFAULT_INSTANCE = new StmtExecute();
        @Deprecated
        public static final Parser<StmtExecute> PARSER = new AbstractParser<StmtExecute>(){

            @Override
            public StmtExecute parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                Builder builder = StmtExecute.newBuilder();
                try {
                    builder.mergeFrom(input, extensionRegistry);
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.setUnfinishedMessage(builder.buildPartial());
                }
                catch (UninitializedMessageException e) {
                    throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                }
                catch (IOException e) {
                    throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                }
                return builder.buildPartial();
            }
        };

        private StmtExecute(GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private StmtExecute() {
            this.namespace_ = "sql";
            this.stmt_ = ByteString.EMPTY;
            this.args_ = Collections.emptyList();
        }

        @Override
        protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
            return new StmtExecute();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_Mysqlx_Sql_StmtExecute_descriptor;
        }

        @Override
        protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_Mysqlx_Sql_StmtExecute_fieldAccessorTable.ensureFieldAccessorsInitialized(StmtExecute.class, Builder.class);
        }

        @Override
        public boolean hasNamespace() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public String getNamespace() {
            Object ref = this.namespace_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.namespace_ = s;
            }
            return s;
        }

        @Override
        public ByteString getNamespaceBytes() {
            Object ref = this.namespace_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.namespace_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public boolean hasStmt() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public ByteString getStmt() {
            return this.stmt_;
        }

        @Override
        public List<MysqlxDatatypes.Any> getArgsList() {
            return this.args_;
        }

        @Override
        public List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList() {
            return this.args_;
        }

        @Override
        public int getArgsCount() {
            return this.args_.size();
        }

        @Override
        public MysqlxDatatypes.Any getArgs(int index) {
            return this.args_.get(index);
        }

        @Override
        public MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int index) {
            return this.args_.get(index);
        }

        @Override
        public boolean hasCompactMetadata() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public boolean getCompactMetadata() {
            return this.compactMetadata_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            if (!this.hasStmt()) {
                this.memoizedIsInitialized = 0;
                return false;
            }
            for (int i = 0; i < this.getArgsCount(); ++i) {
                if (this.getArgs(i).isInitialized()) continue;
                this.memoizedIsInitialized = 0;
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 2) != 0) {
                output.writeBytes(1, this.stmt_);
            }
            for (int i = 0; i < this.args_.size(); ++i) {
                output.writeMessage(2, this.args_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                GeneratedMessageV3.writeString(output, 3, this.namespace_);
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeBool(4, this.compactMetadata_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeBytesSize(1, this.stmt_);
            }
            for (int i = 0; i < this.args_.size(); ++i) {
                size += CodedOutputStream.computeMessageSize(2, this.args_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                size += GeneratedMessageV3.computeStringSize(3, this.namespace_);
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeBoolSize(4, this.compactMetadata_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof StmtExecute)) {
                return super.equals(obj);
            }
            StmtExecute other = (StmtExecute)obj;
            if (this.hasNamespace() != other.hasNamespace()) {
                return false;
            }
            if (this.hasNamespace() && !this.getNamespace().equals(other.getNamespace())) {
                return false;
            }
            if (this.hasStmt() != other.hasStmt()) {
                return false;
            }
            if (this.hasStmt() && !this.getStmt().equals(other.getStmt())) {
                return false;
            }
            if (!this.getArgsList().equals(other.getArgsList())) {
                return false;
            }
            if (this.hasCompactMetadata() != other.hasCompactMetadata()) {
                return false;
            }
            if (this.hasCompactMetadata() && this.getCompactMetadata() != other.getCompactMetadata()) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + StmtExecute.getDescriptor().hashCode();
            if (this.hasNamespace()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getNamespace().hashCode();
            }
            if (this.hasStmt()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getStmt().hashCode();
            }
            if (this.getArgsCount() > 0) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getArgsList().hashCode();
            }
            if (this.hasCompactMetadata()) {
                hash = 37 * hash + 4;
                hash = 53 * hash + Internal.hashBoolean(this.getCompactMetadata());
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static StmtExecute parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecute parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecute parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecute parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecute parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static StmtExecute parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static StmtExecute parseFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static StmtExecute parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static StmtExecute parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
        }

        public static StmtExecute parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static StmtExecute parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input);
        }

        public static StmtExecute parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return StmtExecute.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(StmtExecute prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static StmtExecute getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<StmtExecute> parser() {
            return PARSER;
        }

        public Parser<StmtExecute> getParserForType() {
            return PARSER;
        }

        @Override
        public StmtExecute getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        public static final class Builder
        extends GeneratedMessageV3.Builder<Builder>
        implements StmtExecuteOrBuilder {
            private int bitField0_;
            private Object namespace_ = "sql";
            private ByteString stmt_ = ByteString.EMPTY;
            private List<MysqlxDatatypes.Any> args_ = Collections.emptyList();
            private RepeatedFieldBuilderV3<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> argsBuilder_;
            private boolean compactMetadata_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_Mysqlx_Sql_StmtExecute_descriptor;
            }

            @Override
            protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_Mysqlx_Sql_StmtExecute_fieldAccessorTable.ensureFieldAccessorsInitialized(StmtExecute.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(GeneratedMessageV3.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.namespace_ = "sql";
                this.stmt_ = ByteString.EMPTY;
                if (this.argsBuilder_ == null) {
                    this.args_ = Collections.emptyList();
                } else {
                    this.args_ = null;
                    this.argsBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFFB;
                this.compactMetadata_ = false;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_Mysqlx_Sql_StmtExecute_descriptor;
            }

            @Override
            public StmtExecute getDefaultInstanceForType() {
                return StmtExecute.getDefaultInstance();
            }

            @Override
            public StmtExecute build() {
                StmtExecute result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public StmtExecute buildPartial() {
                StmtExecute result = new StmtExecute(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(StmtExecute result) {
                if (this.argsBuilder_ == null) {
                    if ((this.bitField0_ & 4) != 0) {
                        this.args_ = Collections.unmodifiableList(this.args_);
                        this.bitField0_ &= 0xFFFFFFFB;
                    }
                    result.args_ = this.args_;
                } else {
                    result.args_ = this.argsBuilder_.build();
                }
            }

            private void buildPartial0(StmtExecute result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.namespace_ = this.namespace_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.stmt_ = this.stmt_;
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 8) != 0) {
                    result.compactMetadata_ = this.compactMetadata_;
                    to_bitField0_ |= 4;
                }
                StmtExecute stmtExecute = result;
                stmtExecute.bitField0_ = stmtExecute.bitField0_ | to_bitField0_;
            }

            @Override
            public Builder clone() {
                return (Builder)super.clone();
            }

            @Override
            public Builder setField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder)super.setField(field, value);
            }

            @Override
            public Builder clearField(Descriptors.FieldDescriptor field) {
                return (Builder)super.clearField(field);
            }

            @Override
            public Builder clearOneof(Descriptors.OneofDescriptor oneof) {
                return (Builder)super.clearOneof(oneof);
            }

            @Override
            public Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
                return (Builder)super.setRepeatedField(field, index, value);
            }

            @Override
            public Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
                return (Builder)super.addRepeatedField(field, value);
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof StmtExecute) {
                    return this.mergeFrom((StmtExecute)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(StmtExecute other) {
                if (other == StmtExecute.getDefaultInstance()) {
                    return this;
                }
                if (other.hasNamespace()) {
                    this.namespace_ = other.namespace_;
                    this.bitField0_ |= 1;
                    this.onChanged();
                }
                if (other.hasStmt()) {
                    this.setStmt(other.getStmt());
                }
                if (this.argsBuilder_ == null) {
                    if (!other.args_.isEmpty()) {
                        if (this.args_.isEmpty()) {
                            this.args_ = other.args_;
                            this.bitField0_ &= 0xFFFFFFFB;
                        } else {
                            this.ensureArgsIsMutable();
                            this.args_.addAll(other.args_);
                        }
                        this.onChanged();
                    }
                } else if (!other.args_.isEmpty()) {
                    if (this.argsBuilder_.isEmpty()) {
                        this.argsBuilder_.dispose();
                        this.argsBuilder_ = null;
                        this.args_ = other.args_;
                        this.bitField0_ &= 0xFFFFFFFB;
                        this.argsBuilder_ = alwaysUseFieldBuilders ? this.getArgsFieldBuilder() : null;
                    } else {
                        this.argsBuilder_.addAllMessages(other.args_);
                    }
                }
                if (other.hasCompactMetadata()) {
                    this.setCompactMetadata(other.getCompactMetadata());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                if (!this.hasStmt()) {
                    return false;
                }
                for (int i = 0; i < this.getArgsCount(); ++i) {
                    if (this.getArgs(i).isInitialized()) continue;
                    return false;
                }
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block12: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block12;
                            }
                            case 10: {
                                this.stmt_ = input.readBytes();
                                this.bitField0_ |= 2;
                                continue block12;
                            }
                            case 18: {
                                MysqlxDatatypes.Any m = input.readMessage(MysqlxDatatypes.Any.PARSER, extensionRegistry);
                                if (this.argsBuilder_ == null) {
                                    this.ensureArgsIsMutable();
                                    this.args_.add(m);
                                    continue block12;
                                }
                                this.argsBuilder_.addMessage(m);
                                continue block12;
                            }
                            case 26: {
                                this.namespace_ = input.readBytes();
                                this.bitField0_ |= 1;
                                continue block12;
                            }
                            case 32: {
                                this.compactMetadata_ = input.readBool();
                                this.bitField0_ |= 8;
                                continue block12;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasNamespace() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public String getNamespace() {
                Object ref = this.namespace_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.namespace_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getNamespaceBytes() {
                Object ref = this.namespace_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.namespace_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setNamespace(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.namespace_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearNamespace() {
                this.namespace_ = StmtExecute.getDefaultInstance().getNamespace();
                this.bitField0_ &= 0xFFFFFFFE;
                this.onChanged();
                return this;
            }

            public Builder setNamespaceBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.namespace_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasStmt() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public ByteString getStmt() {
                return this.stmt_;
            }

            public Builder setStmt(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.stmt_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearStmt() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.stmt_ = StmtExecute.getDefaultInstance().getStmt();
                this.onChanged();
                return this;
            }

            private void ensureArgsIsMutable() {
                if ((this.bitField0_ & 4) == 0) {
                    this.args_ = new ArrayList<MysqlxDatatypes.Any>(this.args_);
                    this.bitField0_ |= 4;
                }
            }

            @Override
            public List<MysqlxDatatypes.Any> getArgsList() {
                if (this.argsBuilder_ == null) {
                    return Collections.unmodifiableList(this.args_);
                }
                return this.argsBuilder_.getMessageList();
            }

            @Override
            public int getArgsCount() {
                if (this.argsBuilder_ == null) {
                    return this.args_.size();
                }
                return this.argsBuilder_.getCount();
            }

            @Override
            public MysqlxDatatypes.Any getArgs(int index) {
                if (this.argsBuilder_ == null) {
                    return this.args_.get(index);
                }
                return this.argsBuilder_.getMessage(index);
            }

            public Builder setArgs(int index, MysqlxDatatypes.Any value) {
                if (this.argsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureArgsIsMutable();
                    this.args_.set(index, value);
                    this.onChanged();
                } else {
                    this.argsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setArgs(int index, MysqlxDatatypes.Any.Builder builderForValue) {
                if (this.argsBuilder_ == null) {
                    this.ensureArgsIsMutable();
                    this.args_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.argsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addArgs(MysqlxDatatypes.Any value) {
                if (this.argsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureArgsIsMutable();
                    this.args_.add(value);
                    this.onChanged();
                } else {
                    this.argsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addArgs(int index, MysqlxDatatypes.Any value) {
                if (this.argsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureArgsIsMutable();
                    this.args_.add(index, value);
                    this.onChanged();
                } else {
                    this.argsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addArgs(MysqlxDatatypes.Any.Builder builderForValue) {
                if (this.argsBuilder_ == null) {
                    this.ensureArgsIsMutable();
                    this.args_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.argsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addArgs(int index, MysqlxDatatypes.Any.Builder builderForValue) {
                if (this.argsBuilder_ == null) {
                    this.ensureArgsIsMutable();
                    this.args_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.argsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllArgs(Iterable<? extends MysqlxDatatypes.Any> values) {
                if (this.argsBuilder_ == null) {
                    this.ensureArgsIsMutable();
                    AbstractMessageLite.Builder.addAll(values, this.args_);
                    this.onChanged();
                } else {
                    this.argsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearArgs() {
                if (this.argsBuilder_ == null) {
                    this.args_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFFB;
                    this.onChanged();
                } else {
                    this.argsBuilder_.clear();
                }
                return this;
            }

            public Builder removeArgs(int index) {
                if (this.argsBuilder_ == null) {
                    this.ensureArgsIsMutable();
                    this.args_.remove(index);
                    this.onChanged();
                } else {
                    this.argsBuilder_.remove(index);
                }
                return this;
            }

            public MysqlxDatatypes.Any.Builder getArgsBuilder(int index) {
                return this.getArgsFieldBuilder().getBuilder(index);
            }

            @Override
            public MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int index) {
                if (this.argsBuilder_ == null) {
                    return this.args_.get(index);
                }
                return this.argsBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList() {
                if (this.argsBuilder_ != null) {
                    return this.argsBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.args_);
            }

            public MysqlxDatatypes.Any.Builder addArgsBuilder() {
                return this.getArgsFieldBuilder().addBuilder(MysqlxDatatypes.Any.getDefaultInstance());
            }

            public MysqlxDatatypes.Any.Builder addArgsBuilder(int index) {
                return this.getArgsFieldBuilder().addBuilder(index, MysqlxDatatypes.Any.getDefaultInstance());
            }

            public List<MysqlxDatatypes.Any.Builder> getArgsBuilderList() {
                return this.getArgsFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilderV3<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getArgsFieldBuilder() {
                if (this.argsBuilder_ == null) {
                    this.argsBuilder_ = new RepeatedFieldBuilderV3(this.args_, (this.bitField0_ & 4) != 0, this.getParentForChildren(), this.isClean());
                    this.args_ = null;
                }
                return this.argsBuilder_;
            }

            @Override
            public boolean hasCompactMetadata() {
                return (this.bitField0_ & 8) != 0;
            }

            @Override
            public boolean getCompactMetadata() {
                return this.compactMetadata_;
            }

            public Builder setCompactMetadata(boolean value) {
                this.compactMetadata_ = value;
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder clearCompactMetadata() {
                this.bitField0_ &= 0xFFFFFFF7;
                this.compactMetadata_ = false;
                this.onChanged();
                return this;
            }

            @Override
            public final Builder setUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder)super.setUnknownFields(unknownFields);
            }

            @Override
            public final Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
                return (Builder)super.mergeUnknownFields(unknownFields);
            }
        }
    }

    public static interface StmtExecuteOrBuilder
    extends MessageOrBuilder {
        public boolean hasNamespace();

        public String getNamespace();

        public ByteString getNamespaceBytes();

        public boolean hasStmt();

        public ByteString getStmt();

        public List<MysqlxDatatypes.Any> getArgsList();

        public MysqlxDatatypes.Any getArgs(int var1);

        public int getArgsCount();

        public List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList();

        public MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int var1);

        public boolean hasCompactMetadata();

        public boolean getCompactMetadata();
    }
}

