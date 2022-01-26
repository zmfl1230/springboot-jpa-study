package jpabook.jpashop.learningtest;

import jpabook.jpashop.domain.EntityTestDomain.IdentityTestEntity;
import jpabook.jpashop.domain.EntityTestDomain.SequenceTestEntity;
import jpabook.jpashop.domain.EntityTestDomain.SequenceUsingAllocationSizeTestEntity;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 이 테스트는 기본키 생성 전략 관련 테스트이다.
 * 일반적으로 엔티티를 영속성 컨텍스트가 관리하도록 하기 위해서는 생성한 엔티티를 식별할 수 있는 식별자가 반드시 필요하다.
 * (영속성 컨텍스트 내부적으로 `키`와 `인스턴스 참조 값`으로 매핑되어 있다.)
 *
 * 그런데 엔티티를 생성하고자 할때 기본키 생성을 사용하고 있는 데이터베이스에 위임하고자 한다면,
 * 기본키는 `언제` 생성되고 기본키가 생성이 안된 상태에서는 해당 엔티티를 어떻게 `영속화`할 수 있을까? 영속화를 위해서는 기본키가 꼭 필요한데 말이다.
 * 기본키의 생성 시점은 잠시 생각해보면 기본키 생성을 데이터베이스에 위임하고자 했으니, 데이터베이스가 사용하는 기본 전략에 따라(혹은 설정한 전략에 따라)
 * 데이터베이스에 해당 엔티티를 저장하고자 하는 시점에 기본키가 생성이 될 것이다.
 *
 * "그렇다면 영속화는?" 어떻게 해결하는 것일까?
 * 첫번째 방법은 데이터베이스에 생성하고, 추가적으로 영속화를 시키기 위해 데이터베이스로부터 방금 저장한 엔티티의 값을 받아오던지
 * 두번쨰 방법은 데이터베이스에 저장하기 이전에 그 다음 저장될 아이디 값을 먼저 알아온다던지 등의 방법을 이용해 아이디 값을 해결할 것이다.
 *
 * 첫번째 방법이 `GenerationType.IDENTITY` 전략이다.
 * IDENTITY 전략은 기본키 생성 방법은 데이터베이스에 위임하는 것이다. 이러한 이유로 데이터베이스에 저장이 되고 나서야 생성된 기본키 값을 읽어올 수 있다.
 * 결국, 이 전략을 이용하게 되면, 데이터 베이스에 저장한 이후에, 아이디 값을 받아와서 해당 엔티티를 영속화한다.
 * 이렇게 되면, 한번의 insert 문과 해당 엔티티의 기본키 값을 가져오는 select 문이 각각 모두 2번 일어나게 된다.
 *
 * [최적화]
 * 엔티티 저장을 위해 꼭 두번의 요청을 해야하는 것일까?
 * 이러한 문제에 의해 JDBC3 이후부터는 저장 후, 해당 엔티티의 키 값을 받아올 수 있도록 하는 기능이 추가됐다. (getGeneratedKey())
 *
 * [쓰기 지연]
 * 그런데 위 전략을 쓴다면, 트랜젝션 단위로 쓰기 지연이 가능할까?
 * 영속화를 해야 영속 해당 엔티티를 다음 로직에서 사용할 수 있을텐데(엔티티의 키값을 이용한 로직) JPA는 트랜젝션 단위로 쓰기 지연을 하고, 플러쉬 이후에 데이터베이스에 쓰기 요청을 보낸다.
 * 그런데 지금 당장 저장을 해서 기본키 값을 얻어와야 영속화 과정이 이루어질 수 있는 IDENTITY 전략의 경우에는 트랜젝션 단위로의 쓰기 지연이 이래서 불가능하다.
 * 고로, persist(member); 요청이 들어온다면 JPA는 더이상 지체할 것도 없이 바로 데이터베이스에 쓰기 요청을 보낸 뒤, 원하는 값을 받아와 영속화를 진행한다.
 *
 * 두번쨰 방법이 `GenerationType.SEQUENCE` 전략이다.
 * SEQUENCE 전략은 유일한 값을 순서대로 생성하는 시퀀스 생성기를 이용하는 전략이다.
 * 엔티티가 하나씩 데이터베이스에 저장될 때마다 하나씩 올라가는 이 시퀀스 값이 저장되는 엔티티들의 기본키 값이 된다.
 * 즉, 기본키 생성을 데이터베이스의 시퀀스 생성기에 위힘한다. 기본키는 지정한 시퀀스 생성기에 의해 생성되고, 결정된다.
 *
 * 그렇다면, 이 SEQUENCE 전략 또한 데이터베이스 저장한 후에 그 기본키 값을 받아와야 하는가?
 * 이 전략은 IDENTITY 전략과 다르게 다음 시퀀스 값이 무엇인지에 대한 읽기 요청만을 보내고 받아온 시퀀스 값을 해당 엔티티의 기본키 값으로 정의한다.
 * 즉, SEQUENCE 전략은 이러한 이유로 쓰기 지연이 가능하다. 우선 받아온 값으로 영속화를 진행하고, 실제로 데이터베이스에 저장하는 것은 플러쉬된 시점 이후이다.
 *
 * [최적화]
 * 하지만 이 SEQUENCE 전략 또한 매 생성 및 영속화 과정 마다 데이터베이스에 읽기 요청(다음 시퀀스 값을 알아오는)을 보내야 한다.
 * 이 또한 먼가 좀 애매하다. 이 과정을 좀 효율적으로 하는 방법이 없을까?
 * 이러한 에로(?) 상황을 해결하기 위해 이 전략은 `allocationSize` 라는 옵션을 제공한다. 이 옵션을 한마디로 정의하자면 `할당 사이즈만큼 미리 땡겨옴` 이라고 받아드리면 될 것 같다.
 * 미리 사용할만큼의 키 값들(순차적인)을 메모리에 적재해 놓고, 차례대로 사용한 다음 땡겨온 키 값을 다쓰면 그때 다시 요청해 그 다음 키 값들을 또 떙겨오면 된다.
 * 즉, 이 옵션을 사용한다면 옵션 사이즈만큼의 데이터베이스 요청을 절약할 수 있다.
 *
 * [테스트 내용]
 * -IDENTITY 전략 테스트-
 * 1. 트랜젝션이 끝나기 이전에 엔티티에 아이디 값이 저장돼 있는지 확인
 * 2. 트랜젝션이 끝나기 이전에 데이터 베이스에 쿼리를 날렸는지 확인
 * 3. db 통신 횟수 확인 1번
 *
 * -SEQUENCE 전략 테스트-
 * 1. 트랜젝션이 끝나기 이전에 엔티티에 아이디 값이 저장돼 있는지 확인
 * 2. 트랜젝션이 끝난 이후에 데이터 베이스에 쿼리를 날렸는지 확인
 * 3. db 통신 횟수 확인 (1)
 *  3-1. allocationSize 설정 전, 생성한 entity 개수만큼 요청감
 *  3-2. allocationSize 설정 후, 2번
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class PrimaryKeyCreationStrategyTest {

    @PersistenceContext
    EntityManager em;

    /**
     * @result
        this = jpabook.jpashop.learningtest.PrimaryKeyCreationStrategyTest@2daf0cc9
        insert into identity_test_entity (identity_generated_id, name) values (null, NULL);
        this = jpabook.jpashop.learningtest.PrimaryKeyCreationStrategyTest@2daf0cc9

        update identity_test_entity set name='identity test' where identity_generated_id=1;
     *
     * 보면 트랜젝션 종료 이전에 insert 문이 날라갔다. (이후 추가로 update 문도 날라간 로그)
     * 1. 트랜젝션 종료 이전에 아이디 값 저장 확인
     * 2. 트랜젝션 종료 이전에 쿼리 전송 확인 (식별자 얻기 목적)
     * 3. db 통신 발생 1회 (insert 문에 대해서만, 최적화 적용돼 있음)
     */
    @Test
    @DisplayName("IDENTITY 전략 테스트")
    @Transactional
    public void identityTest() {
        IdentityTestEntity entity = new IdentityTestEntity();

        Assertions.assertThat(entity.getId()).isNull();
        // 이 출력물 사이에 insert 문이 있다면, 트랜젝션 중간에 요청 보냄
        System.out.println("this = " + this);

        em.persist(entity);

        System.out.println("this = " + this);
        Assertions.assertThat(entity.getId()).isNotNull();

        entity.setName("identity test");
        em.flush();

    }

    /**
     * @result 총 3번의 디비 요청 횟수(entity 3개)
        call next value for seq
        call next value for seq;

        call next value for seq
        call next value for seq;

        call next value for seq
        call next value for seq;

        entity1 = 1
        entity2 = 2
        entity3 = 3
     */

    @Test
    @DisplayName("SEQUENCE 전략 테스트 - allocationsize 적용 x")
    @Transactional
    public void sequenceTest() {
        SequenceTestEntity entity1 = new SequenceTestEntity();
        SequenceTestEntity entity2 = new SequenceTestEntity();
        SequenceTestEntity entity3 = new SequenceTestEntity();

        // 두번의 키 값 조사 쿼리가 날라가야 함.
        em.persist(entity1);
        em.persist(entity2);
        em.persist(entity3);

        System.out.println("entity1 = " + entity1.getId());
        System.out.println("entity2 = " + entity2.getId());
        System.out.println("entity3 = " + entity3.getId());

        Assertions.assertThat(entity1.getId()).isNotNull();
        Assertions.assertThat(entity2.getId()).isNotNull();

    }

    /**
     * @result entity는 4개지만 2번만 요청 나감
     * call next value for allocation_seq
     * call next value for allocation_seq;

     * call next value for allocation_seq
     * call next value for allocation_seq;
     *
     * entity1 = 1
     * entity2 = 2
     * entity3 = 3
     * entity4 = 4
     */

    @Test
    @DisplayName("SEQUENCE 전략 테스트 - allocationsize 적용 o")
    @Transactional
    public void sequenceUsingAllocationSizeTest() {
        SequenceUsingAllocationSizeTestEntity entity1 = new SequenceUsingAllocationSizeTestEntity();
        SequenceUsingAllocationSizeTestEntity entity2 = new SequenceUsingAllocationSizeTestEntity();
        SequenceUsingAllocationSizeTestEntity entity3 = new SequenceUsingAllocationSizeTestEntity();
        SequenceUsingAllocationSizeTestEntity entity4 = new SequenceUsingAllocationSizeTestEntity();

        // 두번의 키 값 조사 쿼리가 날라가야 함.
        em.persist(entity1);
        em.persist(entity2);
        em.persist(entity3);
        em.persist(entity4);

        System.out.println("entity1 = " + entity1.getId());
        System.out.println("entity2 = " + entity2.getId());
        System.out.println("entity3 = " + entity3.getId());
        System.out.println("entity4 = " + entity4.getId());

        Assertions.assertThat(entity1.getId()).isNotNull();
        Assertions.assertThat(entity2.getId()).isNotNull();

    }


}
