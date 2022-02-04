# JPA study
**JPA 관련 다양한 실험(?)과 Learning Test를 진행합니다.**  
아래는 실험을 진행하고 고찰한 내용을 담은 파일경로들을 기록합니다 :)



✍🏻  [기본키 생성 전략](https://github.com/zmfl1230/springboot-jpa-study/blob/master/src/test/java/jpabook/jpashop/learningtest/PrimaryKeyCreationStrategyTest.java)
- IDENTITY 전략 테스트
- SEQUENCE 전략 테스트

✍🏻  [Proxy 객체](https://github.com/zmfl1230/springboot-jpa-study/blob/master/src/test/java/jpabook/jpashop/learningtest/ProxyTest.java)
- Proxy 객체의 확인
- getReference()호출 순서에 따른 반환 타입 확인
- db에 저장되어 있지 않은 식별자 값을 getReference()로 호출


✍🏻  [엔티티 생명주기](https://github.com/zmfl1230/springboot-jpa-study/blob/master/src/test/java/jpabook/jpashop/learningtest/PersistentContextTest.java)
- 준영속 엔티티 vs 영속 엔티티
- find vs merge

