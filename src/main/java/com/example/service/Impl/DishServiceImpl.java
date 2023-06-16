package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.DishDto;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.mapper.DishFlavorMapper;
import com.example.mapper.DishMapper;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService flavorService;

    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();//菜品id

        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null) {
            flavors.stream().map((e)->{
                e.setDishId(dishId);
                return e;
            }).collect(Collectors.toList());

            flavorService.saveBatch(flavors);
        }
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish byId = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(byId, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishDto.setFlavors(flavorService.list(queryWrapper));

        return dishDto;
    }

    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据
        log.info("当前id为：{}",dishDto.getId());
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,dishDto.getId());
        flavorService.remove(wrapper);

        //添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((e)->{
            e.setDishId(dishDto.getId());
            return e;
        }).collect(Collectors.toList());
        flavorService.saveBatch(flavors);
    }


}
