package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.mapper.OrderDetailMapper;
import com.example.mapper.OrdersMapper;
import com.example.service.OrderDetailService;
import com.example.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
}
