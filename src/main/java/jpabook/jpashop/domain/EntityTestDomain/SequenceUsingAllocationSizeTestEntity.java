package jpabook.jpashop.domain.EntityTestDomain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@SequenceGenerator(
        name = "sequence_allocation_size_generator",
        sequenceName = "allocation_seq",
        allocationSize = 10
)
@Getter @Setter
public class SequenceUsingAllocationSizeTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_allocation_size_generator")
    @Column(name = "sequence_allo_generated_id", nullable = false)
    private Long id;

    private String name;

}
