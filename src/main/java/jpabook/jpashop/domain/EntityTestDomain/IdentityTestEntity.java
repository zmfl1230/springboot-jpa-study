package jpabook.jpashop.domain.EntityTestDomain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class IdentityTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "identity_generated_id", nullable = false)
    private Long id;

    private String name;
}
