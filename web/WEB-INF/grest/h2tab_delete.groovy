import pool.H2Pool

if (!(request.pathvar1 ==~ /\w+/)) {
    response.setStatus(400, "illegal table identifier")
    return
}

def sql = H2Pool.getSql(context)

if (request.pathvar2) {
    def stmt = 'delete from ' + request.pathvar1.toUpperCase() + ' where ID=?'
    if (sql.executeUpdate(stmt, [request.pathvar2]) > 0) {
        response.setStatus(204, "successfully deleted")
        return
    }
}
response.setStatus(404, "nothing deleted")
