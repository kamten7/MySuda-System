package com.suda.controller.admin;


import com.suda.result.Result;
import com.suda.service.ReportService;
import com.suda.vo.OrderReportVO;
import com.suda.vo.SalesTop10ReportVO;
import com.suda.vo.TurnoverReportVO;
import com.suda.vo.UserReportVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;


/**
 * 报表
 */
@RestController
@Tag(name = "统计报表相关接口")
@Slf4j
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    /**
     * 营业额数据统计
     */
    @GetMapping("/turnoverStatistics")
    @Operation(summary = "营业额数据统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        TurnoverReportVO turnoverReportVO =reportService.getTurnover(begin,end);
        log.info("营业额数据统计{}{}",begin,end);
        return Result.success(turnoverReportVO);
    }
    /**
     * 用户数据统计
     */
    @GetMapping("/userStatistics")
    @Operation(summary = "用户数据统计")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end
    ){

        UserReportVO userReportVO =reportService.getUserStatistics(begin,end);
        return Result.success(userReportVO);
    }


    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @Operation(summary = "用户数据统计")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end){

        return Result.success(reportService.getOrderStatistics(begin,end));
    }


    /**
     * 销量排名统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @Operation(summary = "销量排名统计")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        return Result.success(reportService.getSalesTop10(begin,end));
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @GetMapping("/export")
    @Operation(summary = "导出运营数据报表")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }

}
