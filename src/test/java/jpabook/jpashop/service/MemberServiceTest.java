package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;


    Member member1;
    Member member2;

    @Before
    public void setUp() {
        System.out.println("MemberServiceTest.setUp");
        member1 = new Member();
        member1.setName("same");

        member2 = new Member();
        member2.setName("same");
    }


    @Test
    public void 회원가입() throws Exception {
        //When
        memberService.save(member1);

        //Then
        assertThat(member1).isSameAs(memberService.findById(member1.getId()));

    }


    @Test(expected = IllegalStateException.class)
    @DisplayName("이미 존재하는 이름으로 가입")
    public void duplicateName() throws Exception {
        //when
        memberService.save(member1);

        //Then
        memberService.save(member2);
    }


    @Test
    @DisplayName("회원 이름으로 회원 리스트 가져오기")
    public void findByName() throws Exception {
        //Given
        memberService.save(member1);

        //When
        List<Member> members = memberService.findByName("same");
        
        for(Member member : members) {
            System.out.println("member.getName() = " + member.getName());
        }

        //Then
        assertThat(members.size()).isEqualTo(1);


    }


    @Test
    @DisplayName("회원 이름으로 등록된 회원 수 가져오기")
    public void getMemberCountByName() throws Exception {
        //Given
        memberService.save(member1);

        //When
        int memberCountByName = memberService.getMemberCountByName("same");

        //Then
        assertThat(memberCountByName).isEqualTo(1);
    }


    @Test
    @DisplayName("회원 이름으로 등록된 회원이 존재하는지 확인하기 (존재함)")
    public void memberExist() throws Exception {
        //Given
        memberService.save(member1);

        //When
        boolean exist = memberService.isMemberExistByName("same");

        //Then
        assertThat(exist).isEqualTo(true);
    }

    @Test
    @DisplayName("회원 이름으로 등록된 회원이 존재하는지 확인하기 (존재안함)")
    public void memberExistX() throws Exception {

        //When
        boolean exist = memberService.isMemberExistByName("same");

        //Then
        assertThat(exist).isEqualTo(false);
    }
}