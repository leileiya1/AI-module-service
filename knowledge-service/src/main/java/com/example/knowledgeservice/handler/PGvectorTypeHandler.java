package com.example.knowledgeservice.handler;


import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 自定义 MyBatis TypeHandler，用于在 Java 的 PGvector 对象和 PostgreSQL 的 vector 类型之间进行转换。
 */
public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return (PGvector) rs.getObject(columnName);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return (PGvector) rs.getObject(columnIndex);
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return (PGvector) cs.getObject(columnIndex);
    }
}
