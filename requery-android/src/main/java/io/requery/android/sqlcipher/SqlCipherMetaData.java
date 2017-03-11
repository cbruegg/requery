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

package io.requery.android.sqlcipher;

import android.database.Cursor;
import io.requery.android.sqlite.BaseConnection;
import io.requery.android.sqlite.SqliteMetaData;
import io.requery.util.function.Function;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

class SqlCipherMetaData extends SqliteMetaData {

    SqlCipherMetaData(BaseConnection connection) {
        super(connection);
    }

    @Override
    protected <R> R queryMemory(Function<Cursor, R> function, String query) throws SQLException {
        try {
            final SQLiteDatabase database =
                SQLiteDatabase.openOrCreateDatabase(":memory:", "", null);
            Cursor cursor = database.rawQuery(query, null);
            return function.apply(closeWithCursor(new Closeable() {
                @Override
                public void close() throws IOException {
                    database.close();
                }
            }, cursor));
        } catch (SQLiteException e) {
            throw new SQLException(e);
        }
    }
}
