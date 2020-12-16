package com.example.demo.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="users")
public class User {
    @Id
    private String username;
    private String pwd;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
