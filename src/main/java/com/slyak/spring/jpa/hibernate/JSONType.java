package com.slyak.spring.jpa.hibernate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Column;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;

public class JSONType implements UserType, DynamicParameterizedType, Serializable {
    private static final long serialVersionUID = 352044032843534075L;
    private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class, JSONType.class.getName());
    public static final String TYPE = "com.slyak.spring.jpa.hibernate.JSONType";
    public static final String CLASS_NAME = "class";
    private int sqlType = Types.VARCHAR;
    private Type type = Object.class;

    @Override
    public int[] sqlTypes() {
        return new int[]{sqlType};
    }

    @Override
    public Class returnedClass() {
        if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else {
            return (Class) type;
        }
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        String value = extractString(rs.getObject(names[0]));
        if (rs.wasNull() || StringUtils.isEmpty(value)) {
            if (LOG.isTraceEnabled()) {
                LOG.tracev("Returning null as column {0}", names[0]);
            }
            return null;
        } else if (type == Object.class) {
            return JSON.parse(value);
        } else {
            return JSON.parseObject(value, type);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value == null) {
            if (LOG.isTraceEnabled()) {
                LOG.tracev("Binding null to parameter: {0}", index);
            }
            st.setNull(index, sqlType);
        } else {
            String json;
            if (type == Object.class) {
                json = JSON.toJSONString(value, SerializerFeature.WriteClassName);
            } else {
                json = JSON.toJSONString(value);
            }
            if (sqlType == Types.CLOB) {
                StringReader sr = new StringReader(json);
                st.setCharacterStream(index, sr, json.length());
            } else {
                st.setObject(index, json, sqlType);
            }
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
/*      if (value instanceof JSONObject) {
            return ((JSONObject) value).clone();
        } else if (value instanceof Cloneable) {
            return ObjectUtils.clone(value);
        } else if (value instanceof Serializable) {
            return SerializationHelper.clone((Serializable) value);
        } else {
            return value;
        }*/
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @SuppressWarnings("unchecked")
    public void setParameterValues(Properties parameters) {
        try {
            Class eClass = ReflectHelper.classForName(parameters.getProperty(DynamicParameterizedType.ENTITY));
            Field field = ReflectionUtils.findField(eClass, parameters.getProperty(DynamicParameterizedType.PROPERTY));
            Type fieldType = field.getGenericType();
            if (fieldType instanceof Class || fieldType instanceof ParameterizedType) {
                type = fieldType;
            }
            parseSqlType(field.getAnnotations());
            return;
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        final DynamicParameterizedType.ParameterType reader = (DynamicParameterizedType.ParameterType) parameters.get(
                DynamicParameterizedType.PARAMETER_TYPE);
        if (reader != null) {
            type = reader.getReturnedClass();
            parseSqlType(reader.getAnnotationsMethod());
        } else {
            try {
                type = ReflectHelper.classForName((String) parameters.get(CLASS_NAME));
            } catch (ClassNotFoundException exception) {
                throw new HibernateException("class not found", exception);
            }
        }
    }

    private void parseSqlType(Annotation[] anns) {
        for (Annotation an : anns) {
            if (an instanceof Column) {
                int length = ((Column) an).length();
                if (length > 4000) {
                    sqlType = Types.CLOB;
                }
                break;
            }
        }
    }

    private static String extractString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Reader) {
            return DataHelper.extractString((Reader) value);
        }
        if (value instanceof Clob) {
            return DataHelper.extractString((Clob) value);
        }
        return null;
    }
}
