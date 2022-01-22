package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    /**
     * TODO: 잘못된 설정인 이유
     *  @JoinTable(name = "category_item",
     *     joinColumns = @JoinColumn(name = "item_id",
     *     referencedColumnName = "categories_id"))
      */
    private List<Category> categories = new ArrayList<>();

    /* 비즈니스 로직 */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int retStock = this.stockQuantity - quantity;
        if (retStock < 0) throw new IllegalStateException("재고가 부족합니다.");
        this.stockQuantity = retStock;
    }
}