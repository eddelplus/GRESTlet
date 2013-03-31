import pool.H2Pool

def tabname = request.pathvar1?.toUpperCase()
if (!tabname) {
    response.setStatus(400, "table not specified")
    return
}

def sql = H2Pool.getSql(context)

if (data.ID) {
    sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_UPDATABLE
    sql.eachRow('select * from ' + tabname + ' where ID=' + data.ID) { row ->
        data.each { fld, val -> row[fld] = val }
    }
    json(data)
}
else {
    data.ID = sql.firstRow('select ' + tabname + '_SEQ.NEXTVAL as ID from DUAL').ID
    sql.dataSet(tabname).add(data)
    json(data)
}
