# Redis, Kafka 활용 대용량 데이터 처리

# *멀티모듈 프로젝트 구성*
<details>
<summary>펼치기/접기</summary>

### 1. Spring Project 생성 (Root 모듈)
  - 생성된 Root 모듈 프로젝트의 src 디렉토리 제거
### 2. Main Thread 서버 모듈 구성 (module-application)
  - Root 모듈 Project에서 새 Module추가  
    - Spring Initializer 선택  
      (Spring으로 해야 Boot Main Thread 클래스가 생성되며 일반 module일 경우 일반 Main클래스가 생성된다.)
      - Spring module의 경우 아래 부분을 직접 추가해 줘야 한다.
        - {root module}/pom.xml
            ```xml
            </developers>
              <!-- module 추가 시작  -->
              <modules>
                <module>module-application</module>
              </modules>
              <!-- module 추가 종료  -->
            <scm>
            ```
        - {child module}/pom.xml
          ```xml
          <!-- 기존 spring에서 root module로 수정  -->
          <parent>
            <groupId>com.fc</groupId>
            <artifactId>fc-ecommerce</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <relativePath/> <!-- lookup parent from repository -->
          </parent>
          ```
### 3. 순수 컴포넌트 모듈 구성 (module-redis/module-kafka)
  - Root 모듈 Project에서 새 Module추가
    - New Module을 선택
### 4. Root 모듈 pom.xml에 자식 모듈을 dependency로 관리한다.
  - (root module)/pom.xml
   ```xml
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
            scanBasePackages = {"com.fc.moduleredis"}
    )
    public class MainApplication {/*생략*/}
    ```
### cylce 관련 디펜던시 순환참조 문제
- Build Output Error Message
  ```text/plain
  java: Annotation processing is not supported for module cycles. Please ensure that all modules from cycle [module-application,module-redis] are excluded from annotation processing
  ```
