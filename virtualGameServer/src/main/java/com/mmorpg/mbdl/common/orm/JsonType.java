package com.mmorpg.mbdl.common.orm;


import com.mmorpg.mbdl.framework.common.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 自定义hibernate字段类型
 *
 * @author Sando Geek
 * @since v1.0 2019/1/2
 **/
public class JsonType implements UserType {


    public static final String NAME = "json";

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.CLOB};
    }

    @Override
    public Class returnedClass() {
        return Object.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return JsonUtil.object2String(x).equals(JsonUtil.object2String(y));
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return ObjectUtils.nullSafeHashCode(x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        String json = rs.getString(names[0]);
        if (StringUtils.isEmpty(json)){
            return null;
        }
        String columnName = rs.getMetaData().getColumnName(rs.findColumn(names[0]));
        Field field = OrmUtil.getFieldByClassAndColumnName(owner.getClass(), columnName);
        if (field==null) {
            throw new RuntimeException(String.format("实体类[%s]中不存在字段名为[%s]的带@Type字段",owner.getClass().getSimpleName(),columnName));
        }
        return JsonUtil.string2Object(json, field.getGenericType());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null){
            st.setString(index,"");
        } else {
            st.setString(index, JsonUtil.object2String(value));
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }
        return ClonerComponent.getInstance().deepClone(value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
