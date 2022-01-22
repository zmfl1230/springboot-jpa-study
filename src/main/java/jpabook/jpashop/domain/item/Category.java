package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id")
    private Long id;


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    /**
     * @JoinColumn(name= "") 의 name으로 컬럼명 생성
     * 한 entity에 해당 네이밍이 동일한 값이 있다면, 테이블 생성 실패
     * 고로 아래값 category_id -> parent_id 로 수정
     */
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent") // db에 생성 안됨
    private List<Category> children = new ArrayList<>();


}