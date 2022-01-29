package jpabook.jpashop.learningtest;

import jpabook.jpashop.domain.EntityTestDomain.ProxyMember;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

/**
 * [미해결 의문]
 * 프록시가 초기화 되기 이전, 아직 target의 값이 null인 상태 이떄는 아직 영속화 이전 상태이다.
 * 예시 상황) entityManager.clear()와 같이 모든 영속 엔티티를 준영속 상태로 만든 뒤, 준영속 엔티티 중 하나의 식별값과 타입을 가지고
 * getReferece()를 호출한 상태
 *
 * 위 상황에서는 프록시가 초기화되기 이전의 상태이며, target 값이 null인 상태이다.
 * 이때 영속성 컨텍스트 내부의 상황
 *
 * 예상 -> 프록시로 생성된 엔티티가 식별자 값과 매필되어 있을 것 같다.
 * 현 상황으로는 아이디 값과 타입만 알고 있는 상태이므로 해당 엔티티의 그외 모든 정보를 알고 있지 않을 뿐더라
 * 만약 그 정보만을 가지고 엔티티를 영속화해서 반환해 준다면 getReference()의 반환 타입이 프록시일리가 없다.
 * 단 매핑이 되어 필요에 의해 사용되고, 대체될 뿐이지 그 프록시 엔티티가 영속화 되었다고 보기는 힘들다.
 *
 * 영속성 컨텍스트가 초기화되면, 프록시 엔티티는 어떻게 될까? -> 해당 프록시 엔티티가 담겼던 참조 변수에 접근하고자 하면. 다음과 같은 에러를 뿜는다.
 * could not initialize proxy [jpabook.jpashop.domain.EntityTestDomain.ProxyMember#1] - no Session
 * org.hibernate.LazyInitializationException: could not initialize proxy [jpabook.jpashop.domain.EntityTestDomain.ProxyMember#1] - no Session
 *
 *
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProxyTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("")
    public void getPersistEntity() {
        ProxyMember member = new ProxyMember();
        member.setId(1L);
        member.setName("member");
        /**
         * 멤버 엔티티가 영속성 컨텍스트에 저장된 상태지만 아직 쿼리는 날이가지 않은 상태
         * 테스트를 위해 아이디 자동 생성 옵션을 끈 상태이다. `@GeneratedValue`
         * 만약 자동 생성 옵션을 킨 상태라면, 아이디 생성을 위해 영속화 즉시 반드시 쿼리를 날릴 것이다.
         * 그렇다면 영속성 컨텍스트에 반드시 데이터베이스에서 조회된 엔티티의 참조 값이 엔티티 식별자와 매핑되어 담기게 된다.
         */
        entityManager.persist(member);

        entityManager.flush();
        // entity manager clear 즉시, 참조하던 proxy 엔티티도 참조 끊음,
        entityManager.clear();

        /**
         * getReference()는 find()와 달리 엔티티를 실제로 사용하는 시점까지 데이터베이스 호출을 미루고, 가짜 프록시 엔티티를 반환한다.
         * 프록시의 목적이 영속화된 엔티티가 실제로 사용되고자 할때까지 데이터베이스로의 요청을 지연시키는 것이다.
         * 하지만, 이미 데이터베이스를 조회했거나 이미 영속화된 엔티티가 있다면 굳이 빈 껍데기인 프록시 엔티티를 반환할 필요가 없다.
         * 그렇다보니 프록시는 자신이 target 값으로 가지고 있는 값을 반환한다.
         *
         * 이러한 이유로 디비에 해당 식별자로 값은 저장돼 있는데 아직 영속화 되지 않은 엔티티나 혹은 준영속 상태의 엔티티와 영속화된 엔티티는 getReference()의 반환 타입이
         * 달라진다. 전자는 프록시 개체가 후자는 영속 엔티티가 반환된다.
         *
         * 또한, 엔티티를 실제 사용하고 할떄 프록시 -> 영속성 컨텍스트로 흐름이 이어지기 때문에 영속성 컨텍스트 내에서 관리되지 않은 비영속과 준영속 엔티티의 경우에는
         * 이 엔티티를 사용하고자 할때 에러를 낸다.
         */

        /**
         * [출력]
         * 프록시 타입
         * reference1 = class jpabook.jpashop.domain.EntityTestDomain.ProxyMember$HibernateProxy$Vgmvv3Pd
         */
        ProxyMember reference1 = entityManager.getReference(ProxyMember.class, member.getId() );
        System.out.println("reference1 = " + reference1.getClass());

        entityManager.clear();

        ProxyMember reference2 = entityManager.getReference(ProxyMember.class, member.getId());
        System.out.println("reference2 = " + reference2);

        /**
         * 아래의 값을 출려하면, 에러가 발생한다.
         * could not initialize proxy [jpabook.jpashop.domain.EntityTestDomain.ProxyMember#1] - no Session
         * org.hibernate.LazyInitializationException: could not initialize proxy [jpabook.jpashop.domain.EntityTestDomain.ProxyMember#1] - no Session
         */
        // System.out.println("reference1 = " + reference1);

        /** 관리하는 프록시 개체가 달라졌읍을 보이기 위한 검증 로직이다.
         * 단순히 영속성 컨텍스트가 초기화되면 하이버네이트가 관리하는 프로시 개체들의 target 값이 null이 되는 것이 아니라 참조가 아예 끝어진다.(약 폐기)
         * */
        Assertions.assertThat(reference1).isNotSameAs(reference2);

    }

    @Test
    @Transactional
    @DisplayName("getReference()호출 순서에 따른 반환 타입 확인하기 find -> getReference")
    public void getPersistEntity1() {
        ProxyMember member = new ProxyMember();
        member.setId(1L);
        member.setName("member");
        entityManager.persist(member);

        /**
         * [출력]
         * ProxyMember 타입
         * reference = class jpabook.jpashop.domain.EntityTestDomain.ProxyMember
         */
        ProxyMember reference = entityManager.getReference(ProxyMember.class, member.getId());
        System.out.println("reference = " + reference.getClass());

        entityManager.flush();
        entityManager.clear();

        /**
         * [출력]
         * ProxyMember 타입
         * proxyMember = class jpabook.jpashop.domain.EntityTestDomain.ProxyMember
         * reference = class jpabook.jpashop.domain.EntityTestDomain.ProxyMember
         *
         */

        ProxyMember proxyMember = entityManager.find(ProxyMember.class, member.getId());
        System.out.println("proxyMember = " + proxyMember.getClass());

        ProxyMember reference1 = entityManager.getReference(ProxyMember.class, member.getId());
        System.out.println("reference1 = " + reference1.getClass());


    }

    @Test(expected = EntityNotFoundException.class)
    @Transactional
    @DisplayName("db에 저장되어 있지 않은 식별자 값을 getReference()로 호출시에는 예외 발생 X, 하지만 실제로 디비로 쿼리를 날리는 시점에서 예외 발생")
    public void getPersistEntity2() {
        ProxyMember reference = entityManager.getReference(ProxyMember.class, 1L);
        System.out.println("reference = " + reference.getName());
    }
}

