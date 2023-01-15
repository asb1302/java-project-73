setup:
	gradle wrapper --gradle-version 7.3

clean:
	./gradlew clean

build:
	./gradlew clean build

start: run-dev
run-dev:
	./gradlew bootRun --args='--spring.profiles.active=dev'

start-prod: run-prod
run-prod:
	./gradlew bootRun --args='--spring.profiles.active=prod'

install:
	./gradlew clean installDist

start-dist:
	./build/install/java-project-73/bin/java-project-73

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

generate-migrations:
	gradle diffChangeLog

db-migrate:
	./gradlew update
