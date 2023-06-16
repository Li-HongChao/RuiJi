package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.dto.DishDto;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.service.Impl.CategoryServiceImpl;
import com.example.service.Impl.DishFlavorServiceImpl;
import com.example.service.Impl.DishServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishServiceImpl dishService;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private DishFlavorServiceImpl flavorService;

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        //从 菜品表 中查询的分页
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getSort);
        dishService.page(dishPage, queryWrapper);

        Page<DishDto> dtoPage = new Page<>();
        //对象拷贝到返回的数据类中
        BeanUtils.copyProperties(dishPage, dtoPage, "records");
        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream().map((e) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(e, dishDto);
            dishDto.setCategoryName(categoryService.getById(e.getCategoryId()).getName());
            return dishDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("保存成功");
    }

    @GetMapping("/{id}")
    public R<DishDto> getDate(@PathVariable Long id) {
        return R.success(dishService.getByIdWithFlavor(id));
    }

    @PutMapping
    public R<String> upDate(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> delete(String ids) {
        String[] idList = ids.split(",");
        for (String s : idList) {
            dishService.removeById(s);
            flavorService.removeById(s);
        }
        return R.success("删除成功");
    }

    @PostMapping("/status/1")
    public R<String> StopStatus(String ids) {
        update(ids.split(","), 1);
        return R.success("修改成功");
    }

    @PostMapping("/status/0")
    public R<String> StartStatus(String ids) {

        update(ids.split(","), 0);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> selectByCategory(Dish dish) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        wrapper.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishService.list(wrapper);
        List<DishDto> dtos = dishes.stream().map(e -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(e,dishDto);
            Category byId = categoryService.getById(e.getCategoryId());
            if (byId!=null){
                String name = byId.getName();
                dishDto.setCategoryName(name);
            }
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,e.getId());
            dishDto.setFlavors(flavorService.list(queryWrapper));
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dtos);
    }

    public void update(String[] ids, int status) {
        log.info("ids为：{}", Arrays.toString(ids));
        for (String s : ids) {
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            Dish one = dishService.getOne(wrapper.eq(Dish::getId, s));
            one.setStatus(status);
            dishService.updateById(one);
        }
    }
}
