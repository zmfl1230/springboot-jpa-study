package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public void save(Member member) {
        validateDuplicateName(member.getName());
        memberRepository.save(member);
    }

    private void validateDuplicateName(String name) {
        if(memberRepository.isExistByName(name)) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public Member findById(Long memberId) {
        return memberRepository.find(memberId);
    }

    public List<Member> findByName(String name) {
        return memberRepository.findByName(name);
    }

    public int getMemberCountByName(String name) {
        return memberRepository.getCount(name);
    }

    public boolean isMemberExistByName(String name) {
        return memberRepository.isExistByName(name);
    }
}
