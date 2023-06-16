package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.dto.DishDto;
import com.example.dto.SetmealDto;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.service.CategoryService;
import com.example.service.Impl.CategoryServiceImpl;
import com.example.service.Impl.DishServiceImpl;
import com.example.service.Impl.SetmealDishServiceImpl;
import com.example.service.Impl.SetmealServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealServiceImpl setmealService;
    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private DishServiceImpl dishService;
    @Autowired
    private SetmealDishServiceImpl setmealDishService;

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);

        Page<SetmealDto> dtoPage = new Page<>();
        //对象拷贝到返回的数据类中
        log.info("records的值为:{}", setmealPage.getRecords().toArray());
        BeanUtils.copyProperties(setmealPage, dtoPage, "records");
        List<Setmeal> records = setmealPage.getRecords();
        List<SetmealDto> list = records.stream().map((e) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(e, setmealDto);
            setmealDto.setCategoryName(categoryService.getById(e.getCategoryId()).getName());
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        //套餐表保存
        setmealService.save(setmealDto);
        //保存菜品和套餐的关系表
        List<SetmealDish> setmealDishStream = setmealDto.getSetmealDishes().stream().map((e) -> {
            e.setSetmealId(setmealDto.getId());
            return e;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishStream);

        return R.success("保存成功");
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateById(setmealDto);

        //删除之前的关系
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(wrapper);

        //从新保存关系
        List<SetmealDish> setmealDishStream = setmealDto.getSetmealDishes().stream().map((e) -> {
            e.setSetmealId(setmealDto.getId());
            return e;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishStream);

        return R.success("更改成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getDate(@PathVariable Long id) {
        Setmeal byId = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(byId, setmealDto);
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDto.setSetmealDishes(setmealDishService.list(wrapper));
        return R.success(setmealDto);
    }


    @DeleteMapping
    public R<String> delete(String ids) {
        String[] idList = ids.split(",");
        for (String s : idList) {
            setmealService.removeById(s);
            LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SetmealDish::getSetmealId, s);
            setmealDishService.removeById(s);
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

    public void update(String[] ids, int status) {
        log.info("ids为：{}", Arrays.toString(ids));
        for (String s : ids) {
            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
            Setmeal one = setmealService.getOne(wrapper.eq(Setmeal::getId, s));
            one.setStatus(status);
            setmealService.updateById(one);
        }
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(setmeal.getName()), Setmeal::getName, setmeal.getName());
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return R.success(setmealService.list(queryWrapper));
    }

    @GetMapping("/dish/{id}")
    public R<Dish> getDish(@PathVariable Long id) {
        return R.success(dishService.getById(id));
    }
}
