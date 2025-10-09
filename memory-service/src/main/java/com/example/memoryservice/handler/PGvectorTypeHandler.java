package com.example.memoryservice.handler;


import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 【健壮版】自定义 MyBatis TypeHandler，用于在 Java 的 PGvector 对象和 PostgreSQL 的 vector 类型之间进行转换。
 * 这个版本能正确处理从JDBC驱动返回的 PGobject 类型，避免 ClassCastException。
 */
public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    /**
     * 在向数据库发送数据时被调用。
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter PGvector 对象
     * @param jdbcType JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        // pgvector-java 库会自动处理 PGvector -> PGobject 的转换，所以这里我们直接 setObject 即可。
        ps.setObject(i, parameter);
    }

    /**
     * 【核心修改】从数据库接收数据时被调用。
     * @param rs ResultSet
     * @param columnName 列名
     * @return PGvector 对象
     * @throws SQLException SQL异常
     */
    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 1. 从结果集中获取对象
        Object obj = rs.getObject(columnName);
        // 2. 调用通用的转换方法
        return convertToObject(obj);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return convertToObject(obj);
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return convertToObject(obj);
    }

    /**
     * 【新增】通用的、健壮的转换逻辑。
     * @param obj 从JDBC驱动获取的原始对象
     * @return 转换后的 PGvector 对象，或 null
     * @throws SQLException 如果转换失败
     */
    private PGvector convertToObject(Object obj) throws SQLException {
        switch (obj) {
            case null -> {
                return null;
            }


            // 理想情况：驱动已经正确地将其识别为 PGvector
            case PGvector pGvector -> {
                return pGvector;
            }


            // 常见情况：驱动将其作为通用的 PGobject 返回
            case PGobject pGobject -> {
                String value = pGobject.getValue();
                if (value != null) {
                    // pgvector-java 库的构造函数可以直接从向量的字符串表示创建对象
                    return new PGvector(value);
                }
            }
            default -> {
            }
        }

        // 兜底情况：如果是一个纯字符串（虽然不常见）
        if (obj instanceof String) {
            return new PGvector((String) obj);
        }

        // 如果是未知的类型，则抛出异常
        throw new SQLException("无法将类型 " + obj.getClass().getName() + " 转换为 com.pgvector.PGvector");
    }
}
