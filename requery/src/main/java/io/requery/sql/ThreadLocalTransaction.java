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

import io.requery.EntityCache;
import io.requery.Transaction;
import io.requery.TransactionIsolation;
import io.requery.TransactionListener;
import io.requery.meta.Type;
import io.requery.proxy.EntityProxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Holds transaction state and current connection in {@link ThreadLocal} storage and removes them
 * after a commit or rollback.
 *
 * @author Nikhil Purushe
 */
class ThreadLocalTransaction implements EntityTransaction, ConnectionProvider {

    private final ThreadLocal<EntityTransaction> threadLocal;
    private final RuntimeConfiguration configuration;

    ThreadLocalTransaction(RuntimeConfiguration configuration) {
        this.threadLocal = new ThreadLocal<>();
        this.configuration = configuration;
    }

    @Override
    public Transaction begin() {
        return begin(configuration.getTransactionIsolation());
    }

    @Override
    public Transaction begin(TransactionIsolation isolation) {
        EntityTransaction transaction = threadLocal.get();
        if (transaction == null) {
            EntityCache cache = configuration.getCache();
            TransactionMode mode = configuration.getTransactionMode();
            TransactionListener listener = new CompositeTransactionListener(
                configuration.getTransactionListenerFactories());

            transaction = mode == TransactionMode.MANAGED ?
                new ManagedTransaction(listener, configuration, cache) :
                new ConnectionTransaction(listener, configuration, cache,
                    mode != TransactionMode.NONE);
            threadLocal.set(transaction);
        }
        transaction.begin(isolation);
        return this;
    }

    @Override
    public void commit() {
        Transaction transaction = threadLocal.get();
        if (transaction == null) {
            throw new IllegalStateException();
        }
        transaction.commit();
    }

    @Override
    public void rollback() {
        Transaction transaction = threadLocal.get();
        if (transaction == null) {
            throw new IllegalStateException();
        }
        transaction.rollback();
    }

    @Override
    public boolean active() {
        Transaction transaction = threadLocal.get();
        return transaction != null && transaction.active();
    }

    @Override
    public void addToTransaction(EntityProxy<?> proxy) {
        EntityTransaction transaction = threadLocal.get();
        if (transaction != null) {
            transaction.addToTransaction(proxy);
        }
    }

    @Override
    public void addToTransaction(Collection<Type<?>> types) {
        EntityTransaction transaction = threadLocal.get();
        if (transaction != null) {
            transaction.addToTransaction(types);
        }
    }

    @Override
    public void close() {
        Transaction transaction = threadLocal.get();
        try {
            if (transaction != null) {
                transaction.close();
            }
        } finally {
            threadLocal.remove();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        Transaction transaction = threadLocal.get();
        if (transaction instanceof ConnectionProvider) {
            return ((ConnectionProvider) transaction).getConnection();
        }
        return null;
    }
}
