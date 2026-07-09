package com.suda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @Schema(description = "用户名")
    private String username;

    //ApiModelProperty注解用于给实体属性添加注释，用于在Swagger中显示属性的描述
    @Schema(description = "密码")
    private String password;

}
