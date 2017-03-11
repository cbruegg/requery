/*
 * Copyright 2016 requery.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.requery.android.sqlite;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import io.requery.sql.Keyword;
import io.requery.sql.QueryBuilder;
import io.requery.util.function.Function;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqliteMetaData implements DatabaseMetaData {

    private final BaseConnection connection;

    protected SqliteMetaData(BaseConnection connection) {
        this.connection = connection;
    }

    protected <R> R queryMemory(Function<Cursor, R> function, String query) throws SQLException {
        try {
            final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(":memory:", null);
            Cursor cursor = database.rawQuery(query, null);
            return function.apply(closeWithCursor(database, cursor));
        } catch (android.database.SQLException e) {
            throw new SQLException(e);
        }
    }

    protected CursorWrapper closeWithCursor(final Closeable closeable, Cursor cursor) {
        return new CursorWrapper(cursor) {
            @Override
            public void close() {
                super.close();
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        };
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
                                   String attributeNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope,
                                          boolean nullable) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        return null;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return null;
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table,
                                         String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
                                String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public ResultSet getCrossReference(String primaryCatalog, String primarySchema,
                                       String primaryTable, String foreignCatalog,
                                       String foreignSchema, String foreignTable)
            throws SQLException {
        return null;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return Integer.parseInt(getDatabaseProductVersion().split(".")[0]);
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return Integer.parseInt(getDatabaseProductVersion().split(".")[1]);
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return "SQLite";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return queryMemory(new Function<Cursor, String>() {
            @Override
            public String apply(Cursor cursor) {
                String version = "";
                if (cursor.moveToNext()) {
                    version = cursor.getString(0);
                }
                return version;
            }
        }, "select sqlite_version() AS sqlite_version");
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public int getDriverMajorVersion() {
        return 1;
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;
    }

    @Override
    public String getDriverName() throws SQLException {
        return "SQLite Android";
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return "1.0";
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException {
        return null;
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "";
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return "\"";
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique,
                                  boolean approximate) throws SQLException {
        return null;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 1;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 1000000000;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 500;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 500;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 500;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 500;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 2000;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 1;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 1000000;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 1000000;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 1000000;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 500;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return "abs,hex,max,min,random";
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
                                         String procedureNamePattern, String columnNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return null;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return null;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return null;
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return "";
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return sqlStateSQL99;
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return "";
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return "";
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
                                        String tableNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern,
                               String[] types) throws SQLException {
        if (types == null) {
            types = new String[] {"TABLE", "VIEW"};
        }
        if (tableNamePattern == null) {
            tableNamePattern = "%";
        }
        Map<String, String> select = new LinkedHashMap<>();
        select.put("TABLE_CAT", null);
        select.put("TABLE_SCHEM", null);
        select.put("TABLE_NAME", "name");
        select.put("TABLE_TYPE", "type");
        select.put("REMARKS", null);
        select.put("TYPE_CAT", null);
        select.put("TYPE_SCHEM", null);
        select.put("TYPE_NAME", null);
        select.put("SELF_REFERENCING_COL_NAME", null);
        select.put("REF_GENERATION", null);
        QueryBuilder qb = new QueryBuilder(
            new QueryBuilder.Options(getIdentifierQuoteString(), true, null, null, false, false))
            .keyword(Keyword.SELECT)
            .commaSeparated(select.entrySet(),
                new QueryBuilder.Appender<Map.Entry<String, String>>() {
                @Override
                public void append(QueryBuilder qb, Map.Entry<String, String> entry) {
                    String value = entry.getValue() == null ? "null" : entry.getValue();
                    qb.append(value).append(" as ").append(entry.getKey());
                }
            })
            .keyword(Keyword.FROM)
            .openParenthesis().append("select name, type from sqlite_master").closeParenthesis()
            .keyword(Keyword.WHERE)
            .append(" TABLE_NAME like ").append(tableNamePattern).append(" && TABLE_TYPE in ")
            .openParenthesis()
            .commaSeparated(Arrays.asList(types))
            .closeParenthesis()
            .keyword(Keyword.ORDER, Keyword.BY)
            .append(" TABLE_TYPE, TABLE_NAME");
        return queryMemory(new Function<Cursor, ResultSet>() {
            @Override
            public ResultSet apply(Cursor cursor) {
                return new CursorResultSet(null, cursor, true);
            }
        }, qb.toString());
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return queryMemory(new Function<Cursor, ResultSet>() {
            @Override
            public ResultSet apply(Cursor cursor) {
                return new CursorResultSet(null, cursor, true);
            }
        }, "select 'TABLE' as TABLE_TYPE, 'VIEW' as TABLE_TYPE");
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern,
                             int[] types) throws SQLException {
        return null;
    }

    @Override
    public String getURL() throws SQLException {
        return null;
    }

    @Override
    public String getUserName() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table)
            throws SQLException {
        return null;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        switch (holdability) {
            case ResultSet.HOLD_CURSORS_OVER_COMMIT:
                return true;
        }
        return false;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        switch (type) {
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
            case ResultSet.TYPE_FORWARD_ONLY:
                return true;
        }
        return false;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        switch (level) {
            case Connection.TRANSACTION_SERIALIZABLE:
            case Connection.TRANSACTION_READ_COMMITTED:
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return true;
        }
        return false;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return true;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return true;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return true;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern,
                                        String functionNamePattern, String columnNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
            throws SQLException {
        return null;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
