package jpabook.jpashop.repository;

import jpabook.jpashop.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    public Member find(long memberId) {
        return em.find(Member.class, memberId);
    }
}
