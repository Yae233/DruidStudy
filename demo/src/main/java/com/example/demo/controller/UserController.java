package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.servers.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 视图逻辑层
 */
@Controller
@RequestMapping("")
public class UserController {
    @Autowired
    private UserService service;


    @RequestMapping("/toadd")
    public String toadd(){
        return "/useradd";
    }

    /**
     * 添加用户
     * @param user
     * @param model
     * @return
     */
    @RequestMapping("/useradd")
    public String useradd(User user, Model model){
        int n=service.insertUser(user);
        if(n>0){
            return "redirect:/user";
        }
        else {
            return "redirect:/toadd";
        }
    }

    /**
     * 用户列表
     * @param username
     * @param model
     * @return
     */
    @RequestMapping("/user")
    public String list(String username,Model model){
        User user=new User();
        user.setUsername(username);
        List<User> list=service.selectUser(user);
        model.addAttribute("list",list);
        return "/user";
    }

    /**
     * 编辑用户
     * @param username
     * @param model
     * @return
     */
    @RequestMapping("/toedit/{username}")
    public String toedit(@PathVariable("username")String username,Model model){
        User user=service.selectUserById(username);
        return "/useredit";
    }

    @RequestMapping("/useredit")
    public String edit(User user,Model model){
        int n=service.updateUser(user);
        if(n>0){
            System.out.println(1);
            return "redirect:/user";
        }
        else {
            System.out.println(0);
            return "redirect:/toedit/"+user.getUsername();
        }
    }

    /**
     * 删除用户
     * @param username
     * @param model
     * @return
     */
    @RequestMapping("/del/{username}")
    public String del(@PathVariable("username") String username,Model model){
        int n=service.deleteUserById(username);
        return "redirect:/user";
    }
}
