package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name=:name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

    public int getCount(String name) {
        return em.createQuery("select count(m) from Member m where m.name=:name", Integer.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    public Boolean isExistByName(String name) {
        return em.createQuery("select count(m) from Member m where m.name=:name", Long.class)
                .setParameter("name", name)
                .getSingleResult() != 0;
    }
}
