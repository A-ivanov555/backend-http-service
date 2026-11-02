#!/usr/bin/env bash

BASE_URL="http://localhost:8080"

echo
echo "=== PING ==="
curl -i "$BASE_URL/ping"


echo
echo
echo "=== CREATE CUSTOMERS ==="

curl -i -X POST "$BASE_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Andrey","age":19}'

curl -i -X POST "$BASE_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{"id":2,"name":"Alex","age":16}'


echo
echo
echo "=== CREATE VEHICLES ==="

curl -i -X POST "$BASE_URL/vehicles" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"type":"car","model":"BMW","pricePerHour":500}'

curl -i -X POST "$BASE_URL/vehicles" \
  -H "Content-Type: application/json" \
  -d '{"id":2,"type":"scooter","model":"Ninebot","pricePerHour":200}'

curl -i -X POST "$BASE_URL/vehicles" \
  -H "Content-Type: application/json" \
  -d '{"id":3,"type":"car","model":"Toyota","pricePerHour":400}'


echo
echo
echo "=== GET CUSTOMERS ==="

curl -i "$BASE_URL/customers"

curl -i "$BASE_URL/customers/1"


echo
echo
echo "=== GET VEHICLES ==="

curl -i "$BASE_URL/vehicles"

curl -i "$BASE_URL/vehicles/1"


echo
echo
echo "=== UPDATE CUSTOMER ==="

curl -i -X PUT "$BASE_URL/customers/1" \
  -H "Content-Type: application/json" \
  -d '{"name":"Andrey Ivanov","age":19}'


echo
echo
echo "=== UPDATE VEHICLE ==="

curl -i -X PUT "$BASE_URL/vehicles/1" \
  -H "Content-Type: application/json" \
  -d '{"model":"BMW M3","pricePerHour":550}'


echo
echo
echo "=== FILTER VEHICLES ==="

curl -i "$BASE_URL/vehicles?available=true"

curl -i "$BASE_URL/vehicles?type=car"

curl -i "$BASE_URL/vehicles?type=scooter&available=true"


echo
echo
echo "=== ERROR: CUSTOMER TOO YOUNG FOR CAR ==="

curl -i -X POST "$BASE_URL/vehicles/3/rent" \
  -H "Content-Type: application/json" \
  -d '{"customerId":2}'


echo
echo
echo "=== RENT SCOOTER ==="

curl -i -X POST "$BASE_URL/vehicles/2/rent" \
  -H "Content-Type: application/json" \
  -d '{"customerId":2}'


echo
echo
echo "=== ERROR: DELETE CUSTOMER WITH ACTIVE RENT ==="

curl -i -X DELETE "$BASE_URL/customers/2"


echo
echo
echo "=== RENT CAR ==="

curl -i -X POST "$BASE_URL/vehicles/1/rent" \
  -H "Content-Type: application/json" \
  -d '{"customerId":1}'


echo
echo
echo "=== ERROR: VEHICLE ALREADY RENTED ==="

curl -i -X POST "$BASE_URL/vehicles/1/rent" \
  -H "Content-Type: application/json" \
  -d '{"customerId":2}'


echo
echo
echo "=== FILTER AVAILABLE AFTER RENT ==="

curl -i "$BASE_URL/vehicles?available=true"


echo
echo
echo "=== RETURN VEHICLES ==="

curl -i -X POST "$BASE_URL/vehicles/1/return"

curl -i -X POST "$BASE_URL/vehicles/2/return"


echo
echo
echo "=== ERROR: RETURN ALREADY AVAILABLE VEHICLE ==="

curl -i -X POST "$BASE_URL/vehicles/1/return"


echo
echo
echo "=== ERROR: DUPLICATE CUSTOMER ==="

curl -i -X POST "$BASE_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Duplicate","age":20}'


echo
echo
echo "=== ERROR: DUPLICATE VEHICLE ==="

curl -i -X POST "$BASE_URL/vehicles" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"type":"car","model":"Duplicate","pricePerHour":100}'


echo
echo
echo "=== ERROR: RESOURCE NOT FOUND ==="

curl -i "$BASE_URL/vehicles/999"

curl -i "$BASE_URL/customers/999"


echo
echo
echo "=== ERROR: INVALID ID ==="

curl -i "$BASE_URL/vehicles/abc"


echo
echo
echo "=== ERROR: INVALID JSON ==="

curl -i -X POST "$BASE_URL/customers" \
  -H "Content-Type: application/json" \
  -d '{"id":4,'


echo
echo
echo "=== ERROR: UNSUPPORTED HTTP METHOD ==="

curl -i -X PATCH "$BASE_URL/vehicles"


echo
echo
echo "=== DELETE RESOURCES ==="

curl -i -X DELETE "$BASE_URL/vehicles/1"

curl -i -X DELETE "$BASE_URL/vehicles/2"

curl -i -X DELETE "$BASE_URL/vehicles/3"

curl -i -X DELETE "$BASE_URL/customers/1"

curl -i -X DELETE "$BASE_URL/customers/2"