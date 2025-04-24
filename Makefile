build:
	@mvn -B --no-transfer-progress clean package

docker-build:
	@docker build -t k15g/service-xmldisg:dev .

docker-run:
	@docker run -d -p 8080:8080 k15g/service-xmldsig:dev