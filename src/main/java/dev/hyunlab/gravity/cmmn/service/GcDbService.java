package dev.hyunlab.gravity.cmmn.service;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.hyunlab.gravity.cmmn.domain.GcColumnMetaDto;
import dev.hyunlab.gravity.cmmn.domain.GcDatabaseProductNameEnum;

public interface GcDbService {
        String createDatabaseUrl(GcDatabaseProductNameEnum dbProductName, String ip, String port, String dbName);

        Connection createConnection(String url, String username, String plainPassword) throws SQLException;

        boolean canConnection(String url, String username, String plainPassword) throws SQLException;

        void executeUpdate(Connection conn, String sql) throws SQLException;

        void executeUpdate(Statement stmt, String sql) throws SQLException;

        int[] executeBatch(Statement stmt, List<String> sqls) throws SQLException;

        /**
         * ! 주의. 현재 mysql,mariadb만 지원
         * 
         * @param stmt
         * @param tableName
         * @param columnMetaDtos dataType에 크기까지 존재해야 함(임시). 예) varchar(100)
         * @return
         * @throws SQLException
         */
        boolean createTable(Statement stmt, String tableName, List<GcColumnMetaDto> columnMetaDtos) throws SQLException;

        boolean existsTable(Statement stmt, String tableName) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @return true: 성공, false: tableName 미 존재시
         * @throws SQLException
         */
        boolean dropTable(Statement stmt, String tableName) throws SQLException;

        /**
         * batch 처리
         * 
         * @param stmt
         * @param tableNames
         * @return
         * @throws SQLException
         */
        boolean dropTables(Statement stmt, List<String> tableNames) throws SQLException;

        /**
         * srcTableName을 destTableName으로 변경한다.
         * 
         * @param stmt
         * @param oldTableName
         * @param newTableName
         * @return true: 성공, false: srcTableName 미 존재시 or destTableName 존재시
         * @throws SQLException
         */
        boolean changeTableName(Statement stmt, String oldTableName, String newTableName) throws SQLException;

        /**
         * batch 처리
         * 
         * @param stmt
         * @param oldTableNames
         * @param newTableNames
         * @return
         * @throws SQLException
         */
        boolean changeTablesNames(Statement stmt, List<String> oldTableNames, List<String> newTableNames)
                        throws SQLException;

        /**
         * srcTableName을 destTableName으로 변경하고 데이터를 복사한다.
         * 
         * @param stmt
         * @param srcTableName
         * @param destTableName
         * @return true: 성공, false: srcTableName 미 존재시 or destTableName 존재시
         * @throws SQLException
         */
        boolean copyTableWithDatas(Statement stmt, String srcTableName, String destTableName) throws SQLException;

        boolean existsColumn(Statement stmt, String tableName, String columnName) throws SQLException;

        Set<String> getColumnNames(Statement stmt, String tableName) throws SQLException;

        Set<String> getColumnNames(ResultSetMetaData rsmd) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @param columnName
         * @param comment
         * @return true: 성공, false: columnName 존재시
         * @throws SQLException
         */
        boolean addColumn(Statement stmt, String tableName, String columnName, String comment) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @param columnName
         * @param dataType
         * @param comment
         * @return true: 성공, false: columnName 존재시
         * @throws SQLException
         */
        boolean addColumn(Statement stmt, String tableName, String columnName, String dataType, String comment)
                        throws SQLException;

        /**
         * batch 처리
         * 
         * @param stmt
         * @param tableName
         * @param columnMetaDtos
         * @return
         * @throws SQLException
         */
        boolean addColumns(Statement stmt, String tableName, List<GcColumnMetaDto> columnMetaDtos) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @param columnName
         * @return true: 성공, false: columnName 미 존재시
         * @throws SQLException
         */
        boolean dropColumn(Statement stmt, String tableName, String columnName) throws SQLException;

        /**
         * batch 처리
         * 
         * @param stmt
         * @param tableNames
         * @param columnNames
         * @return
         * @throws SQLException
         */
        boolean dropColumns(Statement stmt, String tableName, List<String> columnNames) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @param oldColumnName
         * @param newColumnName
         * @param newDataType
         * @param newComment
         * @return true: 성공, false: oldColumnName 미 존재시 or newColumnName 존재시
         * @throws SQLException
         */
        boolean changeColumn(Statement stmt, String tableName, String oldColumnName, String newColumnName,
                        String newDataType,
                        String newComment) throws SQLException;

        /**
         * batch 처리
         * 
         * @param stmt
         * @param tableName
         * @param oldColumnNames
         * @param columnMetaDtos
         * @return
         * @throws SQLException
         */
        boolean changeColumns(Statement stmt, String tableName, List<String> oldColumnNames,
                        List<GcColumnMetaDto> columnMetaDtos) throws SQLException;

        /**
         * 
         * @param stmt
         * @param tableName
         * @return true: 성공, false: tableName 미존재 or 데이터 미존재
         * @throws SQLException
         */
        boolean existsData(Statement stmt, String tableName) throws SQLException;

        /**
         * @see getDatas(Statement,String)
         * @param conn
         * @param sql
         * @return
         * @throws SQLException
         */
        List<Map<String, Object>> getDatas(Connection conn, String sql) throws SQLException;

        /**
         * 
         * @param stmt
         * @param sql
         * @return List<Map<String, Object>> key: columnName, value: data
         * @throws SQLException
         */
        List<Map<String, Object>> getDatas(Statement stmt, String sql) throws SQLException;

        /**
         * @param listOfMap List<Map<columnName, value>>
         */
        int[] insertDatas(Statement stmt, String tableName, List<Map<String, Object>> listOfMap) throws SQLException;

}
