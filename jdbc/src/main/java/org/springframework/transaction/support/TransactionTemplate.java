package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable runnable) {
        if (TransactionSynchronizationManager.isTransactionEnable()) {
            runnable.run();
            return;
        }
        Connection conn = DataSourceUtils.getConnection(dataSource);
        begin(conn);
        try {
            runnable.run();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            throw e;
        } finally {
            end(conn);
        }
    }

    private void begin(Connection connection) {
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.doBegin();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void end(Connection conn) {
        TransactionSynchronizationManager.doEnd();
        DataSourceUtils.releaseConnection(conn, dataSource);
    }
}
