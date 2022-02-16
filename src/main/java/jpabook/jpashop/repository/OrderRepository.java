package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderRepository {

    @PersistenceContext
    EntityManager entityManager;

    public Order findOne(Long orderId) {
        return entityManager.find(Order.class, orderId);
    }

    public List<Order> findAll() {
        return entityManager.createQuery("select m from Order m", Order.class)
                .getResultList();
    }
}
