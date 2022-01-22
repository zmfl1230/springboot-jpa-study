package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired
    ApplicationContext ac;

    @Test
    public void getAllBeans() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for(String beanName: beanDefinitionNames) {
            System.out.println("beanName = " + beanName);
        }

    }
    @Test
    @Transactional
    public void saveMember() {
        //given
        Member member = new Member();
        member.setName("memberA");

        //when
        long memberId = memberRepository.save(member);
        Member findMember = memberRepository.find(memberId);

        //then
        Assertions.assertThat(memberId).isEqualTo(findMember.getId());
        Assertions.assertThat(member.getName()).isEqualTo(findMember.getName());

        Assertions.assertThat(member).isEqualTo(findMember);

    }

}