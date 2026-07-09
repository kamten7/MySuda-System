package com.suda.service;

import com.suda.vo.OrderReportVO;
import com.suda.vo.SalesTop10ReportVO;
import com.suda.vo.TurnoverReportVO;
import com.suda.vo.UserReportVO;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 统计数据
 * 报表服务接口
 * 主要是用于统计运营数据报表，如营业额、用户数量、订单数量、销量排名top10等报表
 */
@Service
public interface ReportService {

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnover(LocalDate begin,LocalDate end);

    /**
     * 根据时间区间统计用户数量
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);


    /**
     * 根据时间区间统计订单数量
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);


    /**
     * 查询指定时间区间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出近30天的运营数据报表
     * @param response
     **/
    void exportBusinessData(HttpServletResponse response);
}
