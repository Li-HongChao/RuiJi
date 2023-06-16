package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.entity.User;
import com.example.entity.UserCode;
import com.example.service.Impl.UserServiceImpl;
import com.example.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info("验证码为：{}",code);
            session.setAttribute(phone,code);
        }
        return R.success("登录成功");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody UserCode userCode, HttpSession session){

        String phone = userCode.getPhone();
        String code = userCode.getCode();
        log.info("\n当前验证码已经传输"+code+"\nsession保存验证码为{}",session.getAttribute(phone));

        if (code .equals( session.getAttribute(phone))){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User one = userService.getOne(queryWrapper);
            if(one==null){
                User user= new User();
                user.setPhone(phone);
                userService.save(user);
                one=user;
            }
            session.setAttribute("user",one.getId());
            return R.success(one);
        }
        return R.error("登陆失败");
    }
}
