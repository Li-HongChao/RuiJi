package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.*;
import com.example.service.Impl.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersServiceImpl ordersService;

    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private OrderDetailServiceImpl detailService;
    @Autowired
    private AddressBookServiceImpl addressBookService;

    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String id) {
        Page<Orders> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(id), Orders::getId, id);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(dishPage, queryWrapper);
        return R.success(dishPage);
    }

    @PostMapping("/submit")
    public R<String> save(@RequestBody Orders orders) throws Exception {
        Long currentId = BaseContext.getCurrentId();

        //获取购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        if (list == null || list.size() == 0) {
            throw new Exception("购物车为空");
        }

        AtomicInteger atomicInteger = new AtomicInteger(0);
        //获取金额
        List<OrderDetail> orderDetails = list.stream().map(e -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNumber(e.getNumber());
            orderDetail.setDishFlavor(e.getDishFlavor());
            orderDetail.setDishId(e.getDishId());
            orderDetail.setSetmealId(e.getSetmealId());
            orderDetail.setImage(e.getImage());
            orderDetail.setAmount(e.getAmount());
            orderDetail.setOrderId(e.getId());
            //addAndGet累加  multiply相乘
            atomicInteger.addAndGet(e.getAmount().multiply(new BigDecimal(e.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //查询用户数据
        User user = userService.getById(currentId);

        //查询地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook byId = addressBookService.getById(addressBookId);
        if (byId == null) {
            throw new Exception("地址信息不存在");
        }

        //向订单表添加数据
        orders.setUserId(currentId);
        orders.setPhone(user.getPhone());
        //Idwork是spring提供的id类，setNumber是订单号
        orders.setNumber(String.valueOf(IdWorker.getId()));
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setUserName(user.getName());
        orders.setAmount(new BigDecimal(atomicInteger.get()));
        orders.setConsignee(byId.getConsignee());
        orders.setPhone(byId.getPhone());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAddress(
                (byId.getProvinceName() == null ? "" : byId.getProvinceName())
                        + (byId.getCityName() == null ? "" : byId.getCityName())
                        + (byId.getDistrictName() == null ? "" : byId.getDistrictName())
                        + (byId.getDetail() == null ? "" : byId.getDetail())
        );
        ordersService.save(orders);

        //保存明细表
        detailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
        return R.success("保存成功");
    }
}
