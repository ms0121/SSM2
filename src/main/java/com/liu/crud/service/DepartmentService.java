package com.liu.crud.service;

import com.liu.crud.bean.Department;
import com.liu.crud.dao.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 主要就是用于处理与业务相关的代码信息
 * @author lms
 * @date 2021-04-20 - 21:34
 */

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> getDepts() {
        // 查询所有的部门信息，查询所有的信息，不需要传入任何的限制信息
        List<Department> departments = departmentMapper.selectByExample(null);
        return departments;

    }
}
