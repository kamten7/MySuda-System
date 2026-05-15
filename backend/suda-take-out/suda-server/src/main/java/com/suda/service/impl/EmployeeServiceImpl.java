package com.suda.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.suda.constant.MessageConstant;
import com.suda.constant.PasswordConstant;
import com.suda.constant.StatusConstant;
import com.suda.context.BaseContext;
import com.suda.dto.EmployeeDTO;
import com.suda.dto.EmployeeLoginDTO;
import com.suda.dto.EmployeePageQueryDTO;
import com.suda.entity.Dish;
import com.suda.entity.Employee;
import com.suda.exception.AccountLockedException;
import com.suda.exception.AccountNotFoundException;
import com.suda.exception.PasswordErrorException;
import com.suda.mapper.EmployeeMapper;
import com.suda.result.PageResult;
import com.suda.service.EmployeeService;
import com.suda.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的明文密码进行一个md5加密处理
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程的id"+Thread.currentThread().getId());

        Employee employee = new Employee();
        // 对象属性拷贝,将前面的属性拷贝给后面的属性
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置账号的状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码,默认是123456
        employee.setPassword(DigestUtils.md5DigestAsHex((PasswordConstant.DEFAULT_PASSWORD.getBytes())));
        //设置当前记录的创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人的id和修改人的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //将数据插入数据库
        employeeMapper.insert(employee);

    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //pageHelper分页插件,会在底层修改sql语句,增加limit offset,limit
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page =employeeMapper.pageQuery(employeePageQueryDTO);
        //将分页结果转换为PageResult对象
        PageResult pageResult = new PageResult();
        //设置总记录数
        pageResult.setTotal(page.getTotal());
        //设置当前页数据集合
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    /**
     * 启用禁用员工账号
     * @param status 状态，1：启用，0：禁用
     * @param id 员工id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        //将数据封装到Employee对象中
        //然后利用动态sql语句进行更新
        //MyBatis 的工作原理是：它会把传入的 Employee 对象里的非空字段自动拼接到 SQL 语句中。
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id 员工id
     * @return 员工信息
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }


    /**
     * 编辑员工信息
     * @param employeeDTO 编辑员工数据
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //使用对象拷贝，将employeeDTO属性拷贝给employee
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置当前记录的修改时间和修改人的id
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());//拦截器中设置的当前线程的id
        //这里的BaseContext.getCurrentId()是通过底层的ThreadLocal获取的当前线程的id
        // 所以这里可以确保修改人的id是当前线程的id

        employeeMapper.update(employee);
    }


}
