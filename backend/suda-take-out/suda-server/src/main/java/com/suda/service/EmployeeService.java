package com.suda.service;

import com.suda.dto.EmployeeDTO;
import com.suda.dto.EmployeeLoginDTO;
import com.suda.dto.EmployeePageQueryDTO;
import com.suda.entity.Employee;
import com.suda.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO 登录参数
     * @return 登录成功返回登录员工信息
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO 新增员工参数
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO 分页查询参数
     * @return 分页查询结果
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号
     * @param status 状态，1：启用，0：禁用
     * @param id 员工id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工信息
     * @param id 员工id
     * @return 员工信息
     */
    Employee getById(Long id);

    /**
     * 编辑员工信息
     * @param employeeDTO 编辑员工数据
     */
    void update(EmployeeDTO employeeDTO);
}
