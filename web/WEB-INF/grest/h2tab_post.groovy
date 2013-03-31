import pool.H2Pool

if (!(request.pathvar1 ==~ /\w+/)) {
    response.setStatus(400, "illegal table identifier")
    return
}

def sql = H2Pool.getSql(context)
def tabname = request.pathvar1.toUpperCase()

if (data.ID) {
    sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_UPDATABLE
    sql.eachRow('select * from ' + tabname + ' where ID=' + data.ID.toBigInteger()) { row ->
        data.each { fld, val -> row[fld] = val }
    }
}
else {
    data.ID = sql.firstRow('select ' + tabname + '_SEQ.NEXTVAL as ID from DUAL').ID
    sql.dataSet(tabname).add(data)
}
json(data)
