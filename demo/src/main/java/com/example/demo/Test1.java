package com.example.demo;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.List;

/**
 * 打印sql的ast
 */
public class Test1 {
    public static void main(String[] args){
        String sql="select * from (select * from student where sage=18) where sex='m' and sname='a' group by sdept order by sno";
        //String sql="select sno,sname from student where ssex='m' and sno in (select sno from sc where cno in(select cno from course where cname='DB')) and sno not in (select sno from sc where cno in(select cno from course where cname='Oracle'));";

        //String dbType = JdbcConstants.ORACLE;
        String dbType = JdbcConstants.MYSQL;
        //String dbType = "mysql";

        //1
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
        System.out.println(statementList.get(0));
        System.out.println("---------------------------------------------------------------------------");
        //2
        System.out.println(new SQLUtils().toSQLString(statementList.get(0),dbType));
        System.out.println("---------------------------------------------------------------------------");
        //3
        SQLExpr expr = SQLUtils.toSQLExpr(sql, dbType);
        System.out.println(expr);
        System.out.println("---------------------------------------------------------------------------");
        //4
        SQLStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement statement = parser.parseStatement();
        System.out.println(statement);
//        MySqlSchemaStatVisitor visitor=new MySqlSchemaStatVisitor();
//        statement.accept(visitor);
//        System.out.println(visitor.getGroupByColumns());
//        System.out.println(visitor.getOrderByColumns());
    }
}
