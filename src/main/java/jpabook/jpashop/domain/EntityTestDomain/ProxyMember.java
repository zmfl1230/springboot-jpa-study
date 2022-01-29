package jpabook.jpashop.domain.EntityTestDomain;

import jpabook.jpashop.domain.Address;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class ProxyMember {

    @Id
    private long id;

    private String name;

    @Embedded
    private Address address;
}
