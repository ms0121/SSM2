package com.liu.crud.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.liu.crud.bean.Employee;
import com.liu.crud.bean.Msg;
import com.liu.crud.service.EmployeeService;
import jakarta.validation.Valid;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工的crud请求
 * @author lms
 * @date 2021-04-20 - 9:45
 */

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // 删除指定的员工信息
    // 单个删除批量删除实现二合一的操作
    // 单个删除 ：1
    // 批量删除 ：1-2-3
    // 指定参数中的id来源于路径中传递过来的
    @ResponseBody
    @RequestMapping(value = "/emp/{ids}", method = RequestMethod.DELETE)
    public Msg deleteEmpById(@PathVariable("ids") String ids) {
        // 批量删除
        if (ids.contains("-")){
            ArrayList<Integer> del_ids = new ArrayList<>();
            String[] split = ids.split("-");
            for (String s : split) {
                int i = Integer.parseInt(s);
                del_ids.add(i);
            }
            employeeService.deleteBatch(del_ids);
        } else {
            int id = Integer.parseInt(ids);
            employeeService.deleteEmpById(id);
        }
        return Msg.success();
    }


    // 保存更新后的员工信息
    // 我们要能支持直接发送PUT之类的请求还要还要封装请求体的数据
    // 1. 配置HTTPPutFormContentFilter，它的作用就是能够将请求中的数据解析包装成为map
//    request被重新包装，request.getParameter()被重写，就会从自己封装的map中取出数据信息
    @RequestMapping(value = "/emp/{empId}", method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee) {
        employeeService.updateEmp(employee);
        return Msg.success();
    }



    // 查询员工信息
    // id 来源于请求地址的id变量值,@PathVariable("id")表示id是从路径中取出的
    // 规定:带有id请求的，以及是get请求方式都是查询员工的方式
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Msg name(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEemp(id);
        return Msg.success().add("emp",employee);
    }


    // 检验当前用户是否可用
    // 指定取出 empName的值作为参数
    @RequestMapping("/checkuser")
    @ResponseBody
    public Msg checkuser(@RequestParam("empName")String empName) {
        // 1.先判断用户名是否是合法的表达式
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if (!empName.matches(regx)){
            return Msg.fail().add("va_msg", "用户名可以是2-5位的中文或者6-16位的数字字符");
        }

        // 2. 判断用户名是否可用
        // 通过Msg里面的状态码来识别用户名是否可用
        boolean flag = employeeService.checkUser(empName);
        if (flag){
            return Msg.success();
        } else {
            return Msg.fail().add("va_msg", "用户名不可用");
        }
    }


    /**
     // 保存员工信息
     * 需要添加前端和后端的校验，防止用户越过请求
     *  1. 支持JSR303校验
     *  2. 导入依赖包 Hibernate-Validator
     * @Valid 代表封装的数据要进行Employee对象设定的校验规则，并设置相应的提示信息
     *  BindingResult 用于封装校验的结果
     */
    @RequestMapping(value = "/emp", method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result) {
        if (result.hasErrors()){
            // 校验失败，应该反悔失败，在模态框中显示检验失败的错误信息
            // 获取所有校验失败的错误信息
            Map<String, Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors) {
                System.out.println("错误的字段名: " + error.getField());
                System.out.println("错误的信息: " + error.getDefaultMessage());
                map.put(error.getField(), error.getDefaultMessage());
            }
            // 将错误信息进行封装保存
            return Msg.fail().add("errorField", map);
        } else {
            // 如果校验无误在保存数据信息
            employeeService.saveEmp(employee);
            return Msg.success();
        }
    }


    // 使用ajax的方式
    @RequestMapping("/emps")
    @ResponseBody  // 将返回的对象自动转为json字符串的形式
    public Msg getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn) {
        // 引入一个分页插件PageHelper
        // 在查询员工数据之前需要提前进行调用分页插件
        // 起始页，每页显示几条数据
        PageHelper.startPage(pn, 5);
        // startPage后面紧跟着的这个查询所有员工数据的方法就是一个分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面(使用model或者map封装信息进行传输)
        // 就可以,封装了详细的分页信息（目的更好的处理分页栏），包括有我们查询出来的数据
        // 第二个参数表示连续显示的页数
        PageInfo pageInfo = new PageInfo(emps, 5);
        // 执行Msg.success()得到msg对象，然后设置msg对象里面的map集合的值
        return Msg.success().add("pageInfo", pageInfo);
    }

    /**
     * 在service中查询员工数据
     * 参数：首页过来的时候默认第一页
     * @return
     */
//    @RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                          Model model) {
        // 引入一个分页插件PageHelper
        // 在查询员工数据之前需要提前进行调用分页插件
        // 起始页，每页显示几条数据
        PageHelper.startPage(pn, 5);
        // startPage后面紧跟着的这个查询所有员工数据的方法就是一个分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面(使用model或者map封装信息进行传输)
        // 就可以,封装了详细的分页信息（目的更好的处理分页栏），包括有我们查询出来的数据
        // 第二个参数表示连续显示的页数
        PageInfo pageInfo = new PageInfo(emps, 5);
        model.addAttribute("pageInfo", pageInfo);
        // 跳到list显示页面
        return "list";
    }
}
