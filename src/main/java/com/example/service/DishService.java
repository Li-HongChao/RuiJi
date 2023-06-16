package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.DishDto;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;


public interface DishService extends IService<Dish> {
  void saveWithFlavor(DishDto dishDto);

  DishDto getByIdWithFlavor(Long id);
  void updateWithFlavor(DishDto dishDto);
}
