package com.suda.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    //ApiModelProperty注解用于给实体属性添加注释，用于在Swagger中显示属性的描述
    @ApiModelProperty("密码")
    private String password;

}
