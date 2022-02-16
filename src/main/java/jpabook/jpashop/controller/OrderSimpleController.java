package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleController {

    private final OrderRepository orderRepository;

    /**
     * 에러 발생 REST API
     * 스프링에서 지원하는 Jackson 라이브러리는 proxy 개체 매핑 불가능
     * 고로 연관 필드를 가지고 있는 엔티티 반환 시 에러 발생
     */
    @GetMapping("/order/wrong/{orderId}")
    public Order findOrderWrongWay(@PathVariable("orderId") Long id) {
        return orderRepository.findOne(id);
    }

    /**
     * 조회 환경
     * - 모든 연관관계 필드가 Lazy 로딩된다.
     * - 모든 관계가 양방향 매핑되어있다.
     *
     * order 엔티티의 반환된 내용은 다음과 같다(연관관계 관련된 내용만 다룬다).
     * - member - proxy
     * - orderItems
     *      - order - order entity(이미 조회한 상태니 orderItem 내부의 order는 프록시 객체가 아닌 order entity로 담겨서 나온다.)
     *      - item - proxy
     * - delivery - proxy
     * 즉, 처음 조회한 order entity만 실제 엔티티로 담아오고, 그 외 모든 연관관계 필드는 프록시 객체가 담겨온다.
     *
     * [해결 과정 - 1단계]
     * 가져온 order entity에서 member 프록시 객체와 delivery 프록시 객체와 초기화한다.
     * ```
     * Order order = orderRepository.findOne(id);
     * order.getMember().getName(); // 프록시 강제 초기화
     * order.getDelivery().getAddress(); // 프록시 강제 초기화
     * ```
     *
     * [예상 결과]
     * order 내부적으로 정의된 연관관계 필드들은 모두 초기화 해 주었지만,
     * member와 delivery 내부적으로 갖고 있는 order entity를 제외한 연관관계 필드에는 모두 프록시 객체가 담겨있으므로 이 또한 에러 발생
     * 즉, 멤버 필드의 이름을 가져오는 호출로 멤버 필드와 주소 필드 모두 프록시 객체를 강제 초기화 줄 수는 있어도
     * 그 내부적으로 또 프록시 객체를 가지고 있다.(멤버 필드와 주소 필드 모두 내부적으로 연관 필드를 가지고 있기 때문에)
     *
     * [해결 과정 - 2단계]
     * 초기화 하지 않은 프록시 객체는 노출되지 않도록 해보자.
     * 이를 구현하기 위해 프록시 객체가 담겨있는 경우, 초기화된 프록시 객체만 노출되도록 하는 라이브러리를 사용한다.
     * 다음 선언으로 Hibernate5Module 을 스프링 빈으로 등록한다.
     * ```
     * @Bean
     * Hibernate5Module hibernate5Module() {
     *     return new Hibernate5Module();
     * }
     * ```
     * [예상 결과]
     * 프록시 객체는 무시되어 반환값에 포함되지 않지만, OrderItem 내부적으로 이미 초기화된 order entity를 가지고 있으므로, 무한 루프를 돌게된다.
     * 그 외, 초기화된 엔티티의 경우 내부적으로 order entity를 가지고 있게 된다면 무한정 서로를 호출하게 된다. (이미 서로는 초기화된 상태임)
     *
     * [해결 과정 - 3단계]
     * 현재 문제가 초기화 되지 않은 프록시 객체는 무시되지만, 이미 초기화되어 엔티티화된 경우 양방향 매핑 관게에 의해 무한정 호출되는 문제가 발생한다.
     * 고로 양방향 매핑 관계에 있는 orderItem 내부 필드인 order의 경우 @JsonIgnore 을 붙여 역직렬화 혹은 직렬화 과정에서 무시되도록 한다.
     *
     * [예상 결과]
     * 무한 루프 발생 문제를 해결한다. 반환된 결과에 양방향 매핑 관계였던 order의 경우 무시되어 나오지 않는다.
     * [실제 결과]
     * {
     *     "id": 4,
     *     "member": null, // 초기화되지 않은 객체의 경우 null 값으로 나온다.
     *     "orderItems": [
     *         {
     *             "id": 6,
     *             "item": null,
     *             "orderPrice": 10000,
     *             "quantity": 1
     *         },
     *         {
     *             "id": 7,
     *             "item": null,
     *             "orderPrice": 20000,
     *             "quantity": 2
     *         }
     *     ],
     *     "delivery": null,
     *     "orderDateTime": null,
     *     "orderStatus": null
     * }
     *
     * [해결 과정 - 4단계]
     * 무한루프 문제를 해결했다. 하지만 위의 결과 값은 정말 필요로 하는 값일까? 나는 member, delivery의 값을 얻고 싶다!
     * 현재까지는 라이브러리를 이용해 초기화되지 않은 프록시 객체는 반환되지 않도록 했다. 그럼 다시 해결 과정 2단계로 올라가보자.
     * 그때 직면한 문제는 order 내부 필드는 프록시 초기화를 해주었지만, 내부 필드 내에 프록시가 존재했던게 문제였다. 현재 내가 필요한 것은
     * order 내부 필드인 member, delivery 값이다. 현재 상태에서 조회해 온 order entity에서 member, delivery 강제 초기화 해본다!
     * 물론, memeber와 delivery 내부 필드의 order는 @JsonIgnore을 해준다!
     *
     * (참고)사실 코드로 직접 초기화를 해주었지만, jackson-datatype-hibernate5 라이브러리에서서 강제로 지연 로딩 초기화를 지원한다.
     * 다음과 같이 선언해주면된다.
     * ```
     * hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
     * ```
     *
     * [현재 과정]
     * - Hibernate5Module 빈 등록 (초기화되지 않은 프록시 제외)
     * - order 내부 필드인 member, delivery 코드로 강제 초기화
     * - orderItem, member, delivery 내부적으로 양방향 매핑이 되어있는 order 필드 @JsonIgnore 처리
     *
     * [예상 결과]
     * 내가 원하는 모든 값이 잘 도출된다.
     *
     * [실제 결과]
     * {
     *     "id": 4,
     *     "member": {
     *         "id": 1,
     *         "name": "userA",
     *         "address": {
     *             "city": "서울",
     *             "street": "1",
     *             "zipcode": "1111"
     *         }
     *     },
     *     "orderItems": [
     *         {
     *             "id": 6,
     *             "item": null,
     *             "orderPrice": 10000,
     *             "quantity": 1
     *         },
     *         {
     *             "id": 7,
     *             "item": null,
     *             "orderPrice": 20000,
     *             "quantity": 2
     *         }
     *     ],
     *     "delivery": {
     *         "id": 5,
     *         "address": {
     *             "city": "서울",
     *             "street": "1",
     *             "zipcode": "1111"
     *         },
     *         "deliveryStatus": null
     *     },
     *     "orderDateTime": null,
     *     "orderStatus": null
     * }
     *
     * [해결 과정 - 최종]
     * 위 결과는 만족스럽다. 데이터만 보면...!
     * 하지만 다른 API애서도 해당 엔티티를 사용할텐데 필요한 값이 @JsonIgnore처리가 되어 있다면?? 그떈 또 직렬화 무시 어노테이션을 주석처리해줘야 하나??
     * 매번 API 마다 그 입맛에 entity class를 바꿀수도 없는 노릇이다.
     * 그래서 이러한 모든 문제를 해결하기 위해 요청에 정말로 필요한 데이터만 반활할 수 있도록 해당 요청에 걸맞는 DTO를 선언해 사용한다.
     *
     * [실제 결과]
     * dto에 선언해 놓은 값들로 잘 나온다.
     * {
     *     "orderId": 4,
     *     "name": "userA",
     *     "orderDate": null,
     *     "orderStatus": null,
     *     "address": {
     *         "city": "서울",
     *         "street": "1",
     *         "zipcode": "1111"
     *     }
     * }
     *
     *
     *
     *
     */

    /**
     * 해결과정 - 4단계
     */
    @GetMapping("/order/{orderId}")
    public Order findOrder(@PathVariable("orderId") Long id) {
        Order order = orderRepository.findOne(id);
        order.getMember().getName();
        order.getDelivery().getAddress(); // 프록시 강제 초기화
        return order;
    }

    /**
     * 해결과정 - 5단계
     */
    @GetMapping("/order/dto/{orderId}")
    public OrderDto findOrderReturnDTO(@PathVariable("orderId") Long id) {
        Order order = orderRepository.findOne(id);
        return new OrderDto(order);
    }

    @GetMapping("/orders")
    public List<OrderDto> findOrderAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간 private OrderStatus orderStatus;
        private OrderStatus orderStatus;
        private Address address;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDateTime();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
        }
    }


}
