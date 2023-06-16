package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Setmeal;
import com.example.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}