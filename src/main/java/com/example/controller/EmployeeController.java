package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Employee;
import com.example.service.Impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {
    @Autowired
    private EmployeeServiceImpl employeeService;

    @PostMapping("/login")
    private R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //进行MD5加密
        String password = employee.getPassword();
        DigestUtils.md5DigestAsHex(password.getBytes());

        //数据库比对姓名
        String name = employee.getName();
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee one = employeeService.getOne(employeeLambdaQueryWrapper);
        if (one == null) {
            return R.error("用户不存在，登录失败");
        }

        //密码比对
        if (!one.getPassword().equals(password)) {
            R.error("密码错误，登录失败");
        }

        //查看状态
        if (one.getStatus() == 0) {
            R.error("账号禁用，登录失败");
        }

        //登陆成功，保存在session
        request.getSession().setAttribute("employee", one.getId());

        //返回成功
        return R.success(one);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession()
                .removeAttribute("employee");
        return R.success("退出登录！");
    }

    @PostMapping
    public R<String> add( @RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long userId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(userId);
//        employee.setUpdateUser(userId);
        employeeService.save(employee);
        return R.success("新增成功");
    }

    @GetMapping("/page")
    private R<IPage<Employee>> page(int page, int pageSize, String name) {
        //分页构造器
        IPage<Employee> pages = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //开始查询
        employeeService.page(pages, queryWrapper);
        log.info("page:{},pagesize:{}", page, pageSize);

        return R.success(pages);
    }

    @GetMapping("/{id}")
    private R<Employee> UpData(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }

    @PutMapping
    private R<String> StopOrRun( @RequestBody Employee employee) {

//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改成功");
    }
}
