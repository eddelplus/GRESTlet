import pool.H2Pool

if (!(request.pathvar1 ==~ /\w+/)) {
    response.setStatus(400, "illegal table identifier")
    return
}

def sql = H2Pool.getSql(context)

def stmt = 'select * from ' + request.pathvar1.toUpperCase()

if (request.pathvar2) {
    // Get specific row by id
    def row = sql.firstRow(stmt + ' where ID=?', [request.pathvar2])
    if (row)
        json(row)
    else
        response.sendError(404, "Resource not found")
}
else {
    if (params.order) stmt = stmt + ' order by ' + params.order
    def start = params.start?.toInteger() ?:  0
    def limit = params.limit?.toInteger() ?: 51
    json(sql.rows(stmt, start + 1, limit))
}
