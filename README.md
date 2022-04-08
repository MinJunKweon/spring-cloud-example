# Spring Cloud Example

```
microservices
├── product-composite-service
├── product-service
├── recommendation-service
└── review-service
```

## Getting Started

1. Build this whole project

```sh
$ cd spring-cloud-example
$ ./gradlew build -x test
```

2. Run Docker Compose

```sh
$ docker-compose build
$ docker-compose up -d
```

3. Open Web Browser & Connect swagger ui

```
http://localhost:8080/swagger-ui
```

4. Run create product API (Below sample payload)

```json
{
    "name": "title",
    "productId": 1,
    "recommendations": [
        {
            "author": "minz",
            "content": "It is recommendation!",
            "rate": 1,
            "recommendationId": 1
        }
    ],
    "reviews": [
        {
            "author": "minz",
            "content": "It sounds good",
            "reviewId": 1,
            "subject": "I highly recommend this product."
        }
    ],
    "weight": 0
}
```

5. Check product (productId: 1) & wait response
6. Check dynamic service addresses!

## Services

- `product-composite-service`
- `product-service`
- `recommendation-service`
- `review-service`
