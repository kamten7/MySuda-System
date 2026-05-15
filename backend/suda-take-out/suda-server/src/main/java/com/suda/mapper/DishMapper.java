package com.suda.mapper;

import com.github.pagehelper.Page;
import com.suda.annotation.AutoFill;
import com.suda.dto.DishPageQueryDTO;
import com.suda.entity.Dish;
import com.suda.enumeration.OperationType;
import com.suda.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish 菜品数据
     */
    @AutoFill(value = OperationType.INSERT)//这里用AOP自动填充，因为Dish类中已经定义了自动填充的属性，所以这里不需要再定义
    void insert(Dish dish);

    /**
     * 菜品分页查询菜品
     * @param pageQueryDTO 分页查询参数DTO
     * @return 菜品分页数据
     */
    Page<DishVO> pageQuery(DishPageQueryDTO pageQueryDTO);

    /**
     * 批量删除菜品
     * 需要进行判断，菜品是否有套餐关联，如果有，不能删除
     *
     * @param id 菜品id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据id查询菜品信息
     * @param id 菜品id
     * @return 菜品信息
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 修改菜品
     * @param dish 菜品数据
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);


    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
