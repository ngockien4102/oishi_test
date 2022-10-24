package doan.oishii_share_cong_thuc_nau_an.web.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "DishCategory" )
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
//@JsonIdentityInfo(
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "dishCategoryID"
//)
public class DishCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_category_id")
    private Integer dishCategoryID;

    @Column(nullable=true, name = "name", columnDefinition = "nvarchar(max)")
    private String name;

    @Column(name = "dish_category_image", columnDefinition = "nvarchar(max)")
    private String dishCategoryImage;

    @ManyToMany (mappedBy = "idDishCategory",cascade = {CascadeType.MERGE})
//    @JsonBackReference(value = "category-dish")
    private Set<Dish> idDish;
}
