package jpabook.jpashop.domain.EntityTestDomain.dto;

import jpabook.jpashop.domain.EntityTestDomain.MemberEager;
import jpabook.jpashop.domain.EntityTestDomain.Team;

public class MemberEagerDTO {

    private MemberEager memberEager;
    private Team team;

    public MemberEagerDTO(MemberEager memberEager, Team team) {
        this.memberEager = memberEager;
        this.team = team;
    }
}
