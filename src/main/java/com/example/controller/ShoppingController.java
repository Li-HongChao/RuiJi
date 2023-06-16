package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.ShoppingCart;
import com.example.service.Impl.ShoppingCartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingController {

    //在调用此接口时，有以下几点
    //谁调用的---当前用户id
    //添加、删除的菜品是否存在
    //本次操作是 添加、删除，还是新增，减少
    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }

    @PostMapping("/sub")
    public R<String> delete(@RequestBody ShoppingCart shoppingCart){
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setId(currentId);
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,currentId);
        if (shoppingCart.getDishId()!=null){
            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        if (one.getNumber()>1){
            one.setNumber(one.getNumber()-1);
            shoppingCartService.updateById(one);
        }else {
            shoppingCartService.removeById(one);
        }
        return R.success("删减成功");
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //设置用户id，指定当前是那个用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查看购物车中是否已经存在套餐
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        //需要查询的表为本用户订单表
        wrapper.eq(ShoppingCart::getUserId,userId);
        if (shoppingCart.getDishId()!=null){
            //如果菜品id不为空，则当前数据为菜品，否则为套餐
            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(wrapper);
        //存在-原数值加一，若否，则在购物车中增加
        if (one!=null){
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }
        return R.success(one);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        Long id = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,id);
        shoppingCartService.remove(queryWrapper);
        return R.success("删除成功");
    }
}
