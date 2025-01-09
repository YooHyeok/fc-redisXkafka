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
    - `Root Project 마우스 우클릭` > `New` > `Module` > `좌측 사이드바 최 상단 New Module을 선택` > `module 이름 입력`

이때, root module의 pom.xml에는 아래와 같이 module이 자동으로 추가된다.
  - (root module)/pom.xml
    ```xml
     </developers>
     <modules>
       <module>module-application</module> <!-- 직접 추가됨 -->
       <module>module-redis</module> <!-- 자동 추가됨 -->
       <module>module-kafka</module> <!-- 자동 추가됨 -->
     </modules>
     <scm>
    ```
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
            scanBasePackages = {"com.fc.moduleredis", "com.fc.modulekafka"}
    )
    public class MainApplication {/*생략*/}
    ```
### 6. 공통 모듈 Dependency
Lombok과 같은 공통 모듈은 root 모듈에 선언한다.  
이때, scope 옵션을 provided로 적용한다.
- (root module)/pom.xml
  ```xml
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
  </dependency>
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
  
위 방법으로 해결되지 않았으며, 루트 pom.xml에 있는 3개(application, redis, kafka)의 멀티 모듈에 대한 의존성을 모두 제거한 뒤,  
redis와 kafka를 직접적으로 사용하는 application 모듈에 두 의존성을 추가함.
(이때 root 모듈의 module로 등록된 자식 모듈들은 그대로 구성해야하며 이전에 설정한 애너테이션 프로세서를 비활성화 플러그인을 제거해야 한다.)

- {root module}/pom.xml
  ```xml
  <modules>
    <module>module-application</module>
    <module>module-redis</module>
    <module>module-kafka</module>
  </modules>
  <!-- 자식모듈 dependency 제거 -->
  <!-- 애너테이션 프로세서를 비활성화 플러그인 제거 -->
  ```
- module-application/pom.xml
  ```xml
  <dependency>
        <groupId>com.fc</groupId>
        <artifactId>module-redis</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.fc</groupId>
        <artifactId>module-kafka</artifactId>
        <version>0.0.1-SNAPSHOT</version>
  </dependency>
  ```
- {자식 module}/pom.xml
  ```xml

  ```

### 최종 구성 파일
- [루트 - pom.xml](pom.xml)
- [자식 공통 모듈 - pom.xml](module-application%2Fpom.xml)
- [자식 모듈1 Redis - pom.xml](module-redis%2Fpom.xml)
- [자식 모듈2 Kafka - pom.xml](module-kafka%2Fpom.xml)

### 프로젝트 최종 구성 디렉토리
📂`프로젝트(루트모듈)`: **fc-ecommerce**   
┠ 📂 `자식 공통모듈`: **module-application**(Spring)  
┃ ┖ 📄 pom.xml  
┠ 📂 `자식 모듈 1`: **module-kafka**(Maven)  
┃ ┖ 📄 pom.xml  
┠ 📂 `자식 모듈 2`: **module-redis**(Maven)  
┃ ┖ 📄 pom.xml  
┖━━━━━━━━━━━━━━━━━━━━━━━━━
</details>

# [Ch01. 이커머스 비즈니스 이해.md](Ch01.%20%EC%9D%B4%EC%BB%A4%EB%A8%B8%EC%8A%A4%20%EB%B9%84%EC%A6%88%EB%8B%88%EC%8A%A4%20%EC%9D%B4%ED%95%B4.md)
# [Ch02. Redis 활용 서비스 설계.md](Ch02.%20Redis%20%ED%99%9C%EC%9A%A9%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%84%A4%EA%B3%84.md)
# [Ch03. Redis의 기본개념.md](Ch03.%20Redis%EC%9D%98%20%EA%B8%B0%EB%B3%B8%EA%B0%9C%EB%85%90.md)
# [Ch04. Redis 설치 및 Configuration.md](Ch04.%20Redis%20%EC%84%A4%EC%B9%98%20%EB%B0%8F%20Configuration.md)
# [Ch05. Redis 활용 서비스 개발.md](Ch05.%20Redis%20%ED%99%9C%EC%9A%A9%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EA%B0%9C%EB%B0%9C.md)