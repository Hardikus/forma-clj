/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package forma.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleArray implements org.apache.thrift.TBase<DoubleArray, DoubleArray._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DoubleArray");

  private static final org.apache.thrift.protocol.TField DOUBLES_FIELD_DESC = new org.apache.thrift.protocol.TField("doubles", org.apache.thrift.protocol.TType.LIST, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new DoubleArrayStandardSchemeFactory());
    schemes.put(TupleScheme.class, new DoubleArrayTupleSchemeFactory());
  }

  public List<Double> doubles; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    DOUBLES((short)1, "doubles");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // DOUBLES
          return DOUBLES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.DOUBLES, new org.apache.thrift.meta_data.FieldMetaData("doubles", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DoubleArray.class, metaDataMap);
  }

  public DoubleArray() {
  }

  public DoubleArray(
    List<Double> doubles)
  {
    this();
    this.doubles = doubles;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DoubleArray(DoubleArray other) {
    if (other.isSetDoubles()) {
      List<Double> __this__doubles = new ArrayList<Double>();
      for (Double other_element : other.doubles) {
        __this__doubles.add(other_element);
      }
      this.doubles = __this__doubles;
    }
  }

  public DoubleArray deepCopy() {
    return new DoubleArray(this);
  }

  @Override
  public void clear() {
    this.doubles = null;
  }

  public int getDoublesSize() {
    return (this.doubles == null) ? 0 : this.doubles.size();
  }

  public java.util.Iterator<Double> getDoublesIterator() {
    return (this.doubles == null) ? null : this.doubles.iterator();
  }

  public void addToDoubles(double elem) {
    if (this.doubles == null) {
      this.doubles = new ArrayList<Double>();
    }
    this.doubles.add(elem);
  }

  public List<Double> getDoubles() {
    return this.doubles;
  }

  public DoubleArray setDoubles(List<Double> doubles) {
    this.doubles = doubles;
    return this;
  }

  public void unsetDoubles() {
    this.doubles = null;
  }

  /** Returns true if field doubles is set (has been assigned a value) and false otherwise */
  public boolean isSetDoubles() {
    return this.doubles != null;
  }

  public void setDoublesIsSet(boolean value) {
    if (!value) {
      this.doubles = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case DOUBLES:
      if (value == null) {
        unsetDoubles();
      } else {
        setDoubles((List<Double>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case DOUBLES:
      return getDoubles();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case DOUBLES:
      return isSetDoubles();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof DoubleArray)
      return this.equals((DoubleArray)that);
    return false;
  }

  public boolean equals(DoubleArray that) {
    if (that == null)
      return false;

    boolean this_present_doubles = true && this.isSetDoubles();
    boolean that_present_doubles = true && that.isSetDoubles();
    if (this_present_doubles || that_present_doubles) {
      if (!(this_present_doubles && that_present_doubles))
        return false;
      if (!this.doubles.equals(that.doubles))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_doubles = true && (isSetDoubles());
    builder.append(present_doubles);
    if (present_doubles)
      builder.append(doubles);

    return builder.toHashCode();
  }

  public int compareTo(DoubleArray other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    DoubleArray typedOther = (DoubleArray)other;

    lastComparison = Boolean.valueOf(isSetDoubles()).compareTo(typedOther.isSetDoubles());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDoubles()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.doubles, typedOther.doubles);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DoubleArray(");
    boolean first = true;

    sb.append("doubles:");
    if (this.doubles == null) {
      sb.append("null");
    } else {
      sb.append(this.doubles);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DoubleArrayStandardSchemeFactory implements SchemeFactory {
    public DoubleArrayStandardScheme getScheme() {
      return new DoubleArrayStandardScheme();
    }
  }

  private static class DoubleArrayStandardScheme extends StandardScheme<DoubleArray> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DoubleArray struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // DOUBLES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                struct.doubles = new ArrayList<Double>(_list24.size);
                for (int _i25 = 0; _i25 < _list24.size; ++_i25)
                {
                  double _elem26; // required
                  _elem26 = iprot.readDouble();
                  struct.doubles.add(_elem26);
                }
                iprot.readListEnd();
              }
              struct.setDoublesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DoubleArray struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.doubles != null) {
        oprot.writeFieldBegin(DOUBLES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.doubles.size()));
          for (double _iter27 : struct.doubles)
          {
            oprot.writeDouble(_iter27);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DoubleArrayTupleSchemeFactory implements SchemeFactory {
    public DoubleArrayTupleScheme getScheme() {
      return new DoubleArrayTupleScheme();
    }
  }

  private static class DoubleArrayTupleScheme extends TupleScheme<DoubleArray> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DoubleArray struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetDoubles()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetDoubles()) {
        {
          oprot.writeI32(struct.doubles.size());
          for (double _iter28 : struct.doubles)
          {
            oprot.writeDouble(_iter28);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DoubleArray struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list29 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.doubles = new ArrayList<Double>(_list29.size);
          for (int _i30 = 0; _i30 < _list29.size; ++_i30)
          {
            double _elem31; // required
            _elem31 = iprot.readDouble();
            struct.doubles.add(_elem31);
          }
        }
        struct.setDoublesIsSet(true);
      }
    }
  }

}
