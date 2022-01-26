package jpabook.jpashop.domain.EntityTestDomain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@SequenceGenerator(
        name = "sequence_generator",
        sequenceName = "seq",
        allocationSize = 1
)
@Getter @Setter
public class SequenceTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @Column(name = "sequence_generated_id", nullable = false)
    private Long id;

    private String name;

}
