# 1. 경량 JRE 베이스 이미지 (프로젝트 Java 버전에 맞게 17 또는 21 지정)
FROM eclipse-temurin:21-jre-alpine

# 2. 작업 디렉터리 생성
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너 내부로 복사
# Gradle의 경우: build/libs/*.jar
# Maven의 경우: target/*.jar
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 4. 기초환경변수 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 5. 컨테이너 내부 노출 포트 (환경설정에 적혀져 있는 포트를 맞춤)
EXPOSE 5001

# 6. 실행 명령어: prod 프로필 활성화 및 JVM 메모리 제어
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]