FROM golang:1.24-alpine AS build

WORKDIR /app

COPY go.mod .
RUN go mod download
COPY main.go .
RUN go build -o demo-service .

FROM alpine:3.21
WORKDIR /app
COPY --from=build /app/demo-service .
EXPOSE 8080
CMD ["./demo-service"]