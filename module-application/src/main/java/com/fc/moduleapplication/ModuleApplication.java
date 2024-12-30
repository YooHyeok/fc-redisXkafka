package com.fc.moduleapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        /* 모든 모듈을 다 스캔하는 것은 시간도 오래걸리고 굉장히 비효율적이기 때문에, 빈으로 등록해야 되는 필요한 것들만 명시한다. */
        scanBasePackages = {"com.fc.moduleredis", "com.fc.modulekafka"}
)
public class ModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleApplication.class, args);
    }

}
