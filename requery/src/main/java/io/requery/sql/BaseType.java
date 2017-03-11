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

package io.requery.sql;

import io.requery.util.Objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base {@link FieldType} implementation, providing basic read/write operations that can be
 * overridden.
 *
 * @param <T> mapped type
 *
 * @author Nikhil Purushe
 */
public abstract class BaseType<T> implements FieldType<T> {

    private final Class<T> type;
    private final int sqlType;

    /**
     * Instantiates a new type instance.
     *
     * @param type    java class type being mapped
     * @param sqlType the {@link java.sql.Types} being mapped
     */
    protected BaseType(Class<T> type, int sqlType) {
        this.type = type;
        this.sqlType = sqlType;
    }

    @Override
    public T read(ResultSet results, int column) throws SQLException {
        T value = type.cast(results.getObject(column));
        if (results.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public void write(PreparedStatement statement, int index, T value) throws SQLException {
        if (value == null) {
            statement.setNull(index, sqlType);
        } else {
            statement.setObject(index, value, sqlType);
        }
    }

    @Override
    public int getSqlType() {
        return sqlType;
    }

    @Override
    public boolean hasLength() {
        return false;
    }

    @Override
    public Integer getDefaultLength() {
        return null;
    }

    public abstract Object getIdentifier();

    @Override
    public String getIdentifierSuffix() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldType) {
            FieldType other = (FieldType) obj;
            return Objects.equals(getIdentifier(), other.getIdentifier()) &&
                getSqlType() == other.getSqlType() &&
                hasLength() == other.hasLength() &&
                Objects.equals(getIdentifierSuffix(), other.getIdentifierSuffix()) &&
                Objects.equals(getDefaultLength(), other.getDefaultLength());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getIdentifier(), getSqlType(), getDefaultLength(), getIdentifierSuffix());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getIdentifier());
        if (hasLength()) {
            sb.append("(");
            sb.append(getDefaultLength());
            sb.append(")");
        }
        if (getIdentifierSuffix() != null) {
            sb.append(" ");
            sb.append(getIdentifierSuffix());
        }
        return sb.toString();
    }
}
