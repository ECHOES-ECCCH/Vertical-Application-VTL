package pl.psnc.vtl.portal.conf.fts;

import org.hibernate.dialect.PostgreSQL95Dialect;

public class CustomPostgreSQLDialect extends PostgreSQL95Dialect {

    public CustomPostgreSQLDialect() {
        registerFunction("fts", new PostgreSQLFTSFunction());
    }
}