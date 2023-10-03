run:
	docker build -f Dockerfile -t execution-engine-api .
	docker run -i --rm --net execution-engine-net -v /var/run/docker.sock:/var/run/docker.sock -p 8083:8083 execution-engine-api:latest
