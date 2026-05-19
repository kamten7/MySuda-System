package com.suda.controller.admin;

import com.suda.constant.JwtClaimsConstant;
import com.suda.dto.EmployeeDTO;
import com.suda.dto.EmployeeLoginDTO;
import com.suda.dto.EmployeePageQueryDTO;
import com.suda.entity.Employee;
import com.suda.properties.JwtProperties;
import com.suda.result.PageResult;
import com.suda.result.Result;
import com.suda.service.EmployeeService;
import com.suda.utils.JwtUtil;
import com.suda.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags="员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value="员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        // 2. 创建JWT载荷（Claims）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        //3. 创建JWT令牌
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 4. 返回包含token的响应
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value="员工退出登录")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value="新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工，员工数据：{}", employeeDTO);
        System.out.println("当前线程的id"+Thread.currentThread().getId());
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO 分页查询参数
     * @return 分页查询结果
     */
    @ApiOperation(value="员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，参数：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        log.info("员工分页查询，结果：{}", pageResult);

        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * @param status 状态，1：启用，0：禁用
     * @param id 员工id
     * @return 禁用成功
     */
    @ApiOperation(value="启用禁用员工账号")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id){
        //这里的前端页面
        //会根据后端数据库，如果该员工的status==1就显示禁用按钮，如果该员工的status==0就显示启用按钮
        //启用和禁用都会传入status和id
        //传入的status都是1或者0，是前端组件中写固定的

        log.info("启用禁用员工账号：{} {}", status, id);
        employeeService.startOrStop(status, id);

        return Result.success();
    }

    /**
     * 根据Id查询员工消息
     * @param id 员工id
     * @return 员工信息
     */
    @ApiOperation(value="根据Id查询员工消息")
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据Id查询员工消息：{}", id);
        //查询回显，方便修改员工数据
        Employee employee=employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO 编辑员工数据
     * @return 编辑成功结果
     *
     */
    //复用增加员工的DTO
    @PutMapping
    @ApiOperation(value="编辑员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        //因为前端传过来的是json数据，所以这里需要将json数据转为EmployeeDTO对象
        log.info("编辑员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);

        return Result.success();
    }
}
