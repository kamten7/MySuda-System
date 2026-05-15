package com.suda.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.suda.constant.MessageConstant;
import com.suda.constant.StatusConstant;
import com.suda.dto.DishDTO;
import com.suda.dto.DishPageQueryDTO;
import com.suda.entity.Dish;
import com.suda.entity.DishFlavor;
import com.suda.entity.Setmeal;
import com.suda.exception.DeletionNotAllowedException;
import com.suda.mapper.DishFlavorMapper;
import com.suda.mapper.DishMapper;
import com.suda.mapper.SetmealDishMapper;
import com.suda.mapper.SetmealMapper;
import com.suda.result.PageResult;
import com.suda.service.DishService;
import com.suda.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public DishServiceImpl(DishMapper dishMapper) {
        this.dishMapper = dishMapper;
    }

    /**
     * 新增菜品和口味数据
     * @param dishDTO 菜品数据
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //新增菜品,向菜品表插入1条数据
        Dish dish =new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //获取菜品id
        Long dishId = dish.getId();
        //新增口味，向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //这里遍历 flavors 集合，为每个 DishFlavor 对象设置 dishId 属性，值为新增菜品的 id
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询菜品
     * @param pageQueryDTO 分页查询参数DTO
     * @return 菜品分页数据
     */

    @Override
    public PageResult pageQuery(DishPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids 菜品id列表
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        /*
         * 批量删除菜品
         * 需要进行判断，菜品是否有套餐关联，如果有，不能删除
         * 菜品是否在售，如果售中，不能删除
         */
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds !=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        for(Long id : ids){
            dishMapper.deleteById(id);
        }
        /*
         * 批量删除菜品口味数据
         * 需要删除的菜品id列表， dishFlavorMapper.deleteFlavorBatch(ids);
         */
        for (Long dishId : ids){
            dishFlavorMapper.deleteByDishId(dishId);
        }
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id 菜品id
     * @return 菜品和对应的口味数据VO
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //先查菜品数据
        Dish dish = dishMapper.getById(id);
        //再查菜品口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //组装VO数据并返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品和口味数据
     * @param dishDTO 菜品数据
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //先更新菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        //再更新菜品口味数据
        //先删除旧数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //再插入新数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long dishId = dishDTO.getId();
        //这里遍历 flavors 集合，为每个 DishFlavor 对象设置 dishId 属性，值为新增菜品的 id
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //批量插入
            dishFlavorMapper.insertBatch(flavors);
        }

    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

}
