package pool

import org.h2.jdbcx.JdbcConnectionPool
import groovy.sql.Sql

class H2Pool {

    static pool = null

    static getSql(context) {
        if (!pool) initPool(context)
        Sql.newInstance(pool)
    }

    static initPool(context) {
        String db = "jdbc:h2:" + context.getRealPath("/WEB-INF/schema")
        pool = JdbcConnectionPool.create(db, "admin", "terces")
        def sql = Sql.newInstance(pool)
        if (!sql.firstRow("select * from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='PUBLIC'")) {
            String script = context.getRealPath("/WEB-INF/schema.sql")
            sql.execute("runscript from '${script}' charset 'UTF-8'")
        }
    }
}
