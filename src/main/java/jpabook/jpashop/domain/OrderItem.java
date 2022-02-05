package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "order_item")
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    /**
     * Order를 필드로 가지고 있다는 의미
     * Order 입자에서는 자신을 가지고 있는 OrderItem이 여러개 있을 가능성이 있다.
     * 즉, 하나의 Order에 여러개의 OrderItem이 연결되는 댜대일 관계가 형성. 고로, OrderItem 입장에선 @ManyToOne 이 맞음
     * 반대로 Order에 입장에서 본인은 하나인데 여러개의 OrderItem이 본인을 가리키는 입장이므로 @OneToMany가 맞음
     * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int orderPrice;

    private int quantity;

    /* 연관관계 편의 메서드 */
    public void updateOrder(Order order) {
        this.order = order;
        order.getOrderItems().add(this);
    }

}