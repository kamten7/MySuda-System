package com.suda.service;

import com.suda.dto.DishDTO;
import com.suda.dto.DishPageQueryDTO;
import com.suda.entity.Dish;
import com.suda.result.PageResult;
import com.suda.vo.DishVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService {
    /**
     * 新增菜品和口味数据
     * @param dishDTO 菜品数据
     *
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param pageQueryDTO 分页查询参数DTO
     * @return 菜品分页数据
     */
    PageResult pageQuery(DishPageQueryDTO pageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids 菜品id列表
     *
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id 菜品id
     * @return 菜品和对应的口味数据VO
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品和口味数据
     * @param dishDTO 菜品数据
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);
}
