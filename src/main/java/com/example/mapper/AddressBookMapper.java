package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.AddressBook;
import com.example.entity.Employee;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
