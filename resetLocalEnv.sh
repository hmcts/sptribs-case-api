rm .aat-env
rm -rf ./build/*
docker kill $(docker ps -q)
docker system prune -af
