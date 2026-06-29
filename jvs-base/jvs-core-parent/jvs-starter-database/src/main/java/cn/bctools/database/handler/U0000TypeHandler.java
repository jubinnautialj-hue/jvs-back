package cn.bctools.database.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhuxiaokang
 */
public class U0000TypeHandler extends BaseTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        String p = parameter.replaceAll("\u0000", "\\\\u0000");
        ps.setString(i, p);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        if (columnValue != null && !columnValue.isEmpty()) {
            return columnValue.replaceAll("\\\\u0000", "\u0000");
        } else {
            return null;
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        if (columnValue != null && !columnValue.isEmpty()) {
            return columnValue.replaceAll("\\\\u0000", "\u0000");
        } else {
            return null;
        }
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValue = cs.getString(columnIndex);
        if (columnValue != null && !columnValue.isEmpty()) {
            return columnValue.replaceAll("\\\\u0000", "\u0000");
        } else {
            return null;
        }
    }
}
