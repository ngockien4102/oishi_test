package doan.oishii_share_cong_thuc_nau_an.repositories;

import doan.oishii_share_cong_thuc_nau_an.common.vo.DishCategoryVo;
import doan.oishii_share_cong_thuc_nau_an.web.entities.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface CategoryRepository extends JpaRepository<DishCategory, Integer> {

    @Query("select new doan.oishii_share_cong_thuc_nau_an.common.vo.DishCategoryVo" +
            "(c.dishCategoryID, c.name, c.dishCategoryImage) from DishCategory c")
    List<DishCategoryVo> findAllCategory();
}
