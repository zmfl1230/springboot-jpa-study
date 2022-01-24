package jpabook.jpashop.learningtest;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class CreateQueryReturnValueTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("createQuery(jsql, `반환값 클래스`) 테스트")
    /**
     * [Type specified for TypedQuery is incompatible with query return type] 문제 해결을 위한  테스트
     * createQuery(jsql, `반환값 클래스`) 에서 반환값을 찾고자하는 태이블이 아닌 반환 값 클래스로 명시해주어야 한다.
     */
    public void test() throws Exception {
        //Given
        Member member = new Member();
        member.setName("same");
        memberService.save(member);


        //When
        boolean name = em.createQuery("select count(m) from Member m where m.name=:name", Long.class)
                .setParameter("name", member.getName())
                .setMaxResults(1)
                .getSingleResult() != null;

        TypedQuery<Long> name1 = em.createQuery("select count(m) from Member m where m.name=:name", Long.class)
                .setParameter("name", member.getName());


        //Then
        System.out.println("result = " + name1.getSingleResult()); // 1
        Assertions.assertThat(name).isTrue();

    }

    @Test
    public void test1() throws Exception {
        //Given
        Member member = new Member();
        member.setName("same");
        memberService.save(member);

        //When
        /**
         * Type specified for TypedQuery [java.lang.Integer] is incompatible with query return type [class java.lang.Long]
         * 즉, count 반환 값 `Long`, 반환 값 Integer로 설정 시 예외 발생
         */
        TypedQuery<Long> name1 = em.createQuery("select count(m) from Member m where m.name=:name", Long.class)
                .setParameter("name", member.getName())
                .setFirstResult(1);

        //Then
        System.out.println("result = " + name1.getFirstResult()); // 1
    }

}