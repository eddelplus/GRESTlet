import org.h2.jdbcx.JdbcConnectionPool
import pool.H2Pool

JdbcConnectionPool pool = H2Pool.pool

json([
    maxConnections:     pool.maxConnections,
    activeConnections:  pool.activeConnections,
    loginTimeout:       pool.loginTimeout
])
