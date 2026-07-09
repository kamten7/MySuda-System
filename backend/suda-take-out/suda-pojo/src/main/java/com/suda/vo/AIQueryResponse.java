package com.suda.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



//AI响应的参数实体类
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQueryResponse {
    // 用户原始问题
    private String question;
    // AI的回答
    private String answer;
    // 使用的工具/数据源
    private String source;
    // 是否调用了业务工具
    private Boolean toolUsed;
}