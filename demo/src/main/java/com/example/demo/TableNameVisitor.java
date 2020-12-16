package com.example.demo;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;


/**
 * 数据库表名访问者
 */
public class TableNameVisitor extends MySqlOutputVisitor {

    public TableNameVisitor(Appendable appender) {
        super(appender);
    }

    @Override
    public boolean visit(SQLExprTableSource x) {

        // 改写tableName
        print0("new_" + x.getExpr().toString().toUpperCase());

        return true;
    }
}