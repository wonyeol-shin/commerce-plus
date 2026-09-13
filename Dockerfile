# 1단계: 빌드 환경 (Gradle 8.x + JDK 21)
FROM gradle:8.5-jdk21 AS build
# 도커 /app 디렉터리 생성
WORKDIR /app
# 소스 코드(static 포함)를 /app에 복사
COPY . .
# 도커환경에서 스프링 부트 JAR 빌드 (테스트는 제외하여 빌드 속도 향상)
RUN ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 환경 (JRE 21)
FROM amazoncorretto:21-al2023-headless
WORKDIR /app

# 1단계 빌드 결과물인 jar 파일을 app.jar라는 이름으로 복사
COPY --from=build /app/build/libs/*.jar app.jar
# 포트 문서화
EXPOSE 8080
# jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
