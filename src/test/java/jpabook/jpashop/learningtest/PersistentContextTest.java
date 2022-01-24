package jpabook.jpashop.learningtest;

import jpabook.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class PersistentContextTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("영속성 컨텍스트에서 반환된 값, 동일성 보장")
    public void ensureIdentity() {
        Member member = new Member();
        member.setName("memberA");

        em.persist(member);

        Member member1 = em.find(Member.class, member.getId());
        Member member2 = em.find(Member.class, member.getId());

        Assertions.assertThat(member1).isSameAs(member2);
    }

    /**
     * find 반환 값 vs merge 반환 값
     * find - 전달된 아이디로 매핑된 오브젝트 그대로 반환, 같은 트랜젝션 내에 계속해서 찾아도 같은 값이 반환되므로 동일성 보장
     * merge - 준영속 엔티티를 영속 엔티티로 변경한 새로운 오브젝트를 반환하기 때문에 동일성 보장 안됨
     */

    @Test
    @DisplayName("entity manager에서 반환된 객체와 영속화한 오브젝트, 동일성 보장")
    public void ensureIdentity2() {
        Member member = new Member();
        member.setName("memberA");


        // member를 영속화한 상태
        em.persist(member);

        // 영속성 컨텍스트에서 member id를 가지고 조회 후, 영속 엔티티 반환
        Member member1 = em.find(Member.class, member.getId());

        // member == member1
        Assertions.assertThat(member1).isSameAs(member);

        Assertions.assertThat(em.contains(member)).isTrue();
        Assertions.assertThat(em.contains(member1)).isTrue();

    }

    @Test
    @DisplayName("준영속 엔티티와 준영속 엔티티를 영속화한 뒤 반환된 새로운 오브젝트, 동일성 보장X")
    public void ensureIdentity3(){
        //Given
        Member member = new Member();
        member.setName("memberA");
        // 영소화 후, 준영속 상태로 전환
        em.persist(member);
        em.detach(member);

        //When
        // 준영속 상태의 member를 `새로운 영속상태 엔티티`로 반환
        Member mergeMember = em.merge(member);
        Member mergeMember1 = em.merge(member);

        System.out.println("member = " + member);
        System.out.println("mergeMember = " + mergeMember);
        System.out.println("mergeMember1 = " + mergeMember1);

        //Then
        Assertions.assertThat(member).isNotSameAs(mergeMember);
        Assertions.assertThat(mergeMember1).isNotSameAs(mergeMember);

        Assertions.assertThat(em.contains(member)).isFalse();
        Assertions.assertThat(em.contains(mergeMember)).isTrue();

    }
    /**
     * "준영속 엔티티를 다시 영속 엔티티로 전환하고자 한다면, 다시 persist에 해당 엔티티를 넘겨주면 되지 않을까?"
     * 라는 생각에 아래 테스트를 진행한다.
     *
     * [결과]
     * `detached entity passed to persist`
     * -> 이미 영속성 컨텍스트에서 분리된 엔티티는 persist로 넘겨줄 수 없다. 고로, merge를 이용해 전화해 새로운 오브젝트로 반환받아야 한다.
     *
     * merge는 비영속, 준영속 엔티티 모두 영속 엔티티로 만들 수 있으나 새로운 영속 엔티티로 반환한다.
     * merge는 앤티티의 식별자 값으로 1차 캐시를 조회했을때, 매핑된 내용이 없으면 데이터 베이스에서 조회해 1차 캐시에 저장한다.
     * 머지할 값(인자로 넘겨받은)을 새롭게 생성된(mergeMember에 담길) 오브젝트에 밀어넣고, 새롬게 생성된 오브젝트를 반환한다.
     */
    @Test(expected = PersistenceException.class)
    @DisplayName("준영속 엔티티를 영속화하는 방법 - persist 사용 불가")
    public void ensureIdentity5(){
        //Given
        Member member = new Member();
        member.setName("memberA");


        //When
        // 영소화 후, 준영속 상태로 전환
        em.persist(member);
        em.detach(member);

        //Then
        // 준영속 상태의 member를 `새로운 영속상태 엔티티`로 반환
        em.persist(member);

    }

    @Test
    @DisplayName("준영속 엔티티를 영속화하는 방법 - merge")
    public void ensureIdentity4(){
        //Given
        Member member = new Member();
        member.setName("memberA");
        // 영소화 후, 준영속 상태로 전환
        em.persist(member);
        em.detach(member);

        //When
        // 준영속 상태의 member를 `새로운 영속상태 엔티티`로 반환
        Member mergeMember = em.merge(member);

        //Then
        Assertions.assertThat(member).isNotSameAs(mergeMember);

        Assertions.assertThat(em.contains(mergeMember)).isTrue();

    }





}
