package jpabook.jpashop.domain.EntityTestDomain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id", nullable = false)
    private Long id;

    private String teamName;

    @OneToMany(mappedBy = "team")
    private List<MemberEager> eagerMembers = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<MemberLazy> lazyMembers = new ArrayList<>();
}
