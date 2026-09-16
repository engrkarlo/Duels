/*
 * Decompiled with CFR 0.152.
 */
package com.google.protobuf;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.BooleanArrayList;
import com.google.protobuf.ByteString;
import com.google.protobuf.CanIgnoreReturnValue;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedInputStreamReader;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DoubleArrayList;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Extension;
import com.google.protobuf.ExtensionLite;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.FieldSet;
import com.google.protobuf.FloatArrayList;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.IntArrayList;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.LazyField;
import com.google.protobuf.LongArrayList;
import com.google.protobuf.MapEntry;
import com.google.protobuf.MapField;
import com.google.protobuf.MapFieldReflectionAccessor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.MessageReflection;
import com.google.protobuf.Parser;
import com.google.protobuf.Protobuf;
import com.google.protobuf.ProtobufArrayList;
import com.google.protobuf.Schema;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnsafeUtil;
import com.google.protobuf.WireFormat;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class GeneratedMessageV3
extends AbstractMessage
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static boolean alwaysUseFieldBuilders = false;
    protected UnknownFieldSet unknownFields;

    protected GeneratedMessageV3() {
        this.unknownFields = UnknownFieldSet.getDefaultInstance();
    }

    protected GeneratedMessageV3(Builder<?> builder) {
        this.unknownFields = builder.getUnknownFields();
    }

    public Parser<? extends GeneratedMessageV3> getParserForType() {
        throw new UnsupportedOperationException("This is supposed to be overridden by subclasses.");
    }

    static void enableAlwaysUseFieldBuildersForTesting() {
        GeneratedMessageV3.setAlwaysUseFieldBuildersForTesting(true);
    }

    static void setAlwaysUseFieldBuildersForTesting(boolean useBuilders) {
        alwaysUseFieldBuilders = useBuilders;
    }

    protected abstract FieldAccessorTable internalGetFieldAccessorTable();

    @Override
    public Descriptors.Descriptor getDescriptorForType() {
        return this.internalGetFieldAccessorTable().descriptor;
    }

    @Deprecated
    protected void mergeFromAndMakeImmutableInternal(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        Schema<GeneratedMessageV3> schema = Protobuf.getInstance().schemaFor(this);
        try {
            schema.mergeFrom(this, CodedInputStreamReader.forCodedInput(input), extensionRegistry);
        }
        catch (InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        }
        catch (IOException e) {
            throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
        }
        schema.makeImmutable(this);
    }

    private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable(boolean getBytesForString) {
        TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap<Descriptors.FieldDescriptor, Object>();
        Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
        List<Descriptors.FieldDescriptor> fields = descriptor.getFields();
        for (int i = 0; i < fields.size(); ++i) {
            Descriptors.FieldDescriptor field = fields.get(i);
            Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
            if (oneofDescriptor != null) {
                i += oneofDescriptor.getFieldCount() - 1;
                if (!this.hasOneof(oneofDescriptor)) continue;
                field = this.getOneofFieldDescriptor(oneofDescriptor);
            } else {
                if (field.isRepeated()) {
                    List value = (List)this.getField(field);
                    if (value.isEmpty()) continue;
                    result.put(field, value);
                    continue;
                }
                if (!this.hasField(field)) continue;
            }
            if (getBytesForString && field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
                result.put(field, this.getFieldRaw(field));
                continue;
            }
            result.put(field, this.getField(field));
        }
        return result;
    }

    @Override
    public boolean isInitialized() {
        for (Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
            if (field.isRequired() && !this.hasField(field)) {
                return false;
            }
            if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) continue;
            if (field.isRepeated()) {
                List messageList = (List)this.getField(field);
                for (Message element : messageList) {
                    if (element.isInitialized()) continue;
                    return false;
                }
                continue;
            }
            if (!this.hasField(field) || ((Message)this.getField(field)).isInitialized()) continue;
            return false;
        }
        return true;
    }

    @Override
    public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
        return Collections.unmodifiableMap(this.getAllFieldsMutable(false));
    }

    Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
        return Collections.unmodifiableMap(this.getAllFieldsMutable(true));
    }

    @Override
    public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
        return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
    }

    @Override
    public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
        return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
    }

    @Override
    public boolean hasField(Descriptors.FieldDescriptor field) {
        return this.internalGetFieldAccessorTable().getField(field).has(this);
    }

    @Override
    public Object getField(Descriptors.FieldDescriptor field) {
        return this.internalGetFieldAccessorTable().getField(field).get(this);
    }

    Object getFieldRaw(Descriptors.FieldDescriptor field) {
        return this.internalGetFieldAccessorTable().getField(field).getRaw(this);
    }

    @Override
    public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
        return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
    }

    @Override
    public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
        return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
    }

    @Override
    public UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }

    void setUnknownFields(UnknownFieldSet unknownFields) {
        this.unknownFields = unknownFields;
    }

    protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
        if (input.shouldDiscardUnknownFields()) {
            return input.skipField(tag);
        }
        return unknownFields.mergeFieldFrom(tag, input);
    }

    protected boolean parseUnknownFieldProto3(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
        return this.parseUnknownField(input, unknownFields, extensionRegistry, tag);
    }

    protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input) throws IOException {
        try {
            return (M)((Message)parser.parseFrom(input));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
        try {
            return (M)((Message)parser.parseFrom(input, extensions));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input) throws IOException {
        try {
            return (M)((Message)parser.parseFrom(input));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input, ExtensionRegistryLite extensions) throws IOException {
        try {
            return (M)((Message)parser.parseFrom(input, extensions));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input) throws IOException {
        try {
            return (M)((Message)parser.parseDelimitedFrom(input));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
        try {
            return (M)((Message)parser.parseDelimitedFrom(input, extensions));
        }
        catch (InvalidProtocolBufferException e) {
            throw e.unwrapIOException();
        }
    }

    protected static boolean canUseUnsafe() {
        return UnsafeUtil.hasUnsafeArrayOperations() && UnsafeUtil.hasUnsafeByteBufferOperations();
    }

    protected static Internal.IntList emptyIntList() {
        return IntArrayList.emptyList();
    }

    protected static Internal.IntList newIntList() {
        return new IntArrayList();
    }

    protected static Internal.IntList mutableCopy(Internal.IntList list) {
        return GeneratedMessageV3.makeMutableCopy(list);
    }

    protected static Internal.LongList mutableCopy(Internal.LongList list) {
        return GeneratedMessageV3.makeMutableCopy(list);
    }

    protected static Internal.FloatList mutableCopy(Internal.FloatList list) {
        return GeneratedMessageV3.makeMutableCopy(list);
    }

    protected static Internal.DoubleList mutableCopy(Internal.DoubleList list) {
        return GeneratedMessageV3.makeMutableCopy(list);
    }

    protected static Internal.BooleanList mutableCopy(Internal.BooleanList list) {
        return GeneratedMessageV3.makeMutableCopy(list);
    }

    protected static Internal.LongList emptyLongList() {
        return LongArrayList.emptyList();
    }

    protected static Internal.LongList newLongList() {
        return new LongArrayList();
    }

    protected static Internal.FloatList emptyFloatList() {
        return FloatArrayList.emptyList();
    }

    protected static Internal.FloatList newFloatList() {
        return new FloatArrayList();
    }

    protected static Internal.DoubleList emptyDoubleList() {
        return DoubleArrayList.emptyList();
    }

    protected static Internal.DoubleList newDoubleList() {
        return new DoubleArrayList();
    }

    protected static Internal.BooleanList emptyBooleanList() {
        return BooleanArrayList.emptyList();
    }

    protected static Internal.BooleanList newBooleanList() {
        return new BooleanArrayList();
    }

    protected static <ListT extends Internal.ProtobufList<?>> ListT makeMutableCopy(ListT list) {
        return GeneratedMessageV3.makeMutableCopy(list, 0);
    }

    protected static <ListT extends Internal.ProtobufList<?>> ListT makeMutableCopy(ListT list, int minCapacity) {
        int size = list.size();
        if (minCapacity <= size) {
            minCapacity = size * 2;
        }
        if (minCapacity <= 0) {
            minCapacity = 10;
        }
        return (ListT)list.mutableCopyWithCapacity(minCapacity);
    }

    protected static <T> Internal.ProtobufList<T> emptyList(Class<T> elementType) {
        return ProtobufArrayList.emptyList();
    }

    @Override
    public void writeTo(CodedOutputStream output) throws IOException {
        MessageReflection.writeMessageTo(this, this.getAllFieldsRaw(), output, false);
    }

    @Override
    public int getSerializedSize() {
        int size = this.memoizedSize;
        if (size != -1) {
            return size;
        }
        this.memoizedSize = MessageReflection.getSerializedSize(this, this.getAllFieldsRaw());
        return this.memoizedSize;
    }

    protected Object newInstance(UnusedPrivateParameter unused) {
        throw new UnsupportedOperationException("This method must be overridden by the subclass.");
    }

    protected void makeExtensionsImmutable() {
    }

    protected abstract Message.Builder newBuilderForType(BuilderParent var1);

    @Override
    protected Message.Builder newBuilderForType(final AbstractMessage.BuilderParent parent) {
        return this.newBuilderForType(new BuilderParent(){

            @Override
            public void markDirty() {
                parent.markDirty();
            }
        });
    }

    private static Method getMethodOrDie(Class<?> clazz, String name, Class<?> ... params) {
        try {
            return clazz.getMethod(name, params);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("Generated message class \"" + clazz.getName() + "\" missing method \"" + name + "\".", e);
        }
    }

    @CanIgnoreReturnValue
    private static Object invokeOrDie(Method method, Object object, Object ... params) {
        try {
            return method.invoke(object, params);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't use Java reflection to implement protocol message reflection.", e);
        }
        catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            throw new IllegalStateException("Unexpected exception thrown by generated accessor method.", cause);
        }
    }

    protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
        return this.internalGetMapField(fieldNumber);
    }

    @Deprecated
    protected MapField internalGetMapField(int fieldNumber) {
        throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new GeneratedMessageLite.SerializedForm(this);
    }

    private static <MessageT extends ExtendableMessage<MessageT>, T> Extension<MessageT, T> checkNotLite(ExtensionLite<MessageT, T> extension) {
        if (extension.isLite()) {
            throw new IllegalArgumentException("Expected non-lite extension.");
        }
        return (Extension)extension;
    }

    protected static boolean isStringEmpty(Object value) {
        if (value instanceof String) {
            return ((String)value).isEmpty();
        }
        return ((ByteString)value).isEmpty();
    }

    protected static int computeStringSize(int fieldNumber, Object value) {
        if (value instanceof String) {
            return CodedOutputStream.computeStringSize(fieldNumber, (String)value);
        }
        return CodedOutputStream.computeBytesSize(fieldNumber, (ByteString)value);
    }

    protected static int computeStringSizeNoTag(Object value) {
        if (value instanceof String) {
            return CodedOutputStream.computeStringSizeNoTag((String)value);
        }
        return CodedOutputStream.computeBytesSizeNoTag((ByteString)value);
    }

    protected static void writeString(CodedOutputStream output, int fieldNumber, Object value) throws IOException {
        if (value instanceof String) {
            output.writeString(fieldNumber, (String)value);
        } else {
            output.writeBytes(fieldNumber, (ByteString)value);
        }
    }

    protected static void writeStringNoTag(CodedOutputStream output, Object value) throws IOException {
        if (value instanceof String) {
            output.writeStringNoTag((String)value);
        } else {
            output.writeBytesNoTag((ByteString)value);
        }
    }

    protected static <V> void serializeIntegerMapTo(CodedOutputStream out, MapField<Integer, V> field, MapEntry<Integer, V> defaultEntry, int fieldNumber) throws IOException {
        Map<Integer, V> m = field.getMap();
        if (!out.isSerializationDeterministic()) {
            GeneratedMessageV3.serializeMapTo(out, m, defaultEntry, fieldNumber);
            return;
        }
        int[] keys = new int[m.size()];
        int index = 0;
        Iterator<Integer> iterator = m.keySet().iterator();
        while (iterator.hasNext()) {
            int k = iterator.next();
            keys[index++] = k;
        }
        Arrays.sort(keys);
        for (Object key : (Iterator<Integer>)keys) {
            out.writeMessage(fieldNumber, ((MapEntry.Builder)defaultEntry.newBuilderForType()).setKey((int)key).setValue(m.get((int)key)).build());
        }
    }

    protected static <V> void serializeLongMapTo(CodedOutputStream out, MapField<Long, V> field, MapEntry<Long, V> defaultEntry, int fieldNumber) throws IOException {
        Map<Long, V> m = field.getMap();
        if (!out.isSerializationDeterministic()) {
            GeneratedMessageV3.serializeMapTo(out, m, defaultEntry, fieldNumber);
            return;
        }
        long[] keys = new long[m.size()];
        int index = 0;
        Iterator<Long> iterator = m.keySet().iterator();
        while (iterator.hasNext()) {
            long k = iterator.next();
            keys[index++] = k;
        }
        Arrays.sort(keys);
        for (Object key : (Iterator<Long>)keys) {
            out.writeMessage(fieldNumber, ((MapEntry.Builder)defaultEntry.newBuilderForType()).setKey((long)key).setValue(m.get((long)key)).build());
        }
    }

    protected static <V> void serializeStringMapTo(CodedOutputStream out, MapField<String, V> field, MapEntry<String, V> defaultEntry, int fieldNumber) throws IOException {
        Map<String, V> m = field.getMap();
        if (!out.isSerializationDeterministic()) {
            GeneratedMessageV3.serializeMapTo(out, m, defaultEntry, fieldNumber);
            return;
        }
        Object[] keys = new String[m.size()];
        keys = m.keySet().toArray(keys);
        Arrays.sort(keys);
        for (Object key : keys) {
            out.writeMessage(fieldNumber, ((MapEntry.Builder)defaultEntry.newBuilderForType()).setKey(key).setValue(m.get(key)).build());
        }
    }

    protected static <V> void serializeBooleanMapTo(CodedOutputStream out, MapField<Boolean, V> field, MapEntry<Boolean, V> defaultEntry, int fieldNumber) throws IOException {
        Map<Boolean, V> m = field.getMap();
        if (!out.isSerializationDeterministic()) {
            GeneratedMessageV3.serializeMapTo(out, m, defaultEntry, fieldNumber);
            return;
        }
        GeneratedMessageV3.maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, false);
        GeneratedMessageV3.maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, true);
    }

    private static <V> void maybeSerializeBooleanEntryTo(CodedOutputStream out, Map<Boolean, V> m, MapEntry<Boolean, V> defaultEntry, int fieldNumber, boolean key) throws IOException {
        if (m.containsKey(key)) {
            out.writeMessage(fieldNumber, ((MapEntry.Builder)defaultEntry.newBuilderForType()).setKey(key).setValue(m.get(key)).build());
        }
    }

    private static <K, V> void serializeMapTo(CodedOutputStream out, Map<K, V> m, MapEntry<K, V> defaultEntry, int fieldNumber) throws IOException {
        for (Map.Entry<K, V> entry : m.entrySet()) {
            out.writeMessage(fieldNumber, ((MapEntry.Builder)defaultEntry.newBuilderForType()).setKey(entry.getKey()).setValue(entry.getValue()).build());
        }
    }

    public static final class FieldAccessorTable {
        private final Descriptors.Descriptor descriptor;
        private final FieldAccessor[] fields;
        private String[] camelCaseNames;
        private final OneofAccessor[] oneofs;
        private volatile boolean initialized;

        public FieldAccessorTable(Descriptors.Descriptor descriptor, String[] camelCaseNames, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
            this(descriptor, camelCaseNames);
            this.ensureFieldAccessorsInitialized(messageClass, builderClass);
        }

        public FieldAccessorTable(Descriptors.Descriptor descriptor, String[] camelCaseNames) {
            this.descriptor = descriptor;
            this.camelCaseNames = camelCaseNames;
            this.fields = new FieldAccessor[descriptor.getFields().size()];
            this.oneofs = new OneofAccessor[descriptor.getOneofs().size()];
            this.initialized = false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public FieldAccessorTable ensureFieldAccessorsInitialized(Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
            if (this.initialized) {
                return this;
            }
            FieldAccessorTable fieldAccessorTable = this;
            synchronized (fieldAccessorTable) {
                int i;
                if (this.initialized) {
                    return this;
                }
                int fieldsSize = this.fields.length;
                for (i = 0; i < fieldsSize; ++i) {
                    int index;
                    Descriptors.FieldDescriptor field = this.descriptor.getFields().get(i);
                    String containingOneofCamelCaseName = null;
                    if (field.getContainingOneof() != null && (index = fieldsSize + field.getContainingOneof().getIndex()) < this.camelCaseNames.length) {
                        containingOneofCamelCaseName = this.camelCaseNames[index];
                    }
                    if (field.isRepeated()) {
                        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                            if (field.isMapField()) {
                                this.fields[i] = new MapFieldAccessor(field, messageClass);
                                continue;
                            }
                            this.fields[i] = new RepeatedMessageFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass);
                            continue;
                        }
                        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                            this.fields[i] = new RepeatedEnumFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass);
                            continue;
                        }
                        this.fields[i] = new RepeatedFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass);
                        continue;
                    }
                    this.fields[i] = field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE ? new SingularMessageFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName) : (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM ? new SingularEnumFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName) : (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING ? new SingularStringFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName) : new SingularFieldAccessor(field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName)));
                }
                for (i = 0; i < this.descriptor.getOneofs().size(); ++i) {
                    this.oneofs[i] = i < this.descriptor.getRealOneofs().size() ? new RealOneofAccessor(this.descriptor, i, this.camelCaseNames[i + fieldsSize], messageClass, builderClass) : new SyntheticOneofAccessor(this.descriptor, i);
                }
                this.initialized = true;
                this.camelCaseNames = null;
                return this;
            }
        }

        private FieldAccessor getField(Descriptors.FieldDescriptor field) {
            if (field.getContainingType() != this.descriptor) {
                throw new IllegalArgumentException("FieldDescriptor does not match message type.");
            }
            if (field.isExtension()) {
                throw new IllegalArgumentException("This type does not have extensions.");
            }
            return this.fields[field.getIndex()];
        }

        private OneofAccessor getOneof(Descriptors.OneofDescriptor oneof) {
            if (oneof.getContainingType() != this.descriptor) {
                throw new IllegalArgumentException("OneofDescriptor does not match message type.");
            }
            return this.oneofs[oneof.getIndex()];
        }

        private static final class RepeatedMessageFieldAccessor
        extends RepeatedFieldAccessor {
            private final Method newBuilderMethod;
            private final Method getBuilderMethodBuilder;

            RepeatedMessageFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
                super(descriptor, camelCaseName, messageClass, builderClass);
                this.newBuilderMethod = GeneratedMessageV3.getMethodOrDie(this.type, "newBuilder", new Class[0]);
                this.getBuilderMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder", new Class[]{Integer.TYPE});
            }

            private Object coerceType(Object value) {
                if (this.type.isInstance(value)) {
                    return value;
                }
                return ((Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null, new Object[0])).mergeFrom((Message)value).build();
            }

            @Override
            public void setRepeated(Builder<?> builder, int index, Object value) {
                super.setRepeated(builder, index, this.coerceType(value));
            }

            @Override
            public void addRepeated(Builder<?> builder, Object value) {
                super.addRepeated(builder, this.coerceType(value));
            }

            @Override
            public Message.Builder newBuilder() {
                return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null, new Object[0]);
            }

            @Override
            public Message.Builder getRepeatedBuilder(Builder<?> builder, int index) {
                return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.getBuilderMethodBuilder, builder, new Object[]{index});
            }
        }

        private static final class SingularMessageFieldAccessor
        extends SingularFieldAccessor {
            private final Method newBuilderMethod;
            private final Method getBuilderMethodBuilder;

            SingularMessageFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass, String containingOneofCamelCaseName) {
                super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
                this.newBuilderMethod = GeneratedMessageV3.getMethodOrDie(this.type, "newBuilder", new Class[0]);
                this.getBuilderMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder", new Class[0]);
            }

            private Object coerceType(Object value) {
                if (this.type.isInstance(value)) {
                    return value;
                }
                return ((Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null, new Object[0])).mergeFrom((Message)value).buildPartial();
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                super.set(builder, this.coerceType(value));
            }

            @Override
            public Message.Builder newBuilder() {
                return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null, new Object[0]);
            }

            @Override
            public Message.Builder getBuilder(Builder<?> builder) {
                return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.getBuilderMethodBuilder, builder, new Object[0]);
            }
        }

        private static final class SingularStringFieldAccessor
        extends SingularFieldAccessor {
            private final Method getBytesMethod;
            private final Method setBytesMethodBuilder;

            SingularStringFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass, String containingOneofCamelCaseName) {
                super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
                this.getBytesMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Bytes", new Class[0]);
                this.setBytesMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Bytes", new Class[]{ByteString.class});
            }

            @Override
            public Object getRaw(GeneratedMessageV3 message) {
                return GeneratedMessageV3.invokeOrDie(this.getBytesMethod, message, new Object[0]);
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                if (value instanceof ByteString) {
                    Object object = GeneratedMessageV3.invokeOrDie(this.setBytesMethodBuilder, builder, new Object[]{value});
                } else {
                    super.set(builder, value);
                }
            }
        }

        private static final class RepeatedEnumFieldAccessor
        extends RepeatedFieldAccessor {
            private final Descriptors.EnumDescriptor enumDescriptor;
            private final Method valueOfMethod;
            private final Method getValueDescriptorMethod;
            private final boolean supportUnknownEnumValue;
            private Method getRepeatedValueMethod;
            private Method getRepeatedValueMethodBuilder;
            private Method setRepeatedValueMethod;
            private Method addRepeatedValueMethod;

            RepeatedEnumFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
                super(descriptor, camelCaseName, messageClass, builderClass);
                this.enumDescriptor = descriptor.getEnumType();
                this.valueOfMethod = GeneratedMessageV3.getMethodOrDie(this.type, "valueOf", new Class[]{Descriptors.EnumValueDescriptor.class});
                this.getValueDescriptorMethod = GeneratedMessageV3.getMethodOrDie(this.type, "getValueDescriptor", new Class[0]);
                boolean bl = this.supportUnknownEnumValue = !descriptor.legacyEnumFieldTreatedAsClosed();
                if (this.supportUnknownEnumValue) {
                    this.getRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Value", new Class[]{Integer.TYPE});
                    this.getRepeatedValueMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Value", new Class[]{Integer.TYPE});
                    this.setRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", new Class[]{Integer.TYPE, Integer.TYPE});
                    this.addRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "add" + camelCaseName + "Value", new Class[]{Integer.TYPE});
                }
            }

            @Override
            public Object get(GeneratedMessageV3 message) {
                ArrayList<Object> newList = new ArrayList<Object>();
                int size = this.getRepeatedCount(message);
                for (int i = 0; i < size; ++i) {
                    newList.add(this.getRepeated(message, i));
                }
                return Collections.unmodifiableList(newList);
            }

            @Override
            public Object get(Builder<?> builder) {
                ArrayList<Object> newList = new ArrayList<Object>();
                int size = this.getRepeatedCount(builder);
                for (int i = 0; i < size; ++i) {
                    newList.add(this.getRepeated(builder, i));
                }
                return Collections.unmodifiableList(newList);
            }

            @Override
            public Object getRepeated(GeneratedMessageV3 message, int index) {
                if (this.supportUnknownEnumValue) {
                    int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getRepeatedValueMethod, message, new Object[]{index});
                    return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
                }
                return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(message, index), new Object[0]);
            }

            @Override
            public Object getRepeated(Builder<?> builder, int index) {
                if (this.supportUnknownEnumValue) {
                    int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getRepeatedValueMethodBuilder, builder, new Object[]{index});
                    return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
                }
                return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(builder, index), new Object[0]);
            }

            @Override
            public void setRepeated(Builder<?> builder, int index, Object value) {
                if (this.supportUnknownEnumValue) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.setRepeatedValueMethod, builder, new Object[]{index, ((Descriptors.EnumValueDescriptor)value).getNumber()});
                    return;
                }
                super.setRepeated(builder, index, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, new Object[]{value}));
            }

            @Override
            public void addRepeated(Builder<?> builder, Object value) {
                if (this.supportUnknownEnumValue) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.addRepeatedValueMethod, builder, new Object[]{((Descriptors.EnumValueDescriptor)value).getNumber()});
                    return;
                }
                super.addRepeated(builder, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, new Object[]{value}));
            }
        }

        private static final class SingularEnumFieldAccessor
        extends SingularFieldAccessor {
            private final Descriptors.EnumDescriptor enumDescriptor;
            private final Method valueOfMethod;
            private final Method getValueDescriptorMethod;
            private final boolean supportUnknownEnumValue;
            private Method getValueMethod;
            private Method getValueMethodBuilder;
            private Method setValueMethod;

            SingularEnumFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass, String containingOneofCamelCaseName) {
                super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
                this.enumDescriptor = descriptor.getEnumType();
                this.valueOfMethod = GeneratedMessageV3.getMethodOrDie(this.type, "valueOf", new Class[]{Descriptors.EnumValueDescriptor.class});
                this.getValueDescriptorMethod = GeneratedMessageV3.getMethodOrDie(this.type, "getValueDescriptor", new Class[0]);
                boolean bl = this.supportUnknownEnumValue = !descriptor.legacyEnumFieldTreatedAsClosed();
                if (this.supportUnknownEnumValue) {
                    this.getValueMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Value", new Class[0]);
                    this.getValueMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Value", new Class[0]);
                    this.setValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", new Class[]{Integer.TYPE});
                }
            }

            @Override
            public Object get(GeneratedMessageV3 message) {
                if (this.supportUnknownEnumValue) {
                    int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getValueMethod, message, new Object[0]);
                    return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
                }
                return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.get(message), new Object[0]);
            }

            @Override
            public Object get(Builder<?> builder) {
                if (this.supportUnknownEnumValue) {
                    int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getValueMethodBuilder, builder, new Object[0]);
                    return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
                }
                return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.get(builder), new Object[0]);
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                if (this.supportUnknownEnumValue) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.setValueMethod, builder, new Object[]{((Descriptors.EnumValueDescriptor)value).getNumber()});
                    return;
                }
                super.set(builder, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, new Object[]{value}));
            }
        }

        private static class MapFieldAccessor
        implements FieldAccessor {
            private final Descriptors.FieldDescriptor field;
            private final Message mapEntryMessageDefaultInstance;

            MapFieldAccessor(Descriptors.FieldDescriptor descriptor, Class<? extends GeneratedMessageV3> messageClass) {
                this.field = descriptor;
                Method getDefaultInstanceMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "getDefaultInstance", new Class[0]);
                MapFieldReflectionAccessor defaultMapField = this.getMapField((GeneratedMessageV3)GeneratedMessageV3.invokeOrDie(getDefaultInstanceMethod, null, new Object[0]));
                this.mapEntryMessageDefaultInstance = defaultMapField.getMapEntryMessageDefaultInstance();
            }

            private MapFieldReflectionAccessor getMapField(GeneratedMessageV3 message) {
                return message.internalGetMapFieldReflection(this.field.getNumber());
            }

            private MapFieldReflectionAccessor getMapField(Builder<?> builder) {
                return builder.internalGetMapFieldReflection(this.field.getNumber());
            }

            private MapFieldReflectionAccessor getMutableMapField(Builder<?> builder) {
                return builder.internalGetMutableMapFieldReflection(this.field.getNumber());
            }

            private Message coerceType(Message value) {
                if (value == null) {
                    return null;
                }
                if (this.mapEntryMessageDefaultInstance.getClass().isInstance(value)) {
                    return value;
                }
                return this.mapEntryMessageDefaultInstance.toBuilder().mergeFrom(value).build();
            }

            @Override
            public Object get(GeneratedMessageV3 message) {
                ArrayList<Object> result = new ArrayList<Object>();
                for (int i = 0; i < this.getRepeatedCount(message); ++i) {
                    result.add(this.getRepeated(message, i));
                }
                return Collections.unmodifiableList(result);
            }

            @Override
            public Object get(Builder<?> builder) {
                ArrayList<Object> result = new ArrayList<Object>();
                for (int i = 0; i < this.getRepeatedCount(builder); ++i) {
                    result.add(this.getRepeated(builder, i));
                }
                return Collections.unmodifiableList(result);
            }

            @Override
            public Object getRaw(GeneratedMessageV3 message) {
                return this.get(message);
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                this.clear(builder);
                for (Object entry : (List)value) {
                    this.addRepeated(builder, entry);
                }
            }

            @Override
            public Object getRepeated(GeneratedMessageV3 message, int index) {
                return this.getMapField(message).getList().get(index);
            }

            @Override
            public Object getRepeated(Builder<?> builder, int index) {
                return this.getMapField(builder).getList().get(index);
            }

            @Override
            public void setRepeated(Builder<?> builder, int index, Object value) {
                this.getMutableMapField(builder).getMutableList().set(index, this.coerceType((Message)value));
            }

            @Override
            public void addRepeated(Builder<?> builder, Object value) {
                this.getMutableMapField(builder).getMutableList().add(this.coerceType((Message)value));
            }

            @Override
            public boolean has(GeneratedMessageV3 message) {
                throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
            }

            @Override
            public boolean has(Builder<?> builder) {
                throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
            }

            @Override
            public int getRepeatedCount(GeneratedMessageV3 message) {
                return this.getMapField(message).getList().size();
            }

            @Override
            public int getRepeatedCount(Builder<?> builder) {
                return this.getMapField(builder).getList().size();
            }

            @Override
            public void clear(Builder<?> builder) {
                this.getMutableMapField(builder).getMutableList().clear();
            }

            @Override
            public Message.Builder newBuilder() {
                return this.mapEntryMessageDefaultInstance.newBuilderForType();
            }

            @Override
            public Message.Builder getBuilder(Builder<?> builder) {
                throw new UnsupportedOperationException("Nested builder not supported for map fields.");
            }

            @Override
            public Message.Builder getRepeatedBuilder(Builder<?> builder, int index) {
                throw new UnsupportedOperationException("Map fields cannot be repeated");
            }
        }

        private static class RepeatedFieldAccessor
        implements FieldAccessor {
            protected final Class<?> type;
            protected final MethodInvoker invoker;

            RepeatedFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
                ReflectionInvoker reflectionInvoker = new ReflectionInvoker(descriptor, camelCaseName, messageClass, builderClass);
                this.type = reflectionInvoker.getRepeatedMethod.getReturnType();
                this.invoker = RepeatedFieldAccessor.getMethodInvoker(reflectionInvoker);
            }

            static MethodInvoker getMethodInvoker(ReflectionInvoker accessor) {
                return accessor;
            }

            @Override
            public Object get(GeneratedMessageV3 message) {
                return this.invoker.get(message);
            }

            @Override
            public Object get(Builder<?> builder) {
                return this.invoker.get(builder);
            }

            @Override
            public Object getRaw(GeneratedMessageV3 message) {
                return this.get(message);
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                this.clear(builder);
                for (Object element : (List)value) {
                    this.addRepeated(builder, element);
                }
            }

            @Override
            public Object getRepeated(GeneratedMessageV3 message, int index) {
                return this.invoker.getRepeated(message, index);
            }

            @Override
            public Object getRepeated(Builder<?> builder, int index) {
                return this.invoker.getRepeated(builder, index);
            }

            @Override
            public void setRepeated(Builder<?> builder, int index, Object value) {
                this.invoker.setRepeated(builder, index, value);
            }

            @Override
            public void addRepeated(Builder<?> builder, Object value) {
                this.invoker.addRepeated(builder, value);
            }

            @Override
            public boolean has(GeneratedMessageV3 message) {
                throw new UnsupportedOperationException("hasField() called on a repeated field.");
            }

            @Override
            public boolean has(Builder<?> builder) {
                throw new UnsupportedOperationException("hasField() called on a repeated field.");
            }

            @Override
            public int getRepeatedCount(GeneratedMessageV3 message) {
                return this.invoker.getRepeatedCount(message);
            }

            @Override
            public int getRepeatedCount(Builder<?> builder) {
                return this.invoker.getRepeatedCount(builder);
            }

            @Override
            public void clear(Builder<?> builder) {
                this.invoker.clear(builder);
            }

            @Override
            public Message.Builder newBuilder() {
                throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
            }

            @Override
            public Message.Builder getBuilder(Builder<?> builder) {
                throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
            }

            @Override
            public Message.Builder getRepeatedBuilder(Builder<?> builder, int index) {
                throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
            }

            private static final class ReflectionInvoker
            implements MethodInvoker {
                private final Method getMethod;
                private final Method getMethodBuilder;
                private final Method getRepeatedMethod;
                private final Method getRepeatedMethodBuilder;
                private final Method setRepeatedMethod;
                private final Method addRepeatedMethod;
                private final Method getCountMethod;
                private final Method getCountMethodBuilder;
                private final Method clearMethod;

                ReflectionInvoker(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
                    this.getMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "List", new Class[0]);
                    this.getMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "List", new Class[0]);
                    this.getRepeatedMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName, new Class[]{Integer.TYPE});
                    this.getRepeatedMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName, new Class[]{Integer.TYPE});
                    Class<?> type = this.getRepeatedMethod.getReturnType();
                    this.setRepeatedMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName, new Class[]{Integer.TYPE, type});
                    this.addRepeatedMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "add" + camelCaseName, new Class[]{type});
                    this.getCountMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Count", new Class[0]);
                    this.getCountMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Count", new Class[0]);
                    this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName, new Class[0]);
                }

                @Override
                public Object get(GeneratedMessageV3 message) {
                    return GeneratedMessageV3.invokeOrDie(this.getMethod, message, new Object[0]);
                }

                @Override
                public Object get(Builder<?> builder) {
                    return GeneratedMessageV3.invokeOrDie(this.getMethodBuilder, builder, new Object[0]);
                }

                @Override
                public Object getRepeated(GeneratedMessageV3 message, int index) {
                    return GeneratedMessageV3.invokeOrDie(this.getRepeatedMethod, message, new Object[]{index});
                }

                @Override
                public Object getRepeated(Builder<?> builder, int index) {
                    return GeneratedMessageV3.invokeOrDie(this.getRepeatedMethodBuilder, builder, new Object[]{index});
                }

                @Override
                public void setRepeated(Builder<?> builder, int index, Object value) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.setRepeatedMethod, builder, new Object[]{index, value});
                }

                @Override
                public void addRepeated(Builder<?> builder, Object value) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.addRepeatedMethod, builder, new Object[]{value});
                }

                @Override
                public int getRepeatedCount(GeneratedMessageV3 message) {
                    return (Integer)GeneratedMessageV3.invokeOrDie(this.getCountMethod, message, new Object[0]);
                }

                @Override
                public int getRepeatedCount(Builder<?> builder) {
                    return (Integer)GeneratedMessageV3.invokeOrDie(this.getCountMethodBuilder, builder, new Object[0]);
                }

                @Override
                public void clear(Builder<?> builder) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder, new Object[0]);
                }
            }

            static interface MethodInvoker {
                public Object get(GeneratedMessageV3 var1);

                public Object get(Builder<?> var1);

                public Object getRepeated(GeneratedMessageV3 var1, int var2);

                public Object getRepeated(Builder<?> var1, int var2);

                public void setRepeated(Builder<?> var1, int var2, Object var3);

                public void addRepeated(Builder<?> var1, Object var2);

                public int getRepeatedCount(GeneratedMessageV3 var1);

                public int getRepeatedCount(Builder<?> var1);

                public void clear(Builder<?> var1);
            }
        }

        private static class SingularFieldAccessor
        implements FieldAccessor {
            protected final Class<?> type;
            protected final Descriptors.FieldDescriptor field;
            protected final boolean isOneofField;
            protected final boolean hasHasMethod;
            protected final MethodInvoker invoker;

            SingularFieldAccessor(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass, String containingOneofCamelCaseName) {
                this.isOneofField = descriptor.getRealContainingOneof() != null;
                this.hasHasMethod = descriptor.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.EDITIONS && descriptor.hasPresence() || descriptor.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2 || descriptor.hasOptionalKeyword() || !this.isOneofField && descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE;
                ReflectionInvoker reflectionInvoker = new ReflectionInvoker(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName, this.isOneofField, this.hasHasMethod);
                this.field = descriptor;
                this.type = reflectionInvoker.getMethod.getReturnType();
                this.invoker = SingularFieldAccessor.getMethodInvoker(reflectionInvoker);
            }

            static MethodInvoker getMethodInvoker(ReflectionInvoker accessor) {
                return accessor;
            }

            @Override
            public Object get(GeneratedMessageV3 message) {
                return this.invoker.get(message);
            }

            @Override
            public Object get(Builder<?> builder) {
                return this.invoker.get(builder);
            }

            @Override
            public Object getRaw(GeneratedMessageV3 message) {
                return this.get(message);
            }

            @Override
            public void set(Builder<?> builder, Object value) {
                this.invoker.set(builder, value);
            }

            @Override
            public Object getRepeated(GeneratedMessageV3 message, int index) {
                throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
            }

            @Override
            public Object getRepeated(Builder<?> builder, int index) {
                throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
            }

            @Override
            public void setRepeated(Builder<?> builder, int index, Object value) {
                throw new UnsupportedOperationException("setRepeatedField() called on a singular field.");
            }

            @Override
            public void addRepeated(Builder<?> builder, Object value) {
                throw new UnsupportedOperationException("addRepeatedField() called on a singular field.");
            }

            @Override
            public boolean has(GeneratedMessageV3 message) {
                if (!this.hasHasMethod) {
                    if (this.isOneofField) {
                        return this.invoker.getOneofFieldNumber(message) == this.field.getNumber();
                    }
                    return !this.get(message).equals(this.field.getDefaultValue());
                }
                return this.invoker.has(message);
            }

            @Override
            public boolean has(Builder<?> builder) {
                if (!this.hasHasMethod) {
                    if (this.isOneofField) {
                        return this.invoker.getOneofFieldNumber(builder) == this.field.getNumber();
                    }
                    return !this.get(builder).equals(this.field.getDefaultValue());
                }
                return this.invoker.has(builder);
            }

            @Override
            public int getRepeatedCount(GeneratedMessageV3 message) {
                throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
            }

            @Override
            public int getRepeatedCount(Builder<?> builder) {
                throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
            }

            @Override
            public void clear(Builder<?> builder) {
                this.invoker.clear(builder);
            }

            @Override
            public Message.Builder newBuilder() {
                throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
            }

            @Override
            public Message.Builder getBuilder(Builder<?> builder) {
                throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
            }

            @Override
            public Message.Builder getRepeatedBuilder(Builder<?> builder, int index) {
                throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
            }

            private static final class ReflectionInvoker
            implements MethodInvoker {
                private final Method getMethod;
                private final Method getMethodBuilder;
                private final Method setMethod;
                private final Method hasMethod;
                private final Method hasMethodBuilder;
                private final Method clearMethod;
                private final Method caseMethod;
                private final Method caseMethodBuilder;

                ReflectionInvoker(Descriptors.FieldDescriptor descriptor, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass, String containingOneofCamelCaseName, boolean isOneofField, boolean hasHasMethod) {
                    this.getMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName, new Class[0]);
                    this.getMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName, new Class[0]);
                    Class<?> type = this.getMethod.getReturnType();
                    this.setMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName, new Class[]{type});
                    this.hasMethod = hasHasMethod ? GeneratedMessageV3.getMethodOrDie(messageClass, "has" + camelCaseName, new Class[0]) : null;
                    this.hasMethodBuilder = hasHasMethod ? GeneratedMessageV3.getMethodOrDie(builderClass, "has" + camelCaseName, new Class[0]) : null;
                    this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName, new Class[0]);
                    this.caseMethod = isOneofField ? GeneratedMessageV3.getMethodOrDie(messageClass, "get" + containingOneofCamelCaseName + "Case", new Class[0]) : null;
                    this.caseMethodBuilder = isOneofField ? GeneratedMessageV3.getMethodOrDie(builderClass, "get" + containingOneofCamelCaseName + "Case", new Class[0]) : null;
                }

                @Override
                public Object get(GeneratedMessageV3 message) {
                    return GeneratedMessageV3.invokeOrDie(this.getMethod, message, new Object[0]);
                }

                @Override
                public Object get(Builder<?> builder) {
                    return GeneratedMessageV3.invokeOrDie(this.getMethodBuilder, builder, new Object[0]);
                }

                @Override
                public int getOneofFieldNumber(GeneratedMessageV3 message) {
                    return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message, new Object[0])).getNumber();
                }

                @Override
                public int getOneofFieldNumber(Builder<?> builder) {
                    return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder, new Object[0])).getNumber();
                }

                @Override
                public void set(Builder<?> builder, Object value) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.setMethod, builder, new Object[]{value});
                }

                @Override
                public boolean has(GeneratedMessageV3 message) {
                    return (Boolean)GeneratedMessageV3.invokeOrDie(this.hasMethod, message, new Object[0]);
                }

                @Override
                public boolean has(Builder<?> builder) {
                    return (Boolean)GeneratedMessageV3.invokeOrDie(this.hasMethodBuilder, builder, new Object[0]);
                }

                @Override
                public void clear(Builder<?> builder) {
                    Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder, new Object[0]);
                }
            }

            private static interface MethodInvoker {
                public Object get(GeneratedMessageV3 var1);

                public Object get(Builder<?> var1);

                public int getOneofFieldNumber(GeneratedMessageV3 var1);

                public int getOneofFieldNumber(Builder<?> var1);

                public void set(Builder<?> var1, Object var2);

                public boolean has(GeneratedMessageV3 var1);

                public boolean has(Builder<?> var1);

                public void clear(Builder<?> var1);
            }
        }

        private static class SyntheticOneofAccessor
        implements OneofAccessor {
            private final Descriptors.FieldDescriptor fieldDescriptor;

            SyntheticOneofAccessor(Descriptors.Descriptor descriptor, int oneofIndex) {
                Descriptors.OneofDescriptor oneofDescriptor = descriptor.getOneofs().get(oneofIndex);
                this.fieldDescriptor = oneofDescriptor.getFields().get(0);
            }

            @Override
            public boolean has(GeneratedMessageV3 message) {
                return message.hasField(this.fieldDescriptor);
            }

            @Override
            public boolean has(Builder<?> builder) {
                return builder.hasField(this.fieldDescriptor);
            }

            @Override
            public Descriptors.FieldDescriptor get(GeneratedMessageV3 message) {
                return message.hasField(this.fieldDescriptor) ? this.fieldDescriptor : null;
            }

            @Override
            public Descriptors.FieldDescriptor get(Builder<?> builder) {
                return builder.hasField(this.fieldDescriptor) ? this.fieldDescriptor : null;
            }

            @Override
            public void clear(Builder<?> builder) {
                builder.clearField(this.fieldDescriptor);
            }
        }

        private static class RealOneofAccessor
        implements OneofAccessor {
            private final Descriptors.Descriptor descriptor;
            private final Method caseMethod;
            private final Method caseMethodBuilder;
            private final Method clearMethod;

            RealOneofAccessor(Descriptors.Descriptor descriptor, int oneofIndex, String camelCaseName, Class<? extends GeneratedMessageV3> messageClass, Class<? extends Builder<?>> builderClass) {
                this.descriptor = descriptor;
                this.caseMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Case", new Class[0]);
                this.caseMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Case", new Class[0]);
                this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName, new Class[0]);
            }

            @Override
            public boolean has(GeneratedMessageV3 message) {
                return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message, new Object[0])).getNumber() != 0;
            }

            @Override
            public boolean has(Builder<?> builder) {
                return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder, new Object[0])).getNumber() != 0;
            }

            @Override
            public Descriptors.FieldDescriptor get(GeneratedMessageV3 message) {
                int fieldNumber = ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message, new Object[0])).getNumber();
                if (fieldNumber > 0) {
                    return this.descriptor.findFieldByNumber(fieldNumber);
                }
                return null;
            }

            @Override
            public Descriptors.FieldDescriptor get(Builder<?> builder) {
                int fieldNumber = ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder, new Object[0])).getNumber();
                if (fieldNumber > 0) {
                    return this.descriptor.findFieldByNumber(fieldNumber);
                }
                return null;
            }

            @Override
            public void clear(Builder<?> builder) {
                Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder, new Object[0]);
            }
        }

        private static interface OneofAccessor {
            public boolean has(GeneratedMessageV3 var1);

            public boolean has(Builder<?> var1);

            public Descriptors.FieldDescriptor get(GeneratedMessageV3 var1);

            public Descriptors.FieldDescriptor get(Builder<?> var1);

            public void clear(Builder<?> var1);
        }

        private static interface FieldAccessor {
            public Object get(GeneratedMessageV3 var1);

            public Object get(Builder<?> var1);

            public Object getRaw(GeneratedMessageV3 var1);

            public void set(Builder<?> var1, Object var2);

            public Object getRepeated(GeneratedMessageV3 var1, int var2);

            public Object getRepeated(Builder<?> var1, int var2);

            public void setRepeated(Builder<?> var1, int var2, Object var3);

            public void addRepeated(Builder<?> var1, Object var2);

            public boolean has(GeneratedMessageV3 var1);

            public boolean has(Builder<?> var1);

            public int getRepeatedCount(GeneratedMessageV3 var1);

            public int getRepeatedCount(Builder<?> var1);

            public void clear(Builder<?> var1);

            public Message.Builder newBuilder();

            public Message.Builder getBuilder(Builder<?> var1);

            public Message.Builder getRepeatedBuilder(Builder<?> var1, int var2);
        }
    }

    static interface ExtensionDescriptorRetriever {
        public Descriptors.FieldDescriptor getDescriptor();
    }

    public static abstract class ExtendableBuilder<MessageT extends ExtendableMessage<MessageT>, BuilderT extends ExtendableBuilder<MessageT, BuilderT>>
    extends Builder<BuilderT>
    implements ExtendableMessageOrBuilder<MessageT> {
        private FieldSet.Builder<Descriptors.FieldDescriptor> extensions;

        protected ExtendableBuilder() {
        }

        protected ExtendableBuilder(BuilderParent parent) {
            super(parent);
        }

        void internalSetExtensionSet(FieldSet<Descriptors.FieldDescriptor> extensions) {
            this.extensions = FieldSet.Builder.fromFieldSet(extensions);
        }

        @Override
        public BuilderT clear() {
            this.extensions = null;
            return (BuilderT)((ExtendableBuilder)super.clear());
        }

        private void ensureExtensionsIsMutable() {
            if (this.extensions == null) {
                this.extensions = FieldSet.newBuilder();
            }
        }

        private void verifyExtensionContainingType(Extension<MessageT, ?> extension) {
            if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
                throw new IllegalArgumentException("Extension is for type \"" + extension.getDescriptor().getContainingType().getFullName() + "\" which does not match message type \"" + this.getDescriptorForType().getFullName() + "\".");
            }
        }

        @Override
        public final <T> boolean hasExtension(ExtensionLite<MessageT, T> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            return this.extensions != null && this.extensions.hasField(extension.getDescriptor());
        }

        @Override
        public final <T> int getExtensionCount(ExtensionLite<MessageT, List<T>> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            return this.extensions == null ? 0 : this.extensions.getRepeatedFieldCount(descriptor);
        }

        @Override
        public final <T> T getExtension(ExtensionLite<MessageT, T> extensionLite) {
            Object value;
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            Object object = value = this.extensions == null ? null : this.extensions.getField(descriptor);
            if (value == null) {
                if (descriptor.isRepeated()) {
                    return (T)Collections.emptyList();
                }
                if (descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    return (T)extension.getMessageDefaultInstance();
                }
                return (T)extension.fromReflectionType(descriptor.getDefaultValue());
            }
            return (T)extension.fromReflectionType(value);
        }

        @Override
        public final <T> T getExtension(ExtensionLite<MessageT, List<T>> extensionLite, int index) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            if (this.extensions == null) {
                throw new IndexOutOfBoundsException();
            }
            return (T)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
        }

        public final <T> BuilderT setExtension(ExtensionLite<MessageT, T> extensionLite, T value) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            this.ensureExtensionsIsMutable();
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            this.extensions.setField(descriptor, extension.toReflectionType(value));
            this.onChanged();
            return (BuilderT)this;
        }

        public final <T> BuilderT setExtension(ExtensionLite<MessageT, List<T>> extensionLite, int index, T value) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            this.ensureExtensionsIsMutable();
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            this.extensions.setRepeatedField(descriptor, index, extension.singularToReflectionType(value));
            this.onChanged();
            return (BuilderT)this;
        }

        public final <T> BuilderT addExtension(ExtensionLite<MessageT, List<T>> extensionLite, T value) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            this.ensureExtensionsIsMutable();
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            this.extensions.addRepeatedField(descriptor, extension.singularToReflectionType(value));
            this.onChanged();
            return (BuilderT)this;
        }

        public final <T> BuilderT clearExtension(ExtensionLite<MessageT, T> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            this.ensureExtensionsIsMutable();
            this.extensions.clearField(extension.getDescriptor());
            this.onChanged();
            return (BuilderT)this;
        }

        @Override
        public final <T> boolean hasExtension(Extension<MessageT, T> extension) {
            return this.hasExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
            return this.hasExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> int getExtensionCount(Extension<MessageT, List<T>> extension) {
            return this.getExtensionCount((ExtensionLite<MessageT, List<T>>)extension);
        }

        @Override
        public final <T> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension) {
            return this.getExtensionCount((ExtensionLite<MessageT, List<T>>)extension);
        }

        @Override
        public final <T> T getExtension(Extension<MessageT, T> extension) {
            return this.getExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
            return this.getExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> T getExtension(Extension<MessageT, List<T>> extension, int index) {
            return this.getExtension((ExtensionLite<MessageT, List<T>>)extension, index);
        }

        @Override
        public final <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, int index) {
            return this.getExtension((ExtensionLite<MessageT, List<T>>)extension, index);
        }

        public final <T> BuilderT setExtension(Extension<MessageT, T> extension, T value) {
            return this.setExtension((ExtensionLite<MessageT, T>)extension, value);
        }

        public <T> BuilderT setExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension, T value) {
            return this.setExtension((ExtensionLite<MessageT, T>)extension, value);
        }

        public final <T> BuilderT setExtension(Extension<MessageT, List<T>> extension, int index, T value) {
            return this.setExtension((ExtensionLite<MessageT, List<T>>)extension, index, value);
        }

        public <T> BuilderT setExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, int index, T value) {
            return this.setExtension((ExtensionLite<MessageT, List<T>>)extension, index, value);
        }

        public final <T> BuilderT addExtension(Extension<MessageT, List<T>> extension, T value) {
            return this.addExtension((ExtensionLite<MessageT, List<T>>)extension, value);
        }

        public <T> BuilderT addExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, T value) {
            return this.addExtension((ExtensionLite<MessageT, List<T>>)extension, value);
        }

        public final <T> BuilderT clearExtension(Extension<MessageT, T> extension) {
            return this.clearExtension((ExtensionLite<MessageT, T>)extension);
        }

        public <T> BuilderT clearExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
            return this.clearExtension((ExtensionLite<MessageT, T>)extension);
        }

        protected boolean extensionsAreInitialized() {
            return this.extensions == null || this.extensions.isInitialized();
        }

        private FieldSet<Descriptors.FieldDescriptor> buildExtensions() {
            return this.extensions == null ? FieldSet.emptySet() : this.extensions.buildPartial();
        }

        @Override
        public boolean isInitialized() {
            return super.isInitialized() && this.extensionsAreInitialized();
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            Map result = ((Builder)this).getAllFieldsMutable();
            if (this.extensions != null) {
                result.putAll(this.extensions.getAllFields());
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                Object value;
                this.verifyContainingType(field);
                Object object = value = this.extensions == null ? null : this.extensions.getField(field);
                if (value == null) {
                    if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                        return DynamicMessage.getDefaultInstance(field.getMessageType());
                    }
                    return field.getDefaultValue();
                }
                return value;
            }
            return super.getField(field);
        }

        @Override
        public Message.Builder getFieldBuilder(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
                }
                this.ensureExtensionsIsMutable();
                Object value = this.extensions.getFieldAllowBuilders(field);
                if (value == null) {
                    DynamicMessage.Builder builder = DynamicMessage.newBuilder(field.getMessageType());
                    this.extensions.setField(field, builder);
                    this.onChanged();
                    return builder;
                }
                if (value instanceof Message.Builder) {
                    return (Message.Builder)value;
                }
                if (value instanceof Message) {
                    Message.Builder builder = ((Message)value).toBuilder();
                    this.extensions.setField(field, builder);
                    this.onChanged();
                    return builder;
                }
                throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
            }
            return super.getFieldBuilder(field);
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                return this.extensions == null ? 0 : this.extensions.getRepeatedFieldCount(field);
            }
            return super.getRepeatedFieldCount(field);
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                if (this.extensions == null) {
                    throw new IndexOutOfBoundsException();
                }
                return this.extensions.getRepeatedField(field, index);
            }
            return super.getRepeatedField(field, index);
        }

        @Override
        public Message.Builder getRepeatedFieldBuilder(Descriptors.FieldDescriptor field, int index) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                this.ensureExtensionsIsMutable();
                if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
                }
                Object value = this.extensions.getRepeatedFieldAllowBuilders(field, index);
                if (value instanceof Message.Builder) {
                    return (Message.Builder)value;
                }
                if (value instanceof Message) {
                    Message.Builder builder = ((Message)value).toBuilder();
                    this.extensions.setRepeatedField(field, index, builder);
                    this.onChanged();
                    return builder;
                }
                throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
            }
            return super.getRepeatedFieldBuilder(field, index);
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                return this.extensions != null && this.extensions.hasField(field);
            }
            return super.hasField(field);
        }

        @Override
        public BuilderT setField(Descriptors.FieldDescriptor field, Object value) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                this.ensureExtensionsIsMutable();
                this.extensions.setField(field, value);
                this.onChanged();
                return (BuilderT)this;
            }
            return (BuilderT)((ExtendableBuilder)super.setField(field, value));
        }

        @Override
        public BuilderT clearField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                this.ensureExtensionsIsMutable();
                this.extensions.clearField(field);
                this.onChanged();
                return (BuilderT)this;
            }
            return (BuilderT)((ExtendableBuilder)super.clearField(field));
        }

        @Override
        public BuilderT setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                this.ensureExtensionsIsMutable();
                this.extensions.setRepeatedField(field, index, value);
                this.onChanged();
                return (BuilderT)this;
            }
            return (BuilderT)((ExtendableBuilder)super.setRepeatedField(field, index, value));
        }

        @Override
        public BuilderT addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                this.ensureExtensionsIsMutable();
                this.extensions.addRepeatedField(field, value);
                this.onChanged();
                return (BuilderT)this;
            }
            return (BuilderT)((ExtendableBuilder)super.addRepeatedField(field, value));
        }

        @Override
        public Message.Builder newBuilderForField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                return DynamicMessage.newBuilder(field.getMessageType());
            }
            return super.newBuilderForField(field);
        }

        protected final void mergeExtensionFields(ExtendableMessage<?> other) {
            if (((ExtendableMessage)other).extensions != null) {
                this.ensureExtensionsIsMutable();
                this.extensions.mergeFrom(((ExtendableMessage)other).extensions);
                this.onChanged();
            }
        }

        @Override
        protected boolean parseUnknownField(CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
            this.ensureExtensionsIsMutable();
            return MessageReflection.mergeFieldFrom(input, input.shouldDiscardUnknownFields() ? null : this.getUnknownFieldSetBuilder(), extensionRegistry, this.getDescriptorForType(), new MessageReflection.ExtensionBuilderAdapter(this.extensions), tag);
        }

        private void verifyContainingType(Descriptors.FieldDescriptor field) {
            if (field.getContainingType() != this.getDescriptorForType()) {
                throw new IllegalArgumentException("FieldDescriptor does not match message type.");
            }
        }
    }

    public static abstract class ExtendableMessage<MessageT extends ExtendableMessage<MessageT>>
    extends GeneratedMessageV3
    implements ExtendableMessageOrBuilder<MessageT> {
        private static final long serialVersionUID = 1L;
        private final FieldSet<Descriptors.FieldDescriptor> extensions;

        protected ExtendableMessage() {
            this.extensions = FieldSet.newFieldSet();
        }

        protected ExtendableMessage(ExtendableBuilder<MessageT, ?> builder) {
            super(builder);
            this.extensions = ((ExtendableBuilder)builder).buildExtensions();
        }

        private void verifyExtensionContainingType(Extension<MessageT, ?> extension) {
            if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
                throw new IllegalArgumentException("Extension is for type \"" + extension.getDescriptor().getContainingType().getFullName() + "\" which does not match message type \"" + this.getDescriptorForType().getFullName() + "\".");
            }
        }

        @Override
        public final <T> boolean hasExtension(ExtensionLite<MessageT, T> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            return this.extensions.hasField(extension.getDescriptor());
        }

        @Override
        public final <T> int getExtensionCount(ExtensionLite<MessageT, List<T>> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            return this.extensions.getRepeatedFieldCount(descriptor);
        }

        @Override
        public final <T> T getExtension(ExtensionLite<MessageT, T> extensionLite) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            Object value = this.extensions.getField(descriptor);
            if (value == null) {
                if (descriptor.isRepeated()) {
                    return (T)Collections.emptyList();
                }
                if (descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    return (T)extension.getMessageDefaultInstance();
                }
                return (T)extension.fromReflectionType(descriptor.getDefaultValue());
            }
            return (T)extension.fromReflectionType(value);
        }

        @Override
        public final <T> T getExtension(ExtensionLite<MessageT, List<T>> extensionLite, int index) {
            Extension extension = GeneratedMessageV3.checkNotLite(extensionLite);
            this.verifyExtensionContainingType(extension);
            Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
            return (T)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
        }

        @Override
        public final <T> boolean hasExtension(Extension<MessageT, T> extension) {
            return this.hasExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
            return this.hasExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> int getExtensionCount(Extension<MessageT, List<T>> extension) {
            return this.getExtensionCount((ExtensionLite<MessageT, List<T>>)extension);
        }

        @Override
        public final <T> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension) {
            return this.getExtensionCount((ExtensionLite<MessageT, List<T>>)extension);
        }

        @Override
        public final <T> T getExtension(Extension<MessageT, T> extension) {
            return this.getExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
            return this.getExtension((ExtensionLite<MessageT, T>)extension);
        }

        @Override
        public final <T> T getExtension(Extension<MessageT, List<T>> extension, int index) {
            return this.getExtension((ExtensionLite<MessageT, List<T>>)extension, index);
        }

        @Override
        public final <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, int index) {
            return this.getExtension((ExtensionLite<MessageT, List<T>>)extension, index);
        }

        protected boolean extensionsAreInitialized() {
            return this.extensions.isInitialized();
        }

        @Override
        public boolean isInitialized() {
            return super.isInitialized() && this.extensionsAreInitialized();
        }

        @Override
        protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
            return MessageReflection.mergeFieldFrom(input, input.shouldDiscardUnknownFields() ? null : unknownFields, extensionRegistry, this.getDescriptorForType(), new MessageReflection.ExtensionAdapter(this.extensions), tag);
        }

        @Override
        protected boolean parseUnknownFieldProto3(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
            return this.parseUnknownField(input, unknownFields, extensionRegistry, tag);
        }

        @Override
        protected void makeExtensionsImmutable() {
            this.extensions.makeImmutable();
        }

        protected ExtensionWriter newExtensionWriter() {
            return new ExtensionWriter(false);
        }

        protected ExtensionWriter newMessageSetExtensionWriter() {
            return new ExtensionWriter(true);
        }

        protected int extensionsSerializedSize() {
            return this.extensions.getSerializedSize();
        }

        protected int extensionsSerializedSizeAsMessageSet() {
            return this.extensions.getMessageSetSerializedSize();
        }

        protected Map<Descriptors.FieldDescriptor, Object> getExtensionFields() {
            return this.extensions.getAllFields();
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            Map result = ((GeneratedMessageV3)this).getAllFieldsMutable(false);
            result.putAll(this.getExtensionFields());
            return Collections.unmodifiableMap(result);
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
            Map result = ((GeneratedMessageV3)this).getAllFieldsMutable(false);
            result.putAll(this.getExtensionFields());
            return Collections.unmodifiableMap(result);
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                return this.extensions.hasField(field);
            }
            return super.hasField(field);
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                Object value = this.extensions.getField(field);
                if (value == null) {
                    if (field.isRepeated()) {
                        return Collections.emptyList();
                    }
                    if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                        return DynamicMessage.getDefaultInstance(field.getMessageType());
                    }
                    return field.getDefaultValue();
                }
                return value;
            }
            return super.getField(field);
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                return this.extensions.getRepeatedFieldCount(field);
            }
            return super.getRepeatedFieldCount(field);
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            if (field.isExtension()) {
                this.verifyContainingType(field);
                return this.extensions.getRepeatedField(field, index);
            }
            return super.getRepeatedField(field, index);
        }

        private void verifyContainingType(Descriptors.FieldDescriptor field) {
            if (field.getContainingType() != this.getDescriptorForType()) {
                throw new IllegalArgumentException("FieldDescriptor does not match message type.");
            }
        }

        protected class ExtensionWriter {
            private final Iterator<Map.Entry<Descriptors.FieldDescriptor, Object>> iter;
            private Map.Entry<Descriptors.FieldDescriptor, Object> next;
            private final boolean messageSetWireFormat;

            private ExtensionWriter(boolean messageSetWireFormat) {
                this.iter = ExtendableMessage.this.extensions.iterator();
                if (this.iter.hasNext()) {
                    this.next = this.iter.next();
                }
                this.messageSetWireFormat = messageSetWireFormat;
            }

            public void writeUntil(int end, CodedOutputStream output) throws IOException {
                while (this.next != null && this.next.getKey().getNumber() < end) {
                    Descriptors.FieldDescriptor descriptor = this.next.getKey();
                    if (this.messageSetWireFormat && descriptor.getLiteJavaType() == WireFormat.JavaType.MESSAGE && !descriptor.isRepeated()) {
                        if (this.next instanceof LazyField.LazyEntry) {
                            output.writeRawMessageSetExtension(descriptor.getNumber(), ((LazyField.LazyEntry)this.next).getField().toByteString());
                        } else {
                            output.writeMessageSetExtension(descriptor.getNumber(), (Message)this.next.getValue());
                        }
                    } else {
                        FieldSet.writeField(descriptor, this.next.getValue(), output);
                    }
                    if (this.iter.hasNext()) {
                        this.next = this.iter.next();
                        continue;
                    }
                    this.next = null;
                }
            }
        }
    }

    public static interface ExtendableMessageOrBuilder<MessageT extends ExtendableMessage<MessageT>>
    extends MessageOrBuilder {
        @Override
        public Message getDefaultInstanceForType();

        public <T> boolean hasExtension(ExtensionLite<MessageT, T> var1);

        public <T> int getExtensionCount(ExtensionLite<MessageT, List<T>> var1);

        public <T> T getExtension(ExtensionLite<MessageT, T> var1);

        public <T> T getExtension(ExtensionLite<MessageT, List<T>> var1, int var2);

        public <T> boolean hasExtension(Extension<MessageT, T> var1);

        public <T> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageT, T> var1);

        public <T> int getExtensionCount(Extension<MessageT, List<T>> var1);

        public <T> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageT, List<T>> var1);

        public <T> T getExtension(Extension<MessageT, T> var1);

        public <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, T> var1);

        public <T> T getExtension(Extension<MessageT, List<T>> var1, int var2);

        public <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> var1, int var2);
    }

    public static abstract class Builder<BuilderT extends Builder<BuilderT>>
    extends AbstractMessage.Builder<BuilderT> {
        private BuilderParent builderParent;
        private BuilderParentImpl meAsParent;
        private boolean isClean;
        private Object unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();

        protected Builder() {
            this(null);
        }

        protected Builder(BuilderParent builderParent) {
            this.builderParent = builderParent;
        }

        @Override
        void dispose() {
            this.builderParent = null;
        }

        protected void onBuilt() {
            if (this.builderParent != null) {
                this.markClean();
            }
        }

        @Override
        protected void markClean() {
            this.isClean = true;
        }

        protected boolean isClean() {
            return this.isClean;
        }

        @Override
        public BuilderT clone() {
            Builder builder = (Builder)this.getDefaultInstanceForType().newBuilderForType();
            builder.mergeFrom((Message)this.buildPartial());
            return (BuilderT)builder;
        }

        @Override
        public BuilderT clear() {
            this.unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();
            this.onChanged();
            return (BuilderT)this;
        }

        protected abstract FieldAccessorTable internalGetFieldAccessorTable();

        @Override
        public Descriptors.Descriptor getDescriptorForType() {
            return this.internalGetFieldAccessorTable().descriptor;
        }

        @Override
        public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
            return Collections.unmodifiableMap(this.getAllFieldsMutable());
        }

        private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable() {
            TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap<Descriptors.FieldDescriptor, Object>();
            Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
            List<Descriptors.FieldDescriptor> fields = descriptor.getFields();
            for (int i = 0; i < fields.size(); ++i) {
                Descriptors.FieldDescriptor field = fields.get(i);
                Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
                if (oneofDescriptor != null) {
                    i += oneofDescriptor.getFieldCount() - 1;
                    if (!this.hasOneof(oneofDescriptor)) continue;
                    field = this.getOneofFieldDescriptor(oneofDescriptor);
                } else {
                    if (field.isRepeated()) {
                        List value = (List)this.getField(field);
                        if (value.isEmpty()) continue;
                        result.put(field, value);
                        continue;
                    }
                    if (!this.hasField(field)) continue;
                }
                result.put(field, this.getField(field));
            }
            return result;
        }

        @Override
        public Message.Builder newBuilderForField(Descriptors.FieldDescriptor field) {
            return this.internalGetFieldAccessorTable().getField(field).newBuilder();
        }

        @Override
        public Message.Builder getFieldBuilder(Descriptors.FieldDescriptor field) {
            return this.internalGetFieldAccessorTable().getField(field).getBuilder(this);
        }

        @Override
        public Message.Builder getRepeatedFieldBuilder(Descriptors.FieldDescriptor field, int index) {
            return this.internalGetFieldAccessorTable().getField(field).getRepeatedBuilder(this, index);
        }

        @Override
        public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
            return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
        }

        @Override
        public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
            return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
        }

        @Override
        public boolean hasField(Descriptors.FieldDescriptor field) {
            return this.internalGetFieldAccessorTable().getField(field).has(this);
        }

        @Override
        public Object getField(Descriptors.FieldDescriptor field) {
            Object object = this.internalGetFieldAccessorTable().getField(field).get(this);
            if (field.isRepeated()) {
                return Collections.unmodifiableList((List)object);
            }
            return object;
        }

        public BuilderT setField(Descriptors.FieldDescriptor field, Object value) {
            this.internalGetFieldAccessorTable().getField(field).set(this, value);
            return (BuilderT)this;
        }

        public BuilderT clearField(Descriptors.FieldDescriptor field) {
            this.internalGetFieldAccessorTable().getField(field).clear(this);
            return (BuilderT)this;
        }

        @Override
        public BuilderT clearOneof(Descriptors.OneofDescriptor oneof) {
            this.internalGetFieldAccessorTable().getOneof(oneof).clear(this);
            return (BuilderT)this;
        }

        @Override
        public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
            return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
        }

        @Override
        public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
            return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
        }

        public BuilderT setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            this.internalGetFieldAccessorTable().getField(field).setRepeated(this, index, value);
            return (BuilderT)this;
        }

        public BuilderT addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
            this.internalGetFieldAccessorTable().getField(field).addRepeated(this, value);
            return (BuilderT)this;
        }

        private BuilderT setUnknownFieldsInternal(UnknownFieldSet unknownFields) {
            this.unknownFieldsOrBuilder = unknownFields;
            this.onChanged();
            return (BuilderT)this;
        }

        public BuilderT setUnknownFields(UnknownFieldSet unknownFields) {
            return this.setUnknownFieldsInternal(unknownFields);
        }

        protected BuilderT setUnknownFieldsProto3(UnknownFieldSet unknownFields) {
            return this.setUnknownFieldsInternal(unknownFields);
        }

        @Override
        public BuilderT mergeUnknownFields(UnknownFieldSet unknownFields) {
            if (UnknownFieldSet.getDefaultInstance().equals(unknownFields)) {
                return (BuilderT)this;
            }
            if (UnknownFieldSet.getDefaultInstance().equals(this.unknownFieldsOrBuilder)) {
                this.unknownFieldsOrBuilder = unknownFields;
                this.onChanged();
                return (BuilderT)this;
            }
            this.getUnknownFieldSetBuilder().mergeFrom(unknownFields);
            this.onChanged();
            return (BuilderT)this;
        }

        @Override
        public boolean isInitialized() {
            for (Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
                if (field.isRequired() && !this.hasField(field)) {
                    return false;
                }
                if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) continue;
                if (field.isRepeated()) {
                    List messageList = (List)this.getField(field);
                    for (Message element : messageList) {
                        if (element.isInitialized()) continue;
                        return false;
                    }
                    continue;
                }
                if (!this.hasField(field) || ((Message)this.getField(field)).isInitialized()) continue;
                return false;
            }
            return true;
        }

        @Override
        public final UnknownFieldSet getUnknownFields() {
            if (this.unknownFieldsOrBuilder instanceof UnknownFieldSet) {
                return (UnknownFieldSet)this.unknownFieldsOrBuilder;
            }
            return ((UnknownFieldSet.Builder)this.unknownFieldsOrBuilder).buildPartial();
        }

        protected boolean parseUnknownField(CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
            if (input.shouldDiscardUnknownFields()) {
                return input.skipField(tag);
            }
            return this.getUnknownFieldSetBuilder().mergeFieldFrom(tag, input);
        }

        protected final void mergeUnknownLengthDelimitedField(int number, ByteString bytes) {
            this.getUnknownFieldSetBuilder().mergeLengthDelimitedField(number, bytes);
        }

        protected final void mergeUnknownVarintField(int number, int value) {
            this.getUnknownFieldSetBuilder().mergeVarintField(number, value);
        }

        @Override
        protected UnknownFieldSet.Builder getUnknownFieldSetBuilder() {
            if (this.unknownFieldsOrBuilder instanceof UnknownFieldSet) {
                this.unknownFieldsOrBuilder = ((UnknownFieldSet)this.unknownFieldsOrBuilder).toBuilder();
            }
            this.onChanged();
            return (UnknownFieldSet.Builder)this.unknownFieldsOrBuilder;
        }

        @Override
        protected void setUnknownFieldSetBuilder(UnknownFieldSet.Builder builder) {
            this.unknownFieldsOrBuilder = builder;
            this.onChanged();
        }

        protected BuilderParent getParentForChildren() {
            if (this.meAsParent == null) {
                this.meAsParent = new BuilderParentImpl();
            }
            return this.meAsParent;
        }

        protected final void onChanged() {
            if (this.isClean && this.builderParent != null) {
                this.builderParent.markDirty();
                this.isClean = false;
            }
        }

        protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
            return this.internalGetMapField(fieldNumber);
        }

        @Deprecated
        protected MapField internalGetMapField(int fieldNumber) {
            throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
        }

        protected MapFieldReflectionAccessor internalGetMutableMapFieldReflection(int fieldNumber) {
            return this.internalGetMutableMapField(fieldNumber);
        }

        @Deprecated
        protected MapField internalGetMutableMapField(int fieldNumber) {
            throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
        }

        private class BuilderParentImpl
        implements BuilderParent {
            private BuilderParentImpl() {
            }

            @Override
            public void markDirty() {
                Builder.this.onChanged();
            }
        }
    }

    protected static interface BuilderParent
    extends AbstractMessage.BuilderParent {
    }

    protected static final class UnusedPrivateParameter {
        static final UnusedPrivateParameter INSTANCE = new UnusedPrivateParameter();

        private UnusedPrivateParameter() {
        }
    }
}

