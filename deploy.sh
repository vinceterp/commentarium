export $(cat .env | xargs) &&
docker build --platform=linux/amd64 -t commentarium-backend . && 
docker tag commentarium-backend vincentvanity/commentarium-backend:latest && 
docker push vincentvanity/commentarium-backend:latest