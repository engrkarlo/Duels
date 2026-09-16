/*
 * Decompiled with CFR 0.152.
 */
package com.google.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.CheckReturnValue;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.FieldSet;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.TextFormat;
import com.google.protobuf.WireFormat;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@CheckReturnValue
public final class Descriptors {
    private static final Logger logger = Logger.getLogger(Descriptors.class.getName());
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Descriptor[] EMPTY_DESCRIPTORS = new Descriptor[0];
    private static final FieldDescriptor[] EMPTY_FIELD_DESCRIPTORS = new FieldDescriptor[0];
    private static final EnumDescriptor[] EMPTY_ENUM_DESCRIPTORS = new EnumDescriptor[0];
    private static final ServiceDescriptor[] EMPTY_SERVICE_DESCRIPTORS = new ServiceDescriptor[0];
    private static final OneofDescriptor[] EMPTY_ONEOF_DESCRIPTORS = new OneofDescriptor[0];

    private static String computeFullName(FileDescriptor file, Descriptor parent, String name) {
        if (parent != null) {
            return parent.getFullName() + '.' + name;
        }
        String packageName = file.getPackage();
        if (!packageName.isEmpty()) {
            return packageName + '.' + name;
        }
        return name;
    }

    private static <T> T binarySearch(T[] array, int size, NumberGetter<T> getter, int number) {
        int left = 0;
        int right = size - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            T midValue = array[mid];
            int midValueNumber = getter.getNumber(midValue);
            if (number < midValueNumber) {
                right = mid - 1;
                continue;
            }
            if (number > midValueNumber) {
                left = mid + 1;
                continue;
            }
            return midValue;
        }
        return null;
    }

    private static interface NumberGetter<T> {
        public int getNumber(T var1);
    }

    public static final class OneofDescriptor
    extends GenericDescriptor {
        private final int index;
        private DescriptorProtos.OneofDescriptorProto proto;
        private final String fullName;
        private final FileDescriptor file;
        private Descriptor containingType;
        private int fieldCount;
        private FieldDescriptor[] fields;

        public int getIndex() {
            return this.index;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        public Descriptor getContainingType() {
            return this.containingType;
        }

        public int getFieldCount() {
            return this.fieldCount;
        }

        public DescriptorProtos.OneofOptions getOptions() {
            return this.proto.getOptions();
        }

        public List<FieldDescriptor> getFields() {
            return Collections.unmodifiableList(Arrays.asList(this.fields));
        }

        public FieldDescriptor getField(int index) {
            return this.fields[index];
        }

        @Override
        public DescriptorProtos.OneofDescriptorProto toProto() {
            return this.proto;
        }

        @Deprecated
        public boolean isSynthetic() {
            return this.fields.length == 1 && this.fields[0].isProto3Optional;
        }

        private void setProto(DescriptorProtos.OneofDescriptorProto proto) {
            this.proto = proto;
        }

        private OneofDescriptor(DescriptorProtos.OneofDescriptorProto proto, FileDescriptor file, Descriptor parent, int index) {
            this.proto = proto;
            this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
            this.file = file;
            this.index = index;
            this.containingType = parent;
            this.fieldCount = 0;
        }

        static /* synthetic */ FieldDescriptor[] access$2502(OneofDescriptor x0, FieldDescriptor[] x1) {
            x0.fields = x1;
            return x1;
        }
    }

    private static final class DescriptorPool {
        private final Set<FileDescriptor> dependencies;
        private final boolean allowUnknownDependencies;
        private final Map<String, GenericDescriptor> descriptorsByName = new HashMap<String, GenericDescriptor>();

        DescriptorPool(FileDescriptor[] dependencies, boolean allowUnknownDependencies) {
            this.dependencies = Collections.newSetFromMap(new IdentityHashMap(dependencies.length));
            this.allowUnknownDependencies = allowUnknownDependencies;
            for (FileDescriptor dependency : dependencies) {
                this.dependencies.add(dependency);
                this.importPublicDependencies(dependency);
            }
            for (FileDescriptor dependency : this.dependencies) {
                try {
                    this.addPackage(dependency.getPackage(), dependency);
                }
                catch (DescriptorValidationException e) {
                    throw new AssertionError((Object)e);
                }
            }
        }

        private void importPublicDependencies(FileDescriptor file) {
            for (FileDescriptor dependency : file.getPublicDependencies()) {
                if (!this.dependencies.add(dependency)) continue;
                this.importPublicDependencies(dependency);
            }
        }

        GenericDescriptor findSymbol(String fullName) {
            return this.findSymbol(fullName, SearchFilter.ALL_SYMBOLS);
        }

        GenericDescriptor findSymbol(String fullName, SearchFilter filter) {
            GenericDescriptor result = this.descriptorsByName.get(fullName);
            if (result != null && (filter == SearchFilter.ALL_SYMBOLS || filter == SearchFilter.TYPES_ONLY && this.isType(result) || filter == SearchFilter.AGGREGATES_ONLY && this.isAggregate(result))) {
                return result;
            }
            for (FileDescriptor dependency : this.dependencies) {
                result = ((FileDescriptor)dependency).pool.descriptorsByName.get(fullName);
                if (result == null || filter != SearchFilter.ALL_SYMBOLS && (filter != SearchFilter.TYPES_ONLY || !this.isType(result)) && (filter != SearchFilter.AGGREGATES_ONLY || !this.isAggregate(result))) continue;
                return result;
            }
            return null;
        }

        boolean isType(GenericDescriptor descriptor) {
            return descriptor instanceof Descriptor || descriptor instanceof EnumDescriptor;
        }

        boolean isAggregate(GenericDescriptor descriptor) {
            return descriptor instanceof Descriptor || descriptor instanceof EnumDescriptor || descriptor instanceof PackageDescriptor || descriptor instanceof ServiceDescriptor;
        }

        GenericDescriptor lookupSymbol(String name, GenericDescriptor relativeTo, SearchFilter filter) throws DescriptorValidationException {
            GenericDescriptor result;
            String fullname;
            if (name.startsWith(".")) {
                fullname = name.substring(1);
                result = this.findSymbol(fullname, filter);
            } else {
                int firstPartLength = name.indexOf(46);
                String firstPart = firstPartLength == -1 ? name : name.substring(0, firstPartLength);
                StringBuilder scopeToTry = new StringBuilder(relativeTo.getFullName());
                while (true) {
                    int dotpos;
                    if ((dotpos = scopeToTry.lastIndexOf(".")) == -1) {
                        fullname = name;
                        result = this.findSymbol(name, filter);
                        break;
                    }
                    scopeToTry.setLength(dotpos + 1);
                    scopeToTry.append(firstPart);
                    result = this.findSymbol(scopeToTry.toString(), SearchFilter.AGGREGATES_ONLY);
                    if (result != null) {
                        if (firstPartLength != -1) {
                            scopeToTry.setLength(dotpos + 1);
                            scopeToTry.append(name);
                            result = this.findSymbol(scopeToTry.toString(), filter);
                        }
                        fullname = scopeToTry.toString();
                        break;
                    }
                    scopeToTry.setLength(dotpos);
                }
            }
            if (result == null) {
                if (this.allowUnknownDependencies && filter == SearchFilter.TYPES_ONLY) {
                    logger.warning("The descriptor for message type \"" + name + "\" cannot be found and a placeholder is created for it");
                    result = new Descriptor(fullname);
                    this.dependencies.add(result.getFile());
                    return result;
                }
                throw new DescriptorValidationException(relativeTo, '\"' + name + "\" is not defined.");
            }
            return result;
        }

        void addSymbol(GenericDescriptor descriptor) throws DescriptorValidationException {
            DescriptorPool.validateSymbolName(descriptor);
            String fullName = descriptor.getFullName();
            GenericDescriptor old = this.descriptorsByName.put(fullName, descriptor);
            if (old != null) {
                this.descriptorsByName.put(fullName, old);
                if (descriptor.getFile() == old.getFile()) {
                    int dotpos = fullName.lastIndexOf(46);
                    if (dotpos == -1) {
                        throw new DescriptorValidationException(descriptor, '\"' + fullName + "\" is already defined.");
                    }
                    throw new DescriptorValidationException(descriptor, '\"' + fullName.substring(dotpos + 1) + "\" is already defined in \"" + fullName.substring(0, dotpos) + "\".");
                }
                throw new DescriptorValidationException(descriptor, '\"' + fullName + "\" is already defined in file \"" + old.getFile().getName() + "\".");
            }
        }

        void addPackage(String fullName, FileDescriptor file) throws DescriptorValidationException {
            String name;
            int dotpos = fullName.lastIndexOf(46);
            if (dotpos == -1) {
                name = fullName;
            } else {
                this.addPackage(fullName.substring(0, dotpos), file);
                name = fullName.substring(dotpos + 1);
            }
            GenericDescriptor old = this.descriptorsByName.put(fullName, new PackageDescriptor(name, fullName, file));
            if (old != null) {
                this.descriptorsByName.put(fullName, old);
                if (!(old instanceof PackageDescriptor)) {
                    throw new DescriptorValidationException(file, '\"' + name + "\" is already defined (as something other than a package) in file \"" + old.getFile().getName() + "\".");
                }
            }
        }

        static void validateSymbolName(GenericDescriptor descriptor) throws DescriptorValidationException {
            String name = descriptor.getName();
            if (name.length() == 0) {
                throw new DescriptorValidationException(descriptor, "Missing name.");
            }
            for (int i = 0; i < name.length(); ++i) {
                char c = name.charAt(i);
                if ('a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_' || '0' <= c && c <= '9' && i > 0) continue;
                throw new DescriptorValidationException(descriptor, '\"' + name + "\" is not a valid identifier.");
            }
        }

        private static final class PackageDescriptor
        extends GenericDescriptor {
            private final String name;
            private final String fullName;
            private final FileDescriptor file;

            @Override
            public Message toProto() {
                return this.file.toProto();
            }

            @Override
            public String getName() {
                return this.name;
            }

            @Override
            public String getFullName() {
                return this.fullName;
            }

            @Override
            public FileDescriptor getFile() {
                return this.file;
            }

            PackageDescriptor(String name, String fullName, FileDescriptor file) {
                this.file = file;
                this.fullName = fullName;
                this.name = name;
            }
        }

        static enum SearchFilter {
            TYPES_ONLY,
            AGGREGATES_ONLY,
            ALL_SYMBOLS;

        }
    }

    public static class DescriptorValidationException
    extends Exception {
        private static final long serialVersionUID = 5750205775490483148L;
        private final String name;
        private final Message proto;
        private final String description;

        public String getProblemSymbolName() {
            return this.name;
        }

        public Message getProblemProto() {
            return this.proto;
        }

        public String getDescription() {
            return this.description;
        }

        private DescriptorValidationException(GenericDescriptor problemDescriptor, String description) {
            super(problemDescriptor.getFullName() + ": " + description);
            this.name = problemDescriptor.getFullName();
            this.proto = problemDescriptor.toProto();
            this.description = description;
        }

        private DescriptorValidationException(GenericDescriptor problemDescriptor, String description, Throwable cause) {
            this(problemDescriptor, description);
            this.initCause(cause);
        }

        private DescriptorValidationException(FileDescriptor problemDescriptor, String description) {
            super(problemDescriptor.getName() + ": " + description);
            this.name = problemDescriptor.getName();
            this.proto = problemDescriptor.toProto();
            this.description = description;
        }
    }

    public static abstract class GenericDescriptor {
        private GenericDescriptor() {
        }

        public abstract Message toProto();

        public abstract String getName();

        public abstract String getFullName();

        public abstract FileDescriptor getFile();
    }

    public static final class MethodDescriptor
    extends GenericDescriptor {
        private final int index;
        private DescriptorProtos.MethodDescriptorProto proto;
        private final String fullName;
        private final FileDescriptor file;
        private final ServiceDescriptor service;
        private Descriptor inputType;
        private Descriptor outputType;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.MethodDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        public ServiceDescriptor getService() {
            return this.service;
        }

        public Descriptor getInputType() {
            return this.inputType;
        }

        public Descriptor getOutputType() {
            return this.outputType;
        }

        public boolean isClientStreaming() {
            return this.proto.getClientStreaming();
        }

        public boolean isServerStreaming() {
            return this.proto.getServerStreaming();
        }

        public DescriptorProtos.MethodOptions getOptions() {
            return this.proto.getOptions();
        }

        private MethodDescriptor(DescriptorProtos.MethodDescriptorProto proto, FileDescriptor file, ServiceDescriptor parent, int index) throws DescriptorValidationException {
            this.index = index;
            this.proto = proto;
            this.file = file;
            this.service = parent;
            this.fullName = parent.getFullName() + '.' + proto.getName();
            file.pool.addSymbol(this);
        }

        private void crossLink() throws DescriptorValidationException {
            GenericDescriptor input = this.getFile().pool.lookupSymbol(this.proto.getInputType(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
            if (!(input instanceof Descriptor)) {
                throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getInputType() + "\" is not a message type.");
            }
            this.inputType = (Descriptor)input;
            GenericDescriptor output = this.getFile().pool.lookupSymbol(this.proto.getOutputType(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
            if (!(output instanceof Descriptor)) {
                throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getOutputType() + "\" is not a message type.");
            }
            this.outputType = (Descriptor)output;
        }

        private void setProto(DescriptorProtos.MethodDescriptorProto proto) {
            this.proto = proto;
        }
    }

    public static final class ServiceDescriptor
    extends GenericDescriptor {
        private final int index;
        private DescriptorProtos.ServiceDescriptorProto proto;
        private final String fullName;
        private final FileDescriptor file;
        private MethodDescriptor[] methods;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.ServiceDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        public DescriptorProtos.ServiceOptions getOptions() {
            return this.proto.getOptions();
        }

        public List<MethodDescriptor> getMethods() {
            return Collections.unmodifiableList(Arrays.asList(this.methods));
        }

        public MethodDescriptor findMethodByName(String name) {
            GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
            if (result instanceof MethodDescriptor) {
                return (MethodDescriptor)result;
            }
            return null;
        }

        private ServiceDescriptor(DescriptorProtos.ServiceDescriptorProto proto, FileDescriptor file, int index) throws DescriptorValidationException {
            this.index = index;
            this.proto = proto;
            this.fullName = Descriptors.computeFullName(file, null, proto.getName());
            this.file = file;
            this.methods = new MethodDescriptor[proto.getMethodCount()];
            for (int i = 0; i < proto.getMethodCount(); ++i) {
                this.methods[i] = new MethodDescriptor(proto.getMethod(i), file, this, i);
            }
            file.pool.addSymbol(this);
        }

        private void crossLink() throws DescriptorValidationException {
            for (MethodDescriptor method : this.methods) {
                method.crossLink();
            }
        }

        private void setProto(DescriptorProtos.ServiceDescriptorProto proto) {
            this.proto = proto;
            for (int i = 0; i < this.methods.length; ++i) {
                this.methods[i].setProto(proto.getMethod(i));
            }
        }
    }

    public static final class EnumValueDescriptor
    extends GenericDescriptor
    implements Internal.EnumLite {
        static final Comparator<EnumValueDescriptor> BY_NUMBER = new Comparator<EnumValueDescriptor>(){

            @Override
            public int compare(EnumValueDescriptor o1, EnumValueDescriptor o2) {
                return Integer.valueOf(o1.getNumber()).compareTo(o2.getNumber());
            }
        };
        static final NumberGetter<EnumValueDescriptor> NUMBER_GETTER = new NumberGetter<EnumValueDescriptor>(){

            @Override
            public int getNumber(EnumValueDescriptor enumValueDescriptor) {
                return enumValueDescriptor.getNumber();
            }
        };
        private final int index;
        private DescriptorProtos.EnumValueDescriptorProto proto;
        private final String fullName;
        private final EnumDescriptor type;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.EnumValueDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public int getNumber() {
            return this.proto.getNumber();
        }

        public String toString() {
            return this.proto.getName();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public FileDescriptor getFile() {
            return this.type.file;
        }

        public EnumDescriptor getType() {
            return this.type;
        }

        public DescriptorProtos.EnumValueOptions getOptions() {
            return this.proto.getOptions();
        }

        private EnumValueDescriptor(DescriptorProtos.EnumValueDescriptorProto proto, FileDescriptor file, EnumDescriptor parent, int index) throws DescriptorValidationException {
            this.index = index;
            this.proto = proto;
            this.type = parent;
            this.fullName = parent.getFullName() + '.' + proto.getName();
            file.pool.addSymbol(this);
        }

        private EnumValueDescriptor(EnumDescriptor parent, Integer number) {
            String name = "UNKNOWN_ENUM_VALUE_" + parent.getName() + "_" + number;
            DescriptorProtos.EnumValueDescriptorProto proto = DescriptorProtos.EnumValueDescriptorProto.newBuilder().setName(name).setNumber(number).build();
            this.index = -1;
            this.proto = proto;
            this.type = parent;
            this.fullName = parent.getFullName() + '.' + proto.getName();
        }

        private void setProto(DescriptorProtos.EnumValueDescriptorProto proto) {
            this.proto = proto;
        }
    }

    public static final class EnumDescriptor
    extends GenericDescriptor
    implements Internal.EnumLiteMap<EnumValueDescriptor> {
        private final int index;
        private DescriptorProtos.EnumDescriptorProto proto;
        private final String fullName;
        private final FileDescriptor file;
        private final Descriptor containingType;
        private final EnumValueDescriptor[] values;
        private final EnumValueDescriptor[] valuesSortedByNumber;
        private final int distinctNumbers;
        private Map<Integer, WeakReference<EnumValueDescriptor>> unknownValues = null;
        private ReferenceQueue<EnumValueDescriptor> cleanupQueue = null;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.EnumDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        public boolean isClosed() {
            return this.getFile().getSyntax() != FileDescriptor.Syntax.PROTO3;
        }

        public Descriptor getContainingType() {
            return this.containingType;
        }

        public DescriptorProtos.EnumOptions getOptions() {
            return this.proto.getOptions();
        }

        public List<EnumValueDescriptor> getValues() {
            return Collections.unmodifiableList(Arrays.asList(this.values));
        }

        public boolean isReservedNumber(int number) {
            for (DescriptorProtos.EnumDescriptorProto.EnumReservedRange range : this.proto.getReservedRangeList()) {
                if (range.getStart() > number || number > range.getEnd()) continue;
                return true;
            }
            return false;
        }

        public boolean isReservedName(String name) {
            Internal.checkNotNull(name);
            for (String reservedName : this.proto.getReservedNameList()) {
                if (!reservedName.equals(name)) continue;
                return true;
            }
            return false;
        }

        public EnumValueDescriptor findValueByName(String name) {
            GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
            if (result instanceof EnumValueDescriptor) {
                return (EnumValueDescriptor)result;
            }
            return null;
        }

        @Override
        public EnumValueDescriptor findValueByNumber(int number) {
            return (EnumValueDescriptor)Descriptors.binarySearch(this.valuesSortedByNumber, this.distinctNumbers, EnumValueDescriptor.NUMBER_GETTER, number);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public EnumValueDescriptor findValueByNumberCreatingIfUnknown(int number) {
            EnumValueDescriptor result = this.findValueByNumber(number);
            if (result != null) {
                return result;
            }
            EnumDescriptor enumDescriptor = this;
            synchronized (enumDescriptor) {
                if (this.cleanupQueue == null) {
                    this.cleanupQueue = new ReferenceQueue();
                    this.unknownValues = new HashMap<Integer, WeakReference<EnumValueDescriptor>>();
                } else {
                    UnknownEnumValueReference toClean;
                    while ((toClean = (UnknownEnumValueReference)this.cleanupQueue.poll()) != null) {
                        this.unknownValues.remove(toClean.number);
                    }
                }
                WeakReference<EnumValueDescriptor> reference = this.unknownValues.get(number);
                EnumValueDescriptor enumValueDescriptor = result = reference == null ? null : (EnumValueDescriptor)reference.get();
                if (result == null) {
                    result = new EnumValueDescriptor(this, number);
                    this.unknownValues.put(number, new UnknownEnumValueReference(number, result));
                }
            }
            return result;
        }

        int getUnknownEnumValueDescriptorCount() {
            return this.unknownValues.size();
        }

        private EnumDescriptor(DescriptorProtos.EnumDescriptorProto proto, FileDescriptor file, Descriptor parent, int index) throws DescriptorValidationException {
            this.index = index;
            this.proto = proto;
            this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
            this.file = file;
            this.containingType = parent;
            if (proto.getValueCount() == 0) {
                throw new DescriptorValidationException((GenericDescriptor)this, "Enums must contain at least one value.");
            }
            this.values = new EnumValueDescriptor[proto.getValueCount()];
            for (int i = 0; i < proto.getValueCount(); ++i) {
                this.values[i] = new EnumValueDescriptor(proto.getValue(i), file, this, i);
            }
            this.valuesSortedByNumber = (EnumValueDescriptor[])this.values.clone();
            Arrays.sort(this.valuesSortedByNumber, EnumValueDescriptor.BY_NUMBER);
            int j = 0;
            for (int i = 1; i < proto.getValueCount(); ++i) {
                EnumValueDescriptor oldValue = this.valuesSortedByNumber[j];
                EnumValueDescriptor newValue = this.valuesSortedByNumber[i];
                if (oldValue.getNumber() == newValue.getNumber()) continue;
                this.valuesSortedByNumber[++j] = newValue;
            }
            this.distinctNumbers = j + 1;
            Arrays.fill(this.valuesSortedByNumber, this.distinctNumbers, proto.getValueCount(), null);
            file.pool.addSymbol(this);
        }

        private void setProto(DescriptorProtos.EnumDescriptorProto proto) {
            this.proto = proto;
            for (int i = 0; i < this.values.length; ++i) {
                this.values[i].setProto(proto.getValue(i));
            }
        }

        private static class UnknownEnumValueReference
        extends WeakReference<EnumValueDescriptor> {
            private final int number;

            private UnknownEnumValueReference(int number, EnumValueDescriptor descriptor) {
                super(descriptor);
                this.number = number;
            }
        }
    }

    public static final class FieldDescriptor
    extends GenericDescriptor
    implements Comparable<FieldDescriptor>,
    FieldSet.FieldDescriptorLite<FieldDescriptor> {
        private static final NumberGetter<FieldDescriptor> NUMBER_GETTER = new NumberGetter<FieldDescriptor>(){

            @Override
            public int getNumber(FieldDescriptor fieldDescriptor) {
                return fieldDescriptor.getNumber();
            }
        };
        private static final WireFormat.FieldType[] table = WireFormat.FieldType.values();
        private final int index;
        private DescriptorProtos.FieldDescriptorProto proto;
        private final String fullName;
        private String jsonName;
        private final FileDescriptor file;
        private final Descriptor extensionScope;
        private final boolean isProto3Optional;
        private Type type;
        private Descriptor containingType;
        private Descriptor messageType;
        private OneofDescriptor containingOneof;
        private EnumDescriptor enumType;
        private Object defaultValue;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.FieldDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public int getNumber() {
            return this.proto.getNumber();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        public String getJsonName() {
            String result = this.jsonName;
            if (result != null) {
                return result;
            }
            if (this.proto.hasJsonName()) {
                this.jsonName = this.proto.getJsonName();
                return this.jsonName;
            }
            this.jsonName = FieldDescriptor.fieldNameToJsonName(this.proto.getName());
            return this.jsonName;
        }

        public JavaType getJavaType() {
            return this.type.getJavaType();
        }

        @Override
        public WireFormat.JavaType getLiteJavaType() {
            return this.getLiteType().getJavaType();
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        public Type getType() {
            return this.type;
        }

        @Override
        public WireFormat.FieldType getLiteType() {
            return table[this.type.ordinal()];
        }

        public boolean needsUtf8Check() {
            if (this.type != Type.STRING) {
                return false;
            }
            if (this.getContainingType().getOptions().getMapEntry()) {
                return true;
            }
            if (this.getFile().getSyntax() == FileDescriptor.Syntax.PROTO3) {
                return true;
            }
            return this.getFile().getOptions().getJavaStringCheckUtf8();
        }

        public boolean isMapField() {
            return this.getType() == Type.MESSAGE && this.isRepeated() && this.getMessageType().getOptions().getMapEntry();
        }

        public boolean isRequired() {
            return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED;
        }

        public boolean isOptional() {
            return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;
        }

        @Override
        public boolean isRepeated() {
            return this.proto.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
        }

        @Override
        public boolean isPacked() {
            if (!this.isPackable()) {
                return false;
            }
            if (this.getFile().getSyntax() == FileDescriptor.Syntax.PROTO2) {
                return this.getOptions().getPacked();
            }
            return !this.getOptions().hasPacked() || this.getOptions().getPacked();
        }

        public boolean isPackable() {
            return this.isRepeated() && this.getLiteType().isPackable();
        }

        public boolean hasDefaultValue() {
            return this.proto.hasDefaultValue();
        }

        public Object getDefaultValue() {
            if (this.getJavaType() == JavaType.MESSAGE) {
                throw new UnsupportedOperationException("FieldDescriptor.getDefaultValue() called on an embedded message field.");
            }
            return this.defaultValue;
        }

        public DescriptorProtos.FieldOptions getOptions() {
            return this.proto.getOptions();
        }

        public boolean isExtension() {
            return this.proto.hasExtendee();
        }

        public Descriptor getContainingType() {
            return this.containingType;
        }

        public OneofDescriptor getContainingOneof() {
            return this.containingOneof;
        }

        public OneofDescriptor getRealContainingOneof() {
            return this.containingOneof != null && !this.containingOneof.isSynthetic() ? this.containingOneof : null;
        }

        @Deprecated
        public boolean hasOptionalKeyword() {
            return this.isProto3Optional || this.file.getSyntax() == FileDescriptor.Syntax.PROTO2 && this.isOptional() && this.getContainingOneof() == null;
        }

        public boolean hasPresence() {
            if (this.isRepeated()) {
                return false;
            }
            return this.getType() == Type.MESSAGE || this.getType() == Type.GROUP || this.getContainingOneof() != null || this.file.getSyntax() == FileDescriptor.Syntax.PROTO2;
        }

        public Descriptor getExtensionScope() {
            if (!this.isExtension()) {
                throw new UnsupportedOperationException(String.format("This field is not an extension. (%s)", this.fullName));
            }
            return this.extensionScope;
        }

        public Descriptor getMessageType() {
            if (this.getJavaType() != JavaType.MESSAGE) {
                throw new UnsupportedOperationException(String.format("This field is not of message type. (%s)", this.fullName));
            }
            return this.messageType;
        }

        public EnumDescriptor getEnumType() {
            if (this.getJavaType() != JavaType.ENUM) {
                throw new UnsupportedOperationException(String.format("This field is not of enum type. (%s)", this.fullName));
            }
            return this.enumType;
        }

        public boolean legacyEnumFieldTreatedAsClosed() {
            return this.getType() == Type.ENUM && this.getFile().getSyntax() == FileDescriptor.Syntax.PROTO2;
        }

        @Override
        public int compareTo(FieldDescriptor other) {
            if (other.containingType != this.containingType) {
                throw new IllegalArgumentException("FieldDescriptors can only be compared to other FieldDescriptors for fields of the same message type.");
            }
            return this.getNumber() - other.getNumber();
        }

        public String toString() {
            return this.getFullName();
        }

        private static String fieldNameToJsonName(String name) {
            int length = name.length();
            StringBuilder result = new StringBuilder(length);
            boolean isNextUpperCase = false;
            for (int i = 0; i < length; ++i) {
                char ch = name.charAt(i);
                if (ch == '_') {
                    isNextUpperCase = true;
                    continue;
                }
                if (isNextUpperCase) {
                    if ('a' <= ch && ch <= 'z') {
                        ch = (char)(ch - 97 + 65);
                    }
                    result.append(ch);
                    isNextUpperCase = false;
                    continue;
                }
                result.append(ch);
            }
            return result.toString();
        }

        private FieldDescriptor(DescriptorProtos.FieldDescriptorProto proto, FileDescriptor file, Descriptor parent, int index, boolean isExtension) throws DescriptorValidationException {
            this.index = index;
            this.proto = proto;
            this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
            this.file = file;
            if (proto.hasType()) {
                this.type = Type.valueOf(proto.getType());
            }
            this.isProto3Optional = proto.getProto3Optional();
            if (this.getNumber() <= 0) {
                throw new DescriptorValidationException((GenericDescriptor)this, "Field numbers must be positive integers.");
            }
            if (isExtension) {
                if (!proto.hasExtendee()) {
                    throw new DescriptorValidationException((GenericDescriptor)this, "FieldDescriptorProto.extendee not set for extension field.");
                }
                this.containingType = null;
                this.extensionScope = parent != null ? parent : null;
                if (proto.hasOneofIndex()) {
                    throw new DescriptorValidationException((GenericDescriptor)this, "FieldDescriptorProto.oneof_index set for extension field.");
                }
                this.containingOneof = null;
            } else {
                if (proto.hasExtendee()) {
                    throw new DescriptorValidationException((GenericDescriptor)this, "FieldDescriptorProto.extendee set for non-extension field.");
                }
                this.containingType = parent;
                if (proto.hasOneofIndex()) {
                    if (proto.getOneofIndex() < 0 || proto.getOneofIndex() >= parent.toProto().getOneofDeclCount()) {
                        throw new DescriptorValidationException((GenericDescriptor)this, "FieldDescriptorProto.oneof_index is out of range for type " + parent.getName());
                    }
                    this.containingOneof = parent.getOneofs().get(proto.getOneofIndex());
                    this.containingOneof.fieldCount++;
                } else {
                    this.containingOneof = null;
                }
                this.extensionScope = null;
            }
            file.pool.addSymbol(this);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private void crossLink() throws DescriptorValidationException {
            if (this.proto.hasExtendee()) {
                GenericDescriptor extendee = this.file.pool.lookupSymbol(this.proto.getExtendee(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
                if (!(extendee instanceof Descriptor)) {
                    throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getExtendee() + "\" is not a message type.");
                }
                this.containingType = (Descriptor)extendee;
                if (!this.getContainingType().isExtensionNumber(this.getNumber())) {
                    throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.getContainingType().getFullName() + "\" does not declare " + this.getNumber() + " as an extension number.");
                }
            }
            if (this.proto.hasTypeName()) {
                GenericDescriptor typeDescriptor = this.file.pool.lookupSymbol(this.proto.getTypeName(), this, DescriptorPool.SearchFilter.TYPES_ONLY);
                if (!this.proto.hasType()) {
                    if (typeDescriptor instanceof Descriptor) {
                        this.type = Type.MESSAGE;
                    } else {
                        if (!(typeDescriptor instanceof EnumDescriptor)) throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getTypeName() + "\" is not a type.");
                        this.type = Type.ENUM;
                    }
                }
                if (this.getJavaType() == JavaType.MESSAGE) {
                    if (!(typeDescriptor instanceof Descriptor)) {
                        throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getTypeName() + "\" is not a message type.");
                    }
                    this.messageType = (Descriptor)typeDescriptor;
                    if (this.proto.hasDefaultValue()) {
                        throw new DescriptorValidationException((GenericDescriptor)this, "Messages can't have default values.");
                    }
                } else {
                    if (this.getJavaType() != JavaType.ENUM) throw new DescriptorValidationException((GenericDescriptor)this, "Field with primitive type has type_name.");
                    if (!(typeDescriptor instanceof EnumDescriptor)) {
                        throw new DescriptorValidationException((GenericDescriptor)this, '\"' + this.proto.getTypeName() + "\" is not an enum type.");
                    }
                    this.enumType = (EnumDescriptor)typeDescriptor;
                }
            } else if (this.getJavaType() == JavaType.MESSAGE || this.getJavaType() == JavaType.ENUM) {
                throw new DescriptorValidationException((GenericDescriptor)this, "Field with message or enum type missing type_name.");
            }
            if (this.proto.getOptions().getPacked() && !this.isPackable()) {
                throw new DescriptorValidationException((GenericDescriptor)this, "[packed = true] can only be specified for repeated primitive fields.");
            }
            if (this.proto.hasDefaultValue()) {
                if (this.isRepeated()) {
                    throw new DescriptorValidationException((GenericDescriptor)this, "Repeated fields cannot have default values.");
                }
                try {
                    switch (this.getType()) {
                        case INT32: 
                        case SINT32: 
                        case SFIXED32: {
                            this.defaultValue = TextFormat.parseInt32(this.proto.getDefaultValue());
                            break;
                        }
                        case UINT32: 
                        case FIXED32: {
                            this.defaultValue = TextFormat.parseUInt32(this.proto.getDefaultValue());
                            break;
                        }
                        case INT64: 
                        case SINT64: 
                        case SFIXED64: {
                            this.defaultValue = TextFormat.parseInt64(this.proto.getDefaultValue());
                            break;
                        }
                        case UINT64: 
                        case FIXED64: {
                            this.defaultValue = TextFormat.parseUInt64(this.proto.getDefaultValue());
                            break;
                        }
                        case FLOAT: {
                            if (this.proto.getDefaultValue().equals("inf")) {
                                this.defaultValue = Float.valueOf(Float.POSITIVE_INFINITY);
                                break;
                            }
                            if (this.proto.getDefaultValue().equals("-inf")) {
                                this.defaultValue = Float.valueOf(Float.NEGATIVE_INFINITY);
                                break;
                            }
                            if (this.proto.getDefaultValue().equals("nan")) {
                                this.defaultValue = Float.valueOf(Float.NaN);
                                break;
                            }
                            this.defaultValue = Float.valueOf(this.proto.getDefaultValue());
                            break;
                        }
                        case DOUBLE: {
                            if (this.proto.getDefaultValue().equals("inf")) {
                                this.defaultValue = Double.POSITIVE_INFINITY;
                                break;
                            }
                            if (this.proto.getDefaultValue().equals("-inf")) {
                                this.defaultValue = Double.NEGATIVE_INFINITY;
                                break;
                            }
                            if (this.proto.getDefaultValue().equals("nan")) {
                                this.defaultValue = Double.NaN;
                                break;
                            }
                            this.defaultValue = Double.valueOf(this.proto.getDefaultValue());
                            break;
                        }
                        case BOOL: {
                            this.defaultValue = Boolean.valueOf(this.proto.getDefaultValue());
                            break;
                        }
                        case STRING: {
                            this.defaultValue = this.proto.getDefaultValue();
                            break;
                        }
                        case BYTES: {
                            try {
                                this.defaultValue = TextFormat.unescapeBytes(this.proto.getDefaultValue());
                                break;
                            }
                            catch (TextFormat.InvalidEscapeSequenceException e) {
                                throw new DescriptorValidationException(this, "Couldn't parse default value: " + e.getMessage(), e);
                            }
                        }
                        case ENUM: {
                            this.defaultValue = this.enumType.findValueByName(this.proto.getDefaultValue());
                            if (this.defaultValue != null) break;
                            throw new DescriptorValidationException((GenericDescriptor)this, "Unknown enum default value: \"" + this.proto.getDefaultValue() + '\"');
                        }
                        case MESSAGE: 
                        case GROUP: {
                            throw new DescriptorValidationException((GenericDescriptor)this, "Message type had default value.");
                        }
                    }
                }
                catch (NumberFormatException e) {
                    throw new DescriptorValidationException(this, "Could not parse default value: \"" + this.proto.getDefaultValue() + '\"', e);
                }
            } else if (this.isRepeated()) {
                this.defaultValue = Collections.emptyList();
            } else {
                switch (this.getJavaType()) {
                    case ENUM: {
                        this.defaultValue = this.enumType.getValues().get(0);
                        break;
                    }
                    case MESSAGE: {
                        this.defaultValue = null;
                        break;
                    }
                    default: {
                        this.defaultValue = this.getJavaType().defaultDefault;
                    }
                }
            }
            if (this.containingType == null || !this.containingType.getOptions().getMessageSetWireFormat()) return;
            if (!this.isExtension()) throw new DescriptorValidationException((GenericDescriptor)this, "MessageSets cannot have fields, only extensions.");
            if (this.isOptional() && this.getType() == Type.MESSAGE) return;
            throw new DescriptorValidationException((GenericDescriptor)this, "Extensions of MessageSets must be optional messages.");
        }

        private void setProto(DescriptorProtos.FieldDescriptorProto proto) {
            this.proto = proto;
        }

        @Override
        public MessageLite.Builder internalMergeFrom(MessageLite.Builder to, MessageLite from) {
            return ((Message.Builder)to).mergeFrom((Message)from);
        }

        static {
            if (Type.types.length != DescriptorProtos.FieldDescriptorProto.Type.values().length) {
                throw new RuntimeException("descriptor.proto has a new declared type but Descriptors.java wasn't updated.");
            }
        }

        public static enum JavaType {
            INT(0),
            LONG(0L),
            FLOAT(Float.valueOf(0.0f)),
            DOUBLE(0.0),
            BOOLEAN(false),
            STRING(""),
            BYTE_STRING(ByteString.EMPTY),
            ENUM(null),
            MESSAGE(null);

            private final Object defaultDefault;

            private JavaType(Object defaultDefault) {
                this.defaultDefault = defaultDefault;
            }
        }

        public static enum Type {
            DOUBLE(JavaType.DOUBLE),
            FLOAT(JavaType.FLOAT),
            INT64(JavaType.LONG),
            UINT64(JavaType.LONG),
            INT32(JavaType.INT),
            FIXED64(JavaType.LONG),
            FIXED32(JavaType.INT),
            BOOL(JavaType.BOOLEAN),
            STRING(JavaType.STRING),
            GROUP(JavaType.MESSAGE),
            MESSAGE(JavaType.MESSAGE),
            BYTES(JavaType.BYTE_STRING),
            UINT32(JavaType.INT),
            ENUM(JavaType.ENUM),
            SFIXED32(JavaType.INT),
            SFIXED64(JavaType.LONG),
            SINT32(JavaType.INT),
            SINT64(JavaType.LONG);

            private static final Type[] types;
            private final JavaType javaType;

            private Type(JavaType javaType) {
                this.javaType = javaType;
            }

            public DescriptorProtos.FieldDescriptorProto.Type toProto() {
                return DescriptorProtos.FieldDescriptorProto.Type.forNumber(this.ordinal() + 1);
            }

            public JavaType getJavaType() {
                return this.javaType;
            }

            public static Type valueOf(DescriptorProtos.FieldDescriptorProto.Type type) {
                return types[type.getNumber() - 1];
            }

            static {
                types = Type.values();
            }
        }
    }

    public static final class Descriptor
    extends GenericDescriptor {
        private final int index;
        private DescriptorProtos.DescriptorProto proto;
        private final String fullName;
        private final FileDescriptor file;
        private final Descriptor containingType;
        private final Descriptor[] nestedTypes;
        private final EnumDescriptor[] enumTypes;
        private final FieldDescriptor[] fields;
        private final FieldDescriptor[] fieldsSortedByNumber;
        private final FieldDescriptor[] extensions;
        private final OneofDescriptor[] oneofs;
        private final int realOneofCount;
        private final int[] extensionRangeLowerBounds;
        private final int[] extensionRangeUpperBounds;

        public int getIndex() {
            return this.index;
        }

        @Override
        public DescriptorProtos.DescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public String getFullName() {
            return this.fullName;
        }

        @Override
        public FileDescriptor getFile() {
            return this.file;
        }

        public Descriptor getContainingType() {
            return this.containingType;
        }

        public DescriptorProtos.MessageOptions getOptions() {
            return this.proto.getOptions();
        }

        public List<FieldDescriptor> getFields() {
            return Collections.unmodifiableList(Arrays.asList(this.fields));
        }

        public List<OneofDescriptor> getOneofs() {
            return Collections.unmodifiableList(Arrays.asList(this.oneofs));
        }

        public List<OneofDescriptor> getRealOneofs() {
            return Collections.unmodifiableList(Arrays.asList(this.oneofs).subList(0, this.realOneofCount));
        }

        public List<FieldDescriptor> getExtensions() {
            return Collections.unmodifiableList(Arrays.asList(this.extensions));
        }

        public List<Descriptor> getNestedTypes() {
            return Collections.unmodifiableList(Arrays.asList(this.nestedTypes));
        }

        public List<EnumDescriptor> getEnumTypes() {
            return Collections.unmodifiableList(Arrays.asList(this.enumTypes));
        }

        public boolean isExtensionNumber(int number) {
            int index = Arrays.binarySearch(this.extensionRangeLowerBounds, number);
            if (index < 0) {
                index = ~index - 1;
            }
            return index >= 0 && number < this.extensionRangeUpperBounds[index];
        }

        public boolean isReservedNumber(int number) {
            for (DescriptorProtos.DescriptorProto.ReservedRange range : this.proto.getReservedRangeList()) {
                if (range.getStart() > number || number >= range.getEnd()) continue;
                return true;
            }
            return false;
        }

        public boolean isReservedName(String name) {
            Internal.checkNotNull(name);
            for (String reservedName : this.proto.getReservedNameList()) {
                if (!reservedName.equals(name)) continue;
                return true;
            }
            return false;
        }

        public boolean isExtendable() {
            return !this.proto.getExtensionRangeList().isEmpty();
        }

        public FieldDescriptor findFieldByName(String name) {
            GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
            if (result instanceof FieldDescriptor) {
                return (FieldDescriptor)result;
            }
            return null;
        }

        public FieldDescriptor findFieldByNumber(int number) {
            return (FieldDescriptor)Descriptors.binarySearch(this.fieldsSortedByNumber, this.fieldsSortedByNumber.length, FieldDescriptor.NUMBER_GETTER, number);
        }

        public Descriptor findNestedTypeByName(String name) {
            GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
            if (result instanceof Descriptor) {
                return (Descriptor)result;
            }
            return null;
        }

        public EnumDescriptor findEnumTypeByName(String name) {
            GenericDescriptor result = this.file.pool.findSymbol(this.fullName + '.' + name);
            if (result instanceof EnumDescriptor) {
                return (EnumDescriptor)result;
            }
            return null;
        }

        Descriptor(String fullname) throws DescriptorValidationException {
            String name = fullname;
            String packageName = "";
            int pos = fullname.lastIndexOf(46);
            if (pos != -1) {
                name = fullname.substring(pos + 1);
                packageName = fullname.substring(0, pos);
            }
            this.index = 0;
            this.proto = DescriptorProtos.DescriptorProto.newBuilder().setName(name).addExtensionRange(DescriptorProtos.DescriptorProto.ExtensionRange.newBuilder().setStart(1).setEnd(0x20000000).build()).build();
            this.fullName = fullname;
            this.containingType = null;
            this.nestedTypes = EMPTY_DESCRIPTORS;
            this.enumTypes = EMPTY_ENUM_DESCRIPTORS;
            this.fields = EMPTY_FIELD_DESCRIPTORS;
            this.fieldsSortedByNumber = EMPTY_FIELD_DESCRIPTORS;
            this.extensions = EMPTY_FIELD_DESCRIPTORS;
            this.oneofs = EMPTY_ONEOF_DESCRIPTORS;
            this.realOneofCount = 0;
            this.file = new FileDescriptor(packageName, this);
            this.extensionRangeLowerBounds = new int[]{1};
            this.extensionRangeUpperBounds = new int[]{0x20000000};
        }

        /*
         * WARNING - void declaration
         */
        private Descriptor(DescriptorProtos.DescriptorProto proto, FileDescriptor file, Descriptor parent, int index) throws DescriptorValidationException {
            int i;
            this.index = index;
            this.proto = proto;
            this.fullName = Descriptors.computeFullName(file, parent, proto.getName());
            this.file = file;
            this.containingType = parent;
            this.oneofs = proto.getOneofDeclCount() > 0 ? new OneofDescriptor[proto.getOneofDeclCount()] : EMPTY_ONEOF_DESCRIPTORS;
            for (i = 0; i < proto.getOneofDeclCount(); ++i) {
                this.oneofs[i] = new OneofDescriptor(proto.getOneofDecl(i), file, this, i);
            }
            this.nestedTypes = proto.getNestedTypeCount() > 0 ? new Descriptor[proto.getNestedTypeCount()] : EMPTY_DESCRIPTORS;
            for (i = 0; i < proto.getNestedTypeCount(); ++i) {
                this.nestedTypes[i] = new Descriptor(proto.getNestedType(i), file, this, i);
            }
            this.enumTypes = proto.getEnumTypeCount() > 0 ? new EnumDescriptor[proto.getEnumTypeCount()] : EMPTY_ENUM_DESCRIPTORS;
            for (i = 0; i < proto.getEnumTypeCount(); ++i) {
                this.enumTypes[i] = new EnumDescriptor(proto.getEnumType(i), file, this, i);
            }
            this.fields = proto.getFieldCount() > 0 ? new FieldDescriptor[proto.getFieldCount()] : EMPTY_FIELD_DESCRIPTORS;
            for (i = 0; i < proto.getFieldCount(); ++i) {
                this.fields[i] = new FieldDescriptor(proto.getField(i), file, this, i, false);
            }
            this.fieldsSortedByNumber = proto.getFieldCount() > 0 ? (FieldDescriptor[])this.fields.clone() : EMPTY_FIELD_DESCRIPTORS;
            this.extensions = proto.getExtensionCount() > 0 ? new FieldDescriptor[proto.getExtensionCount()] : EMPTY_FIELD_DESCRIPTORS;
            for (i = 0; i < proto.getExtensionCount(); ++i) {
                this.extensions[i] = new FieldDescriptor(proto.getExtension(i), file, this, i, true);
            }
            for (i = 0; i < proto.getOneofDeclCount(); ++i) {
                OneofDescriptor.access$2502(this.oneofs[i], new FieldDescriptor[this.oneofs[i].getFieldCount()]);
                this.oneofs[i].fieldCount = 0;
            }
            for (i = 0; i < proto.getFieldCount(); ++i) {
                OneofDescriptor oneofDescriptor = this.fields[i].getContainingOneof();
                if (oneofDescriptor == null) continue;
                ((OneofDescriptor)oneofDescriptor).fields[((OneofDescriptor)oneofDescriptor).fieldCount++] = this.fields[i];
            }
            int syntheticOneofCount = 0;
            for (OneofDescriptor oneof : this.oneofs) {
                if (oneof.isSynthetic()) {
                    ++syntheticOneofCount;
                    continue;
                }
                if (syntheticOneofCount <= 0) continue;
                throw new DescriptorValidationException((GenericDescriptor)this, "Synthetic oneofs must come last.");
            }
            this.realOneofCount = this.oneofs.length - syntheticOneofCount;
            file.pool.addSymbol(this);
            if (proto.getExtensionRangeCount() > 0) {
                this.extensionRangeLowerBounds = new int[proto.getExtensionRangeCount()];
                this.extensionRangeUpperBounds = new int[proto.getExtensionRangeCount()];
                boolean bl = false;
                for (DescriptorProtos.DescriptorProto.ExtensionRange range : proto.getExtensionRangeList()) {
                    void var6_9;
                    this.extensionRangeLowerBounds[var6_9] = range.getStart();
                    this.extensionRangeUpperBounds[var6_9] = range.getEnd();
                    ++var6_9;
                }
                Arrays.sort(this.extensionRangeLowerBounds);
                Arrays.sort(this.extensionRangeUpperBounds);
            } else {
                this.extensionRangeLowerBounds = EMPTY_INT_ARRAY;
                this.extensionRangeUpperBounds = EMPTY_INT_ARRAY;
            }
        }

        private void crossLink() throws DescriptorValidationException {
            for (Descriptor descriptor : this.nestedTypes) {
                descriptor.crossLink();
            }
            for (GenericDescriptor genericDescriptor : this.fields) {
                ((FieldDescriptor)genericDescriptor).crossLink();
            }
            Arrays.sort(this.fieldsSortedByNumber);
            this.validateNoDuplicateFieldNumbers();
            for (GenericDescriptor genericDescriptor : this.extensions) {
                ((FieldDescriptor)genericDescriptor).crossLink();
            }
        }

        private void validateNoDuplicateFieldNumbers() throws DescriptorValidationException {
            int i = 0;
            while (i + 1 < this.fieldsSortedByNumber.length) {
                FieldDescriptor old = this.fieldsSortedByNumber[i];
                FieldDescriptor field = this.fieldsSortedByNumber[i + 1];
                if (old.getNumber() == field.getNumber()) {
                    throw new DescriptorValidationException((GenericDescriptor)field, "Field number " + field.getNumber() + " has already been used in \"" + field.getContainingType().getFullName() + "\" by field \"" + old.getName() + "\".");
                }
                ++i;
            }
        }

        private void setProto(DescriptorProtos.DescriptorProto proto) {
            int i;
            this.proto = proto;
            for (i = 0; i < this.nestedTypes.length; ++i) {
                this.nestedTypes[i].setProto(proto.getNestedType(i));
            }
            for (i = 0; i < this.oneofs.length; ++i) {
                this.oneofs[i].setProto(proto.getOneofDecl(i));
            }
            for (i = 0; i < this.enumTypes.length; ++i) {
                this.enumTypes[i].setProto(proto.getEnumType(i));
            }
            for (i = 0; i < this.fields.length; ++i) {
                this.fields[i].setProto(proto.getField(i));
            }
            for (i = 0; i < this.extensions.length; ++i) {
                this.extensions[i].setProto(proto.getExtension(i));
            }
        }
    }

    public static final class FileDescriptor
    extends GenericDescriptor {
        private DescriptorProtos.FileDescriptorProto proto;
        private final Descriptor[] messageTypes;
        private final EnumDescriptor[] enumTypes;
        private final ServiceDescriptor[] services;
        private final FieldDescriptor[] extensions;
        private final FileDescriptor[] dependencies;
        private final FileDescriptor[] publicDependencies;
        private final DescriptorPool pool;

        @Override
        public DescriptorProtos.FileDescriptorProto toProto() {
            return this.proto;
        }

        @Override
        public String getName() {
            return this.proto.getName();
        }

        @Override
        public FileDescriptor getFile() {
            return this;
        }

        @Override
        public String getFullName() {
            return this.proto.getName();
        }

        public String getPackage() {
            return this.proto.getPackage();
        }

        public DescriptorProtos.FileOptions getOptions() {
            return this.proto.getOptions();
        }

        public List<Descriptor> getMessageTypes() {
            return Collections.unmodifiableList(Arrays.asList(this.messageTypes));
        }

        public List<EnumDescriptor> getEnumTypes() {
            return Collections.unmodifiableList(Arrays.asList(this.enumTypes));
        }

        public List<ServiceDescriptor> getServices() {
            return Collections.unmodifiableList(Arrays.asList(this.services));
        }

        public List<FieldDescriptor> getExtensions() {
            return Collections.unmodifiableList(Arrays.asList(this.extensions));
        }

        public List<FileDescriptor> getDependencies() {
            return Collections.unmodifiableList(Arrays.asList(this.dependencies));
        }

        public List<FileDescriptor> getPublicDependencies() {
            return Collections.unmodifiableList(Arrays.asList(this.publicDependencies));
        }

        @Deprecated
        public Syntax getSyntax() {
            if (Syntax.PROTO3.name.equals(this.proto.getSyntax())) {
                return Syntax.PROTO3;
            }
            if (Syntax.EDITIONS.name.equals(this.proto.getSyntax())) {
                return Syntax.EDITIONS;
            }
            return Syntax.PROTO2;
        }

        public DescriptorProtos.Edition getEdition() {
            return this.proto.getEdition();
        }

        public String getEditionName() {
            if (this.proto.getEdition().equals(DescriptorProtos.Edition.EDITION_UNKNOWN)) {
                return "";
            }
            return this.proto.getEdition().name().substring("EDITION_".length());
        }

        public void copyHeadingTo(DescriptorProtos.FileDescriptorProto.Builder protoBuilder) {
            protoBuilder.setName(this.getName()).setSyntax(this.getSyntax().name);
            if (!this.getPackage().isEmpty()) {
                protoBuilder.setPackage(this.getPackage());
            }
            if (this.getSyntax().equals((Object)Syntax.EDITIONS)) {
                protoBuilder.setEdition(this.getEdition());
            }
            if (!this.getOptions().equals(DescriptorProtos.FileOptions.getDefaultInstance())) {
                protoBuilder.setOptions(this.getOptions());
            }
        }

        public Descriptor findMessageTypeByName(String name) {
            GenericDescriptor result;
            if (name.indexOf(46) != -1) {
                return null;
            }
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
                name = packageName + '.' + name;
            }
            if ((result = this.pool.findSymbol(name)) instanceof Descriptor && result.getFile() == this) {
                return (Descriptor)result;
            }
            return null;
        }

        public EnumDescriptor findEnumTypeByName(String name) {
            GenericDescriptor result;
            if (name.indexOf(46) != -1) {
                return null;
            }
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
                name = packageName + '.' + name;
            }
            if ((result = this.pool.findSymbol(name)) instanceof EnumDescriptor && result.getFile() == this) {
                return (EnumDescriptor)result;
            }
            return null;
        }

        public ServiceDescriptor findServiceByName(String name) {
            GenericDescriptor result;
            if (name.indexOf(46) != -1) {
                return null;
            }
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
                name = packageName + '.' + name;
            }
            if ((result = this.pool.findSymbol(name)) instanceof ServiceDescriptor && result.getFile() == this) {
                return (ServiceDescriptor)result;
            }
            return null;
        }

        public FieldDescriptor findExtensionByName(String name) {
            GenericDescriptor result;
            if (name.indexOf(46) != -1) {
                return null;
            }
            String packageName = this.getPackage();
            if (!packageName.isEmpty()) {
                name = packageName + '.' + name;
            }
            if ((result = this.pool.findSymbol(name)) instanceof FieldDescriptor && result.getFile() == this) {
                return (FieldDescriptor)result;
            }
            return null;
        }

        public static FileDescriptor buildFrom(DescriptorProtos.FileDescriptorProto proto, FileDescriptor[] dependencies) throws DescriptorValidationException {
            return FileDescriptor.buildFrom(proto, dependencies, false);
        }

        public static FileDescriptor buildFrom(DescriptorProtos.FileDescriptorProto proto, FileDescriptor[] dependencies, boolean allowUnknownDependencies) throws DescriptorValidationException {
            DescriptorPool pool = new DescriptorPool(dependencies, allowUnknownDependencies);
            FileDescriptor result = new FileDescriptor(proto, dependencies, pool, allowUnknownDependencies);
            result.crossLink();
            return result;
        }

        private static byte[] latin1Cat(String[] strings) {
            if (strings.length == 1) {
                return strings[0].getBytes(Internal.ISO_8859_1);
            }
            StringBuilder descriptorData = new StringBuilder();
            for (String part : strings) {
                descriptorData.append(part);
            }
            return descriptorData.toString().getBytes(Internal.ISO_8859_1);
        }

        private static FileDescriptor[] findDescriptors(Class<?> descriptorOuterClass, String[] dependencyClassNames, String[] dependencyFileNames) {
            ArrayList<FileDescriptor> descriptors = new ArrayList<FileDescriptor>();
            for (int i = 0; i < dependencyClassNames.length; ++i) {
                try {
                    Class<?> clazz = descriptorOuterClass.getClassLoader().loadClass(dependencyClassNames[i]);
                    descriptors.add((FileDescriptor)clazz.getField("descriptor").get(null));
                    continue;
                }
                catch (Exception e) {
                    logger.warning("Descriptors for \"" + dependencyFileNames[i] + "\" can not be found.");
                }
            }
            return descriptors.toArray(new FileDescriptor[0]);
        }

        @Deprecated
        public static void internalBuildGeneratedFileFrom(String[] descriptorDataParts, FileDescriptor[] dependencies, InternalDescriptorAssigner descriptorAssigner) {
            FileDescriptor result;
            DescriptorProtos.FileDescriptorProto proto;
            byte[] descriptorBytes = FileDescriptor.latin1Cat(descriptorDataParts);
            try {
                proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes);
            }
            catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", e);
            }
            try {
                result = FileDescriptor.buildFrom(proto, dependencies, true);
            }
            catch (DescriptorValidationException e) {
                throw new IllegalArgumentException("Invalid embedded descriptor for \"" + proto.getName() + "\".", e);
            }
            ExtensionRegistry registry = descriptorAssigner.assignDescriptors(result);
            if (registry != null) {
                try {
                    proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes, (ExtensionRegistryLite)registry);
                }
                catch (InvalidProtocolBufferException e) {
                    throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", e);
                }
                result.setProto(proto);
            }
        }

        public static FileDescriptor internalBuildGeneratedFileFrom(String[] descriptorDataParts, FileDescriptor[] dependencies) {
            DescriptorProtos.FileDescriptorProto proto;
            byte[] descriptorBytes = FileDescriptor.latin1Cat(descriptorDataParts);
            try {
                proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptorBytes);
            }
            catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", e);
            }
            try {
                return FileDescriptor.buildFrom(proto, dependencies, true);
            }
            catch (DescriptorValidationException e) {
                throw new IllegalArgumentException("Invalid embedded descriptor for \"" + proto.getName() + "\".", e);
            }
        }

        @Deprecated
        public static void internalBuildGeneratedFileFrom(String[] descriptorDataParts, Class<?> descriptorOuterClass, String[] dependencyClassNames, String[] dependencyFileNames, InternalDescriptorAssigner descriptorAssigner) {
            FileDescriptor[] dependencies = FileDescriptor.findDescriptors(descriptorOuterClass, dependencyClassNames, dependencyFileNames);
            FileDescriptor.internalBuildGeneratedFileFrom(descriptorDataParts, dependencies, descriptorAssigner);
        }

        public static FileDescriptor internalBuildGeneratedFileFrom(String[] descriptorDataParts, Class<?> descriptorOuterClass, String[] dependencyClassNames, String[] dependencyFileNames) {
            FileDescriptor[] dependencies = FileDescriptor.findDescriptors(descriptorOuterClass, dependencyClassNames, dependencyFileNames);
            return FileDescriptor.internalBuildGeneratedFileFrom(descriptorDataParts, dependencies);
        }

        public static void internalUpdateFileDescriptor(FileDescriptor descriptor, ExtensionRegistry registry) {
            ByteString bytes = descriptor.proto.toByteString();
            try {
                DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(bytes, (ExtensionRegistryLite)registry);
                descriptor.setProto(proto);
            }
            catch (InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse protocol buffer descriptor for generated code.", e);
            }
        }

        private FileDescriptor(DescriptorProtos.FileDescriptorProto proto, FileDescriptor[] dependencies, DescriptorPool pool, boolean allowUnknownDependencies) throws DescriptorValidationException {
            int i;
            this.pool = pool;
            this.proto = proto;
            this.dependencies = (FileDescriptor[])dependencies.clone();
            HashMap<String, FileDescriptor> nameToFileMap = new HashMap<String, FileDescriptor>();
            for (FileDescriptor file : dependencies) {
                nameToFileMap.put(file.getName(), file);
            }
            ArrayList<FileDescriptor> publicDependencies = new ArrayList<FileDescriptor>();
            for (i = 0; i < proto.getPublicDependencyCount(); ++i) {
                int index = proto.getPublicDependency(i);
                if (index < 0 || index >= proto.getDependencyCount()) {
                    throw new DescriptorValidationException(this, "Invalid public dependency index.");
                }
                String name = proto.getDependency(index);
                FileDescriptor file = (FileDescriptor)nameToFileMap.get(name);
                if (file == null) {
                    if (allowUnknownDependencies) continue;
                    throw new DescriptorValidationException(this, "Invalid public dependency: " + name);
                }
                publicDependencies.add(file);
            }
            this.publicDependencies = new FileDescriptor[publicDependencies.size()];
            publicDependencies.toArray(this.publicDependencies);
            pool.addPackage(this.getPackage(), this);
            this.messageTypes = proto.getMessageTypeCount() > 0 ? new Descriptor[proto.getMessageTypeCount()] : EMPTY_DESCRIPTORS;
            for (i = 0; i < proto.getMessageTypeCount(); ++i) {
                this.messageTypes[i] = new Descriptor(proto.getMessageType(i), this, null, i);
            }
            this.enumTypes = proto.getEnumTypeCount() > 0 ? new EnumDescriptor[proto.getEnumTypeCount()] : EMPTY_ENUM_DESCRIPTORS;
            for (i = 0; i < proto.getEnumTypeCount(); ++i) {
                this.enumTypes[i] = new EnumDescriptor(proto.getEnumType(i), this, null, i);
            }
            this.services = proto.getServiceCount() > 0 ? new ServiceDescriptor[proto.getServiceCount()] : EMPTY_SERVICE_DESCRIPTORS;
            for (i = 0; i < proto.getServiceCount(); ++i) {
                this.services[i] = new ServiceDescriptor(proto.getService(i), this, i);
            }
            this.extensions = proto.getExtensionCount() > 0 ? new FieldDescriptor[proto.getExtensionCount()] : EMPTY_FIELD_DESCRIPTORS;
            for (i = 0; i < proto.getExtensionCount(); ++i) {
                this.extensions[i] = new FieldDescriptor(proto.getExtension(i), this, null, i, true);
            }
        }

        FileDescriptor(String packageName, Descriptor message) throws DescriptorValidationException {
            this.pool = new DescriptorPool(new FileDescriptor[0], true);
            this.proto = DescriptorProtos.FileDescriptorProto.newBuilder().setName(message.getFullName() + ".placeholder.proto").setPackage(packageName).addMessageType(message.toProto()).build();
            this.dependencies = new FileDescriptor[0];
            this.publicDependencies = new FileDescriptor[0];
            this.messageTypes = new Descriptor[]{message};
            this.enumTypes = EMPTY_ENUM_DESCRIPTORS;
            this.services = EMPTY_SERVICE_DESCRIPTORS;
            this.extensions = EMPTY_FIELD_DESCRIPTORS;
            this.pool.addPackage(packageName, this);
            this.pool.addSymbol(message);
        }

        private void crossLink() throws DescriptorValidationException {
            for (Descriptor descriptor : this.messageTypes) {
                descriptor.crossLink();
            }
            for (GenericDescriptor genericDescriptor : this.services) {
                ((ServiceDescriptor)genericDescriptor).crossLink();
            }
            for (GenericDescriptor genericDescriptor : this.extensions) {
                ((FieldDescriptor)genericDescriptor).crossLink();
            }
        }

        private void setProto(DescriptorProtos.FileDescriptorProto proto) {
            int i;
            this.proto = proto;
            for (i = 0; i < this.messageTypes.length; ++i) {
                this.messageTypes[i].setProto(proto.getMessageType(i));
            }
            for (i = 0; i < this.enumTypes.length; ++i) {
                this.enumTypes[i].setProto(proto.getEnumType(i));
            }
            for (i = 0; i < this.services.length; ++i) {
                this.services[i].setProto(proto.getService(i));
            }
            for (i = 0; i < this.extensions.length; ++i) {
                this.extensions[i].setProto(proto.getExtension(i));
            }
        }

        @Deprecated
        public static interface InternalDescriptorAssigner {
            public ExtensionRegistry assignDescriptors(FileDescriptor var1);
        }

        @Deprecated
        public static enum Syntax {
            UNKNOWN("unknown"),
            PROTO2("proto2"),
            PROTO3("proto3"),
            EDITIONS("editions");

            private final String name;

            private Syntax(String name) {
                this.name = name;
            }
        }
    }
}

