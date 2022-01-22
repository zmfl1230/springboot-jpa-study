package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ItemRepository {

    @Autowired
    EntityManager em;

    public void save(Item item) {
        // 비어있으면, 새롭게 생성
        if (item.getId() == null) em.persist(item);

        // 비어있지 않으면, 기존 값이 머지
        else em.merge(item);
    }

    public Item findById(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select item from Item item", Item.class)
                .getResultList();
    }

}
