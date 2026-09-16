/*
 * Decompiled with CFR 0.152.
 */
package com.ultimateduels.database.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DAO<T, ID> {
    public T create(T var1) throws SQLException;

    public Optional<T> findById(ID var1) throws SQLException;

    public List<T> findAll() throws SQLException;

    public T update(T var1) throws SQLException;

    public boolean delete(ID var1) throws SQLException;

    public boolean exists(ID var1) throws SQLException;

    public long count() throws SQLException;

    default public CompletableFuture<T> createAsync(T entity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.create(entity);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default public CompletableFuture<Optional<T>> findByIdAsync(ID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.findById(id);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default public CompletableFuture<T> updateAsync(T entity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.update(entity);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default public CompletableFuture<Boolean> deleteAsync(ID id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.delete(id);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

