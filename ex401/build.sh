SKIP_LOCAL_IMAGE_BUILD="$1"
LOG_READER_IMAGE_TAG="$2"
PING_PONG_APP_IMAGE_TAG="$3"

if [[ -n "$LOG_READER_IMAGE_TAG" ]]
then
  echo "Build image tag received as input: $LOG_READER_IMAGE_TAG"
else
    LOG_READER_IMAGE_TAG="dilansachi/devopswk8s-ex401-log-reader:1.0.0"
    echo "Using default image tag: $LOG_READER_IMAGE_TAG"
fi

if [[ -n "$PING_PONG_APP_IMAGE_TAG" ]]
then
  echo "Build image tag received as input: $PING_PONG_APP_IMAGE_TAG"
else
    PING_PONG_APP_IMAGE_TAG="dilansachi/devopswk8s-ex401-ping-pong-application:1.0.0"
    echo "Using default image tag: $PING_PONG_APP_IMAGE_TAG"
fi

if [[ -n "$SKIP_LOCAL_IMAGE_BUILD" ]]
then
  echo "SKIP_LOCAL_IMAGE_BUILD: $SKIP_LOCAL_IMAGE_BUILD"
else
    SKIP_LOCAL_IMAGE_BUILD="0"
    echo "Building local images..."
fi

DIR=$(pwd)
cd ${DIR}/log_reader && ./gradlew clean build -Pimage_tag="$LOG_READER_IMAGE_TAG" -Pskip_local_image_build=$SKIP_LOCAL_IMAGE_BUILD
cd ${DIR}/ping_pong_application && ./gradlew clean build  -Pimage_tag="$PING_PONG_APP_IMAGE_TAG" -Pskip_local_image_build=$SKIP_LOCAL_IMAGE_BUILD