- {root module}/pom.xml
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.10.1</version>
        <configuration>
          <compilerArgs>
            <arg>-proc:none</arg> <!-- 애너테이션 프로세서를 비활성화 -->
          </compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>
  ```
</details>

# *Ch01. 이커머스 비즈니스 이해*
<details>
<summary>펼치기/접기</summary>

## 01. 이커머스 비즈니스 타입 및 환경

<details>
<summary>펼치기/접기</summary>

### E-commerce란?

commerce는 상거래라는 뜻으로, E-commerce는 전자상거래를 뜻한다.  
과거의 시장 개념이 온라인 상점으로 옮겨졌다고 쉽게 생각하면 된다.  
과거에는 발로 걸어서 시장을 가거나 차를 타고 시장을 가는 등 실제 대면을 통해 상거래를 진행했다면 현재는 모바일기기 혹은 패드 랩탑 pc 등을 통해 온라인으로 시장 상거래처럼 거래한다.  

### Skateholder: 이해관계자

- 판매자(Seller, 사업자 or 개인)
- 구매자(Buyer, 소비자 or 사업자)
- `Platform 사업자` (OpenMarket: Naver, Coupang, VericalMarket:무신사)  
  \+ 제품이나 서비스를 만드는 사람

### E-commere Business Type

#### Brand Store - ex) Ni*e

실제 프로덕트를 만들고(물론 외주 가능) 브랜드를 만들어 자신들의 공식 홈페이지 웹사이트를 제작하여 고객들이 온라인에서 쉽게 구매할 수 있도록 만드는 역할이다.  
이 사람들의 주 관점은 자사 제품에 대해 어떻게 잘 판매 할 것인지가 주 목적이다.
자신들의 제품을 잘 판매하기 위한 도구로서 온라인 마켓을 이용한다고 볼 수 있다.
그렇기에 상품의 세세한 내용이나 정확한 설명, 소재 등이 명확하게 표현되어 있는 경우가 많다.

#### Open Market- ex) Cou*ang, Na*er

예를 들어 장난감 을 검색했을 때  출력되고 여러 사업자들이 판매하는 장난감에 대한 물건 리스트가 수십 수백 수천개 검색된다.  
이러한 비즈니스 타입을 `오픈 마켓` 이라고 부른다.  
Producer(공급자)가 따로 있고 Seller들이 구매하여 Open Market에 올리는 경우와 Producer가 직접 올리는 두가지 경우가 있다.  
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

## 02. 이커머스 데이터, 트래픽 특징 1
<details>
<summary>펼치기/접기</summary>

### E-commerce data (platform biz)

1. #### 상품의 갯수多  
   같은 상품이라도 판매자에 따라 노출하고자 하는 정보가 다르기 때문에 각 별도로 존재한다.  
   예를들어 커클랜드 골프공을 검색한다고 가정한다.  
   실제 같은 제품임에도 직접 사진을 찍은 사진이 담긴 상품 정보와, 공식 홈페이지에서 촬영한 상품 정보의 사진이 각각 다르다.  
   분명 같은 상품임에도 불구하고 노출되는 제품의 디테일, 사진, 설명, 사업자 정보 등 컨텐츠 내용이 다르다.  
   따라서 같은 상품이라 할지라도 제품별로 노출하고자 하는 컨텐츠가 다르다.  
   이러한 데이터가 중복된 데이터지만 따로 관리해야 하므로 데이터가 굉장히 많아진다.  
2. #### 상품의 다양성多  
   데이터의  Cataloging, Categorazing이 중요하다.  
   예를들어 남성패딩을 검색한다고 가정한다.  
   만원짜리 부터 천만원 까지 가격 레인지가 큰 상품의 리스트들이 조회된다.  
   물론 각각의 상품들이 가지고있는 기능, 소재, 스타일들이 각 제품별로 서로 다르지만 항상 명확한 같은 스타일이 나오는 것이 아니기 때문에 데이터들이 굉장히 많아진다.  
   상품의 레인지가 크다는 것은 만원과 천만원 사이의 수 많은 상품들이 있다는 것이고,  이런것들을 어떻게 Cataloging하느냐, Categorazing하느냐 등  
   데이터 관리에 있어 중요한 점이다 라고 할 수 있다.
3. #### 데이터 통제의 어려움 존재  
   일관된 상품의 정보를 요구하거나 Generation() 해야한다  
    - Stock Keeping Unit  
      우리가 흔히 볼 수 있는 바코드로 실제 재고 관리를 위한 데이터를 담은 코드이다.  
    - SerialNumber  
      전자제품을 구매할때 제품의 고유의 번호로 사용된다.  
      전자 제품 하나하나의 고유번호가 관리되지 않는다면 실제 판매자가 어떤 상품을 보냈는지, 같은 상품인지 다른상품인지 유무를 통제하기 위해 수 많은 데이터들을 만들어 놔야 한다.  
4. #### 판매자의 정보 관리, 지표화  
   Policy, Margin, Quantity, Quality  
   여러가지 판매자 들에게 적용되는 정책이라 던지 판매자들 별 마진 혹은 판매할 수 있는 수량이 정해져 있는 경우가 많다.  
   판매자가 판매하는 제품의 품질이 너무 떨어지거나 가품 여부 등을 관리해야 한다.  
   물론 비즈니스적인 문제일 수 있지만 실제 데이터로도 판별 가능한 것들이 굉장히 많다.  
5. #### 즉각적  
   검색을 위한 keyword 완성, 검색한 keyword를 대상으로 추천, 연관 데이터들 반환  
   예를들어 남성 이라고 입력했을 때 남성에 대해 과거 입력했던 데이터 목록들과, 남성 키워드에 대한 연관 추천 검색 목록들이 즉각적으로 나오게 된다.  
   또, 검색어에 대한 결과로 상품 목록이 나오게 된다면 동일한 키워드의 여러 종류의 상품목록들과 해당 상품과 연관성이 있는 제품들을 추천 목록으로  나오게 된다.  
   ex) `골프공` 검색 =  골프공 A, 골프공 B, 골프공 세트, 골프장갑 등  
   하나의 클릭에 의해 여러 정보를 빠르게 조회해 사용자들 에게 노출 시키는 즉각적인 역할을 한다.  
6. #### 이력 데이터多  
   판매, 가입, 탈퇴, 배송 등의 이력 데이터들이 매우 많기 때문에 각각의 이력 데이터들 잘 관리 하고 데이터들 활용하는 것이 비즈니스에 어떤 도움이 될 지 고민하는 것.  
7. #### 파레토 vs 롱테일  
   경쟁력 있는 20%가 80%의 수익을 가져오는가?  
   그렇지 않다. 작은 수요에도 적극적으로 대응할 수 있는 e-commerce에서는 롱테일 법칙이 적용된다.  
   여기서 말하는 적극적인 대응이란?  
   검색, 추천 등에 있어서 사용자의 이력과 알고리즘, 학습 이용  
    1. 파레토  
       경쟁력 있는 20%의 제품이 80% 수익을 가져온다는 법칙  
       과거 오프라인 환경에서는 매대라는 것이 한정되어 있었기 때문에 파레토 법칙이 적용되는 듯 했음.  
    2. 롱테일  
       굉장히 많은 상품이 적당량, 어느 정도의 판매량을 유지하면서 수익을 견인한다는 법칙  
       롱테일법칙이 적용되면 적극적인 대응들이 필요하다.  
       예를들어 골프공을 사는 사람들이 얼마 되지 않겠지만, 사용자들이 검색했던 히스토리 혹은 이것을 검색하면 인사이트, 마이닝을 통해 이런것들을 검색 하더라 등의 데이터들을 활용하여 사용자들에게 적절한 데이터를 노출 함으로써 수익을 가져오는 원리
8. #### 동일 데이터를 다른 용도로 사용
   같은 데이터임에도 용도에 따라 저장소나 저장방식을 다르게 가져가는 경우가 많다.  
   이는 데이터의 중복도 많다는 의미  
    - RDB (Oracle, MySQL) (일반 적인 strucure 데이터 저장소)  
    - Redis (상품 가격 등)  
    - ElasticSearch (상품 정보 등)  
    - MongoDB (상품 세부 정보 등)  

   각자 다른 방식으로 사용이 가능하며, 한 서비스가 아닌 여러 서비스에서 활용됨으로써, 많은 데이터가 중복된다.  
   이와같이 용도에 따라 저장방식을 다르게 가져가고, 이러한 데이터 이동이 많아짐으로써 카프카라는 툴을 통해 데이터를 이동 시키거나 바로 수급받거나 한다.  

9. #### 데이터 이동多  
   위와 같은 이유로 데이터의 이동이 많으므로 저장된 데이터에 쉽게 접근하고 편리하게 이동시킬 수 있어야 한다.  
10. #### 수익 개선  
    이동이 많은 데이터들과, 많이 쌓이는 데이터들을 통해 수익을 개선할 수 있어야 한다.  
</details>

## 03. 이커머스 데이터, 트래픽 특징 2
<details>
<summary>펼치기/접기</summary>

### E-commerce Traffic (platform biz)

1. #### Promotion
   어떤 플랫폼이던, 마켓 활성화를 위해 기간을 정해 프로모션을 진행한다.  
   (카테고리별, 기간별, 설, 추석, 명절, 블랙프라이데이, 전자제품 등)  
   프로모션의 규모가 클 수록 Traffic에 peak가 발생되며, 이로 인해 서비스 관련 자원들이 유연하게 확장될 수 있어야 한다.  
   (평소 트랜드가 stable하다가도 traffic이 굉장히 많아진다는 것은 사람들이 관심을 보이는 것이다.  
   아마존이 왜 클라우드 사업을 하게 되었는지 보면 알 수 있다.  
   여러 프로모션으로 인해 자사 자원들이 많이 늘고 줄어드는데, 줄었을 때는 프로모션을 하더라도 피크 기간에 잘 대응해서 서비스를 원할하게 운영할 수 있지만 프로모션을 하지 않는 기간에는 트랜드가 굉장히 stable한 상태이기 때문에 자원이 낭비가 되고, 어떻게 활용할 까 고민을 하다가 플랫폼 사업을 하게 된 것이다.)  
   프로모션은 기간을 기준으로 `Daily(특정시간)`, `Weekly(특졍요일)`, `Monthly(특정일)`, `Yearly(특정기간, 주로 소비진작이 많은 시기 = 연말, 연시, 가족의 달, 명절 전 후)` 진행되며, 상품군이나 Seller 기준으로 진행되기도 한다.  
2. #### 새벽시간 Traffic 감소
   대부분이 24/7 으로 운영되지만 트래픽이 적은 구간은 반드시 존재한다.  
   이 때 다양한 점검활동(PM)이 진행되기도 한다.  
   (문제에 대한 PM, 버전업, 업데이트 등)  
   플랫폼이 전 세계에 있다면 다르겠으나 아무래도 주로 사용하는 국가가 있을 것이다.  
   (네이버의 경우 우리나라에서 사용함.)  
   보통 새벽 2시~6시 사이에 물건을 사는 사람이 많지 않다.
3. #### 외부, 내부 Traffic Handling
   네이버에서 기저귀 라고 검색하게 되면 다양한 사이트에서 나오는 가격들이 순서대로 나온다.  
   이는 해당 플랫폼에서 운영하는 api에서 호출함으로써 해당 데이터들을 가져오게 된다.  
   예를들어 __기저귀를 검색하기 위해 키워드를 보내주면 해당 플랫폼이 가지고 있는 제품들 중 가격이 가장 저렴한 제품을 반환 해 준다.  
   (가격 뿐만 아니라 실제 접속 가능한 주소 정보도 포함된다.)  
   inbound 되는 주소지가 내부인지 외부인지에 따라 제어방향이 다르고, 수준이 다르다.  
   (네이버에서 초당 100만 건씩 호출한다고 가정한다면, 그것들을 다 받아줄 수는 없다.)  
   이에 따라 cdn, network구조, security, service circuit breaker 등의 구현방법이 다르다.

### 특징

BigData, **`Log`**(여러 이력데이터를 확인), Catalog, Governance(제어), Longtail,  **`Varidation`**(Query Defined 변형 재각인-사용), **`Immediacy`**(즉각성-검색추천),
**`Duplication`**(중복), Scalabilty(늘어남), **`Optimization`**(최적화), Control(제어), Policy(정책)

### 프로젝트

- #### LOG
  상품 데이터와 이력 데이터 이용  
- #### Immediacy, Duplication
  데이터 이용을 위해 Kafka 사용 - 즉각적 대응  
  예를들어 웹사이트의 로그를 확인하여 해당 데이터를 끌어와 서비스에서 그에 맞는 리턴값을 다시 던져주는 등의 역할
- #### Variation
  Request Defined 된 구조를 가진 Redis 사용  
  예를들어 사용자들은 상품 전체 정보를 원하지 않고 가격만 원하기 때문에 상품 고유번호와 가격만 가지고있는 캐시를 마련하는 등의 예시가 있다
- #### Immediacy, Duplication
  빠르게 응답할 수 있는 Cache 용도로 Redis 사용
- #### Optimaization
  Service 최적화 목적  
  비즈니스를 이해하려는 목적은 정확한 목적을 설정하고 그에 따른 적절한 품질을 설정할 수 있다.  
  10초 혹은 0.1초 안에 결과 도출 등 적절한 요구사항에 맞춰 그에 맞는 최적화 된 서비스를 설계 하는 것을 말한다.  
- #### Policy
  사전에 정의한 품질 수준 내에서 사용자 호출 수행
</details>
</details>