package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Category;
import com.example.service.Impl.CategoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    CategoryServiceImpl categoryServiceImpl;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryServiceImpl.save(category);
        return R.success("新增成功");
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        categoryServiceImpl.removeById(ids);
        return R.success("删除成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryServiceImpl.updateById(category);
        return R.success("修改成功");
    }

    @GetMapping("/page")
    public R<Page<Category>> page(int page,int pageSize){
        Page<Category> categoryPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.orderByDesc(Category::getSort);
        categoryServiceImpl.page(categoryPage,categoryLambdaQueryWrapper);
        return R.success(categoryPage);
    }


    @GetMapping("/list")
     public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getType()!=null ,Category::getType,category.getType());
        wrapper.orderByAsc(Category::getType).orderByAsc(Category::getSort);
        List<Category> categories = categoryServiceImpl.list(wrapper);
        log.info(R.success(categories).toString());
        return R.success(categories);
    }
}
