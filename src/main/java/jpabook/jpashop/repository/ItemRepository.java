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

    // 생성과 병힙을 이용한 업데이트
    public void save(Item newItem) {
        // 비어있으면, 새롭게 생성
        /**
         * 아이디가 없다는 사실은 아직 데이터베이스에 저장된 적 없다는 의미이며, 1차 캐시에 저장되어 있지 않다는 의미이므로
         * persist()를 통해 영속화를 시킨다.
         * (바로 데이터베이스에 쿼리가 날라가든 메모리에 있는 다음 시퀀스 값을 가져오든 해서 아이디값을 부여받을 수 있다.)
         * 결국, persist()를 하면, 아이디 값을 얻을 수 있다.
         */
        if (newItem.getId() == null) em.persist(newItem);
        else {
        // 비어있지 않으면, 기존 값이 머지
        /**
         * merge로 값을 업데이트를 해줄 수 있지만 사실 이건 좋은 방법이 아니다.
         * newItem에서 비어있는 값은 null 값으로 초기화하기 때문이다.
         * 그래서 DTO를 통해 필요한 값만 setXxx()로 초기화 하는 것이 맞다.
        */
        em.merge(newItem);
        }
    }

    public Item findById(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select item from Item item", Item.class)
                .getResultList();
    }

}
