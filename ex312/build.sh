SKIP_LOCAL_IMAGE_BUILD="$1"
TODO_APP_IMAGE_TAG="$2"
TODO_BACKEND_IMAGE_TAG="$3"

if [[ -n "$TODO_APP_IMAGE_TAG" ]]
then
  echo "Build image tag received as input: $TODO_APP_IMAGE_TAG"
else
    TODO_APP_IMAGE_TAG="dilansachi/devopswk8s-ex312-todo-app:1.0.0"
    echo "Using default image tag: $TODO_APP_IMAGE_TAG"
fi

if [[ -n "$TODO_BACKEND_IMAGE_TAG" ]]
then
  echo "Build image tag received as input: $TODO_BACKEND_IMAGE_TAG"
else
    TODO_BACKEND_IMAGE_TAG="dilansachi/devopswk8s-ex312-todo-backend:1.0.0"
    echo "Using default image tag: $TODO_BACKEND_IMAGE_TAG"
fi

if [[ -n "$SKIP_LOCAL_IMAGE_BUILD" ]]
then
  echo "SKIP_LOCAL_IMAGE_BUILD: $SKIP_LOCAL_IMAGE_BUILD"
else
    SKIP_LOCAL_IMAGE_BUILD="0"
    echo "Building local images..."
fi

DIR=$(pwd)
cd ${DIR}/todo_app && ./gradlew clean build -Pimage_tag="$TODO_APP_IMAGE_TAG" -Pskip_local_image_build=$SKIP_LOCAL_IMAGE_BUILD
cd ${DIR}/todo_backend && ./gradlew clean build  -Pimage_tag="$TODO_BACKEND_IMAGE_TAG" -Pskip_local_image_build=$SKIP_LOCAL_IMAGE_BUILD
