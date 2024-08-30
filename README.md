# Redis, Kafka 활용 대용량 데이터 처리

# 멀티모듈 프로젝트 구성
<details>
<summary>펼치기/접기</summary>

### 1. Spring Project 생성 (Root 모듈)
  - 생성된 Root 모듈 프로젝트의 src 디렉토리 제거
### 2. Main Thread 서버 모듈 구성 (module-application)
  - Root 모듈 Project에서 새 Module추가  
    - Spring Initializer 선택  
      (Spring으로 해야 Boot Main Thread 클래스가 생성되며 일반 module일 경우 일반 Main클래스가 생성된다.)
### 3. 순수 컴포넌트 모듈 구성 (module-redis/module-kafka)
  - Root 모듈 Project에서 새 Module추가
    - New Module을 선택
### 4. Root 모듈 pom.xml에 자식 모듈을 dependency로 관리한다.
  - (root module)/pom.xml
   ```
   <dependency>
       <groupId>com.fc</groupId>
       <artifactId>module-redis</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   <dependency>
       <groupId>com.fc</groupId>
       <artifactId>module-application</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```
### 5. Main Thread 서버 모듈의 Main클래스 수정
- @SpringBootApplication 어노테이션 scanBasePackages 옵션 추가
  - module-application/MainApplication.java
    ```java
    @SpringBootApplication(
            /* 모든 모듈을 다 스캔하는 것은 시간도 오래걸리고 굉장히 비효율적이기 때문에, 빈으로 등록해야 되는 필요한 것들만 명시한다. */
            scanBasePackages = {"com.fc"}
    )
    public class MainApplication {/*생략*/}
    ```
</details>

# Ch01. 이커머스 비즈니스 이해
<details>
<summary>펼치기/접기</summary>

## 01. 이커머스 비즈니스 타입 및 환경

### E-commerce란?

commerce는 상거래라는 뜻으로, E-commerce는 전자상거래를 뜻한다.

과거의 시장 개념이 온라인 상점으로 옮겨졌다고 쉽게 생각하면 된다.

과거에는 발로 걸어서 시장을 가거나 차를 타고 시장을 가는 등 실제 대면을 통해 상거래를 진행했다면 현재는 모바일기기 혹은 패드 랩탑 pc 등을 통해 온라인으로 시장 상거래처럼 거래한다.

### Skateholder: 이해관계자

- 판매자(Seller, 사업자 or 개인)
- 구매자(Buyer, 소비자 or 사업자)
- `Platform 사업자` (OpenMarket: Naver, Coupang, VericalMarket:무신사)
+ 제품이나 서비스를 만드는 사람

## E-commere Business Type

### Brand Store - ex) Ni*e

실제 프로덕트를 만들고(물론 외주 가능) 브랜드를 만들어 자신들의 공식 홈페이지 웹사이트를 제작하여 고객들이 온라인에서 쉽게 구매할 수 있도록 만드는 역할이다.

이 사람들의 주 관점은 자사 제품에 대해 어떻게 잘 판매 할 것인지가 주 목적이다.

자신들의 제품을 잘 판매하기 위한 도구로서 온라인 마켓을 이용한다고 볼 수 있다.

그렇기에 상품의 세세한 내용이나 정확한 설명, 소재 등이 명확하게 표현되어 있는 경우가 많다.

### Open Market- ex) Cou*ang, Na*er

예를 들어 장난감 을 검색했을 때  출력되고 여러 사업자들이 판매하는 장난감에 대한 물건 리스트가 수십 수백 수천개 검색된다.

이러한 비즈니스 타입을 `오픈 마켓` 이라고 부른다.

Producer(공급자)가 따로 있고 Seller들이 구매하여 Open Market에 올리는 경우와

Producer가 직접 올리는 두가지 경우가 있다.

이러한 플랫폼 사업자들의 주관점은 마켓을 얼마나 어떻게 활성화 시킬 수 있을까 라는 고민이 주된 주관점이다.

좀더 많은 Seller(상인) 들이 모여야 물건의 수가 풍부해지고, 구매자들이 소비할 컨텐츠들이 점점 많아지면서 마켓이 점점 커지고 그로 인해 플랫폼 사업자가 얻는 중간 마진 등이 올라갈 수 있기 때문이다.

### Brand Store Type과 차이점

1. 상품의 개수와 다양성이 많다.
   상품의 수가 굉장히 많다.
   예시로 들었던 Ni*e는 해당 브랜드에서 만든 상품이 대부분 이다.
   하지만 오픈마켓의 경우 사업자가 늘수록 계속 상품이 늘어나고, 가격, 스타일 등 상품 카테고리의 다양성이 굉장히 많아진다
2. 판매자 관리
   편한 환경을 제공함으로 써 마켓의 이점을 충분히 어필하여 판매자를 모으는것이 중요하다.
3. 정보 통제의 어려움
   굉장히 많은 사업자들이 존재하기 때문에 그들이 관리하는 데이터 체계와 플랫폼에서 관리하는 정보 체계가 다를수 밖에 없으므로 이러한 것들을 하나로 모아 통합하여 관리해야 한다.
</details>