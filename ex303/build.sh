DIR=$(pwd)
cd ${DIR}/log_writer && ./gradlew clean build
cd ${DIR}/log_reader && ./gradlew clean build
cd ${DIR}/ping_pong_application && ./gradlew clean build
