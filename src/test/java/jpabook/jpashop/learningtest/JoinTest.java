package jpabook.jpashop.learningtest;

import jpabook.jpashop.domain.EntityTestDomain.MemberEager;
import jpabook.jpashop.domain.EntityTestDomain.MemberLazy;
import jpabook.jpashop.domain.EntityTestDomain.Team;
import jpabook.jpashop.domain.EntityTestDomain.dto.MemberLazyDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * 글로벌 로딩 전략에 따른 페치 조인과 내부 조인에 대해 알아본다.
 *
 * `페치 조인`은 어떤 글로벌 로딩 전략을 사용하더라도 다대일, 일대다 조인에서 한번의 쿼리로 연관 필드까지(지연 로딩 없이) 탐색이 가능하게 한다.
 *
 * 글로벌 로딩 전략에서 `EAGER`는 즉시 로딩을 `LAZY`는 지연 로딩 전략을 취한다. 하지만, 어떤 전략을 취하든 한번의 쿼리를 더 실행한다.
 * (이떄 LAZY 전략의 경우 실제 해당 필드를 사용하기 전까지는 프록시 객체로 유지하다가 실제로 연관객체를 사용하고자하는 시점에 쿼리를 한번 더 날린다.
 * 고로 이 전략 또한 한번의 쿼리가 더 실행된다고 볼 수 있다.)
 *
 * 이러한 이유로 일반적으로 글로벌 전략의 경우 LAZY로 유지하고(매번 사용하지도 않을 객체를 즉시 불러오는 것은 비효율적이므로),
 * 필요시에(반드시 접근할 것같은 연관객체의 경우) 페치 조인을 이용해 한번의 쿼리로 필요한 연관객체까지 조인해 불러오는 것이 일반적인 방법이다.
 *
 * [테스트의 핵심]
 * 1. 그런데 문득 이 페치 조인은 JPQL에서 성능 최적화를 위해 지원하는 기능인데 어떤식으로 쿼리가 날라가며,
 * 만약 일반 조인으로 쿼리가 날라간다면 그냥 조인하는 것과 그 결과가 어떻게 다른지가 궁금해졌다.
 *
 * 2. 또 만약에 이 둘이 명확하게 다르다면 내부 조인을 페치 조인처럼 사용할 수 있는 방법은 없을까를 고민해본다.
 *
 * [테스트 시나리오]
 * 1. 페치 조인 시, 쿼리 확인
 * 2. 내부 조인 시, 쿼리 확인
 * 3. 내부 조인 페치 조인처럼 사용하기
 *
 * [추후에 해볼만한 테스트]
 * 2. 로딩 전략 EAGER
 *  2.2. 다대일 조인
 * 3. 로딩 전략 LAZY
 *  3.2. 다대일 조인
 *
 * [테스트 엔티티]
 * MemberEager
 * MemberLazy
 * Team
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class JoinTest {

    @PersistenceContext
    EntityManager entityManager;

    Team team;
    MemberEager memberEager1;
    MemberLazy memberLazy1;

    @Before
    public void setUp() {
        team = new Team();
        team.setTeamName("team");

        memberEager1 = new MemberEager();
        memberEager1.setName("eager1");
        memberEager1.setTeam(team);

        memberLazy1 = new MemberLazy();
        memberLazy1.setName("lazy1");
        memberLazy1.setTeam(team);

    }

    /**
     * [출력 결과]
     * select
         * memberlazy0_.member_id as member_i1_7_0_,
         * team1_.team_id as team_id1_13_1_,
         * memberlazy0_.name as name2_7_0_,
         * memberlazy0_.team_id as team_id3_7_0_,
         * team1_.team_name as team_nam2_13_1_
     * from member_lazy memberlazy0_ inner join team team1_ on memberlazy0_.team_id=team1_.team_id;
     *
     * [결론]
     * JPQL에서 페치조인은 내부 조인을 사용해 쿼리를 보내는 것을 확인할 수 있다.
     * JPQL과 다른 점은 JPQL에선 select 문에 m (MemberLazy entity projection) 을 조회 대상으로 지정했으나
     * 실제 SQL문으로는 memberlazy와 team이 조회되었다.
     */

    @Test
    @Transactional
    @DisplayName("페치 조인 쿼리 확인")
    public void checkQueryFetchJoin() {

        entityManager.persist(team);
        entityManager.persist(memberLazy1);

        String LazyJPQL = "select m from MemberLazy m join fetch m.team";
        MemberLazy singleResult1 = entityManager.createQuery(LazyJPQL, MemberLazy.class).getSingleResult();

        System.out.println("singleResult1 = " + singleResult1);
    }

    /**
     * [출력 결과]
     * - 로딩 전략, EAGER
     * select
         * membereage0_.member_id as member_i1_6_,
         * membereage0_.name as name2_6_,
         * membereage0_.team_id as team_id3_6_
     * from member_eager membereage0_ inner join team team1_ on membereage0_.team_id=team1_.team_id;
     *
     * - 로딩 잔략, LAZY
     * select
         * memberlazy0_.member_id as member_i1_7_,
         * memberlazy0_.name as name2_7_,
         * memberlazy0_.team_id as team_id3_7_
     * from member_lazy memberlazy0_ inner join team team1_ on memberlazy0_.team_id=team1_.team_id;
     *
     * [결론]
     * 페치 조인과는 다르게 실제로 프로젝션 대상으로 지정한 값만 조회가 이루어졌다.
     * 즉, SQL의 SELECT 절을 보면 회원만 조회하고 팀에 대한 정보는 전혀 조회하지 않았다. 단순히 SELECT 절에 지정한 대상만 조회했을뿐이다.
     * 이렇게 되면 딱히 join을 하는 이유가 없어지긴한다.
     *
     * 단순 내부 조인은 글로벌 로딩 전략이 지연이든 즉시든 프로젝션 대상만을 가져온다.
     */

    @Test
    @Transactional
    @DisplayName("내부 조인 쿼리 확인")
    public void checkQueryInnerJoin() {

        entityManager.persist(team);
        entityManager.persist(memberEager1);
        entityManager.persist(memberLazy1);

        String EagerJPQL = "select m from MemberEager m join m.team";
        MemberEager singleResult = entityManager.createQuery(EagerJPQL, MemberEager.class).getSingleResult();

        System.out.println("singleResult = " + singleResult);

        String LazyJPQL = "select m from MemberLazy m join m.team";
        MemberLazy singleResult1 = entityManager.createQuery(LazyJPQL, MemberLazy.class).getSingleResult();

        System.out.println("singleResult1 = " + singleResult1);
    }

    /**
     * [출력 결과]
     * - 조회 대상: m, t
     * select
         * membereage0_.member_id as member_i1_6_0_,
         * team1_.team_id as team_id1_13_1_,
         * membereage0_.name as name2_6_0_,
         * membereage0_.team_id as team_id3_6_0_,
         * team1_.team_name as team_nam2_13_1_
     * from member_eager membereage0_ inner join team team1_ on membereage0_.team_id=team1_.team_id;
     *
     * - 조회 대상: new jpabook.jpashop.domain.EntityTestDomain.dto.MemberLazyDTO(m, t)
     * select
         * memberlazy0_.member_id as col_0_0_,
         * team1_.team_id as col_1_0_
     * from member_lazy memberlazy0_ inner join team team1_ on memberlazy0_.team_id=team1_.team_id;
     *
     * [결론]
     * inner join으로 쿼리를 보내고 프로젝션 대상에 회원과 팀 모두를 넣어주면 fetch 조인과 같은 쿼리를 실행한다.
     * 하지만, 어플리케이션 단에서 반환 타입을 지정하기 애매하고, DTO로 반환값을 매핑한 경우에는 실행하는 쿼리가 달라짐을 알 수 있었다.
     * 결론적으로 fetch join은 inner join 조인이지만, 반환 객체 관리를 편리하게 하면, 한번의 쿼리로 최적화된 객체 그래프 탐색을 제공한다.
     *
     */

    @Test
    @Transactional
    @DisplayName("내부 조인 페치 조인 처럼 사용하기")
    public void usingInnerSuchLikeFetch() {

        entityManager.persist(team);
        entityManager.persist(memberEager1);
        entityManager.persist(memberLazy1);

        String EagerJPQL = "select m, t from MemberEager m join m.team t";
        List<Object[]> resultList = entityManager.createQuery(EagerJPQL).getResultList();

        for(Object[] result : resultList) {
            System.out.println("Member = " + (MemberEager) result[0]);
            System.out.println("Team = " + (Team)result[1]);
        }

        String LazyJPQL = "select new jpabook.jpashop.domain.EntityTestDomain.dto.MemberLazyDTO(m, t)  from MemberLazy m join m.team t";
        List<MemberLazyDTO> resultList1 = entityManager.createQuery(LazyJPQL, MemberLazyDTO.class).getResultList();

        for(MemberLazyDTO result : resultList1) {
            System.out.println("Member = " + result.getMemberLazy().getName());
            System.out.println("Team = " + result.getTeam().getTeamName());
        }

    }


}