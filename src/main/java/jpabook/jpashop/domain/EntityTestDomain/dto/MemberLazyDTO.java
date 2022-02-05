package jpabook.jpashop.domain.EntityTestDomain.dto;

import jpabook.jpashop.domain.EntityTestDomain.MemberLazy;
import jpabook.jpashop.domain.EntityTestDomain.Team;
import lombok.Getter;

@Getter
public class MemberLazyDTO {

    private MemberLazy memberLazy;
    private Team team;

    public MemberLazyDTO(MemberLazy memberLazy, Team team) {
        this.memberLazy = memberLazy;
        this.team = team;
    }
}
