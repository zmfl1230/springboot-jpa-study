package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 한번에 여러개의 주문 아이템을 넣을 수 있으므로, cascade 설정
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_Id", unique = true)
    private Delivery delivery;


    private LocalDateTime orderDateTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;


    public static Order createOrder(Member member, Delivery delivery, OrderItem orderItem1, OrderItem orderItem2) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        order.getOrderItems().add(orderItem1);
        order.getOrderItems().add(orderItem2);
        orderItem1.setOrder(order);
        orderItem2.setOrder(order);
        return order;
    }
}