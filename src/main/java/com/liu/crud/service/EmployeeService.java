package com.liu.crud.service;

import com.liu.crud.bean.Employee;
import com.liu.crud.bean.EmployeeExample;
import com.liu.crud.dao.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lms
 * @date 2021-04-20 - 9:51
 */

@Service
public class EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    // 查询所有的员工以及部门信息
    public List<Employee> getAll() {
        return employeeMapper.selectByExampleWithDept(null);
    }

    // 保存员工
    public void saveEmp(Employee employee) {
        // 有选择的进行插入
        employeeMapper.insertSelective(employee);
    }

    /**
     *  // 校验当前用户名是否可用
     * @param empName
     * @return true表示当前用户名可用，false不可用
     */
    public boolean checkUser(String empName) {
        EmployeeExample example = new EmployeeExample();
        EmployeeExample.Criteria criteria = example.createCriteria();
        // 查询数据库中是否存在传入进来的名字
        criteria.andEmpNameEqualTo(empName);
        long count = employeeMapper.countByExample(example);
        return count == 0;
    }


    /**
     * 根据传入的id获取指定的员工信息
     * @param id
     * @return
     */
    public Employee getEemp(Integer id) {
        Employee employee = employeeMapper.selectByPrimaryKey(id);
        return employee;
    }

    public void updateEmp(Employee employee) {
        // 根据提交过来的员工信息，按照主键有选择的进行更新(带有什么内容修改什么内容)
        employeeMapper.updateByPrimaryKeySelective(employee);

        return;
    }

    // 删除指定id的员工信息
    public void deleteEmpById(Integer id) {
        employeeMapper.deleteByPrimaryKey(id);
    }

    // 批量删除指定的员工信息
    public void deleteBatch(List<Integer> ids) {
        EmployeeExample example = new EmployeeExample();
        EmployeeExample.Criteria criteria = example.createCriteria();
        criteria.andEmpIdIn(ids);
        employeeMapper.deleteByExample(example);
    }
}
