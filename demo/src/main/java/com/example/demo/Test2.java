package com.example.demo;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;

/**
 * visitor使用实例
 */
public class Test2 {
    public static void main(String[] args) {
        //sql语句
        String sql = "select * from (select * from student where sage=18 group by sdept) where sex='m' and sname='a' order by sno";
//        String sql = "insert into student(c1, c2, c3, c4) VALUES ('1', '2', '3', '4')";

        //需要初始化一个 Parser，在这里 SQLStatementParser 是一个父类，真正解析 SQL 语句的 Parser 实现是 MySqlStatementParser。
        //Parser 的解析结果是一个 SQLStatement，这是一个内部维护了树状逻辑结构的类。
        // 新建 MySQL Parser
        SQLStatementParser parser = new MySqlStatementParser(sql);

        // 使用Parser解析生成AST，这里SQLStatement就是AST
        SQLStatement statement = parser.parseStatement();

        // 使用visitor来访问AST
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        statement.accept(visitor);

        // 从visitor中拿出你所关注的信息
        System.out.println(visitor.getTables());
        System.out.println(visitor.getColumns());
        System.out.println(visitor.getGroupByColumns());
        System.out.println(visitor.getOrderByColumns());
        System.out.println(visitor.getConditions());
    }
}
