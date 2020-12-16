package com.example.demo;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import java.io.StringWriter;

/**
 * 自定义visitor
 */
public class Test3 {

    public static void main(String[] args) {
        //String sql = "update t set a=1 where id=1";
        String sql = "select * from t where id=1 and name='ming' group by uid limit 1,200 order by ctime";

        // 新建 MySQL Parser
        SQLStatementParser parser = new MySqlStatementParser(sql);

        // 使用Parser解析生成AST，这里SQLStatement就是AST
        SQLStatement sqlStatement = parser.parseStatement();

        // 改写表名t为new_T
        // 最终sql输出
        StringWriter out = new StringWriter();
        TableNameVisitor outputVisitor = new TableNameVisitor(out);
        sqlStatement.accept(outputVisitor);
        System.out.println(out.toString());
    }

}