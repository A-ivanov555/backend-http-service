
#!/usr/bin/env bash

set -u

BASE_URL="http://localhost:8080"
RESPONSE_FILE=$(mktemp)

PASSED=0
FAILED=0

trap 'rm -f "$RESPONSE_FILE"' EXIT

# Отправляет HTTP-запрос и проверяет статус ответа.
check() {
    local expected="$1"
    local method="$2"
    local path="$3"
    local actual

    : > "$RESPONSE_FILE"

    if [ "$#" -ge 4 ]; then
        actual=$(curl -sS \
            -o "$RESPONSE_FILE" \
            -w "%{http_code}" \
            -X "$method" \
            "$BASE_URL$path" \
            -H "Content-Type: application/json" \
            --data-raw "$4")
    else
        actual=$(curl -sS \
            -o "$RESPONSE_FILE" \
            -w "%{http_code}" \
            -X "$method" \
            "$BASE_URL$path")
    fi

    if [ "$actual" = "$expected" ]; then
        echo "PASS: $method $path -> $actual"
        PASSED=$((PASSED + 1))
    else
        echo "FAIL: $method $path -> $actual (expected $expected)"
        FAILED=$((FAILED + 1))
    fi

    if [ -s "$RESPONSE_FILE" ]; then
        cat "$RESPONSE_FILE"
        echo
    fi

    echo
}

# Проверяет наличие значения в последнем HTTP-ответе.
expect_body() {
    local expected="$1"

    if grep -Fq "$expected" "$RESPONSE_FILE"; then
        echo "PASS: response contains $expected"
        PASSED=$((PASSED + 1))
    else
        echo "FAIL: response does not contain $expected"
        FAILED=$((FAILED + 1))
    fi

    echo
}

echo "=== PING ==="

check 200 GET "/ping"


echo "=== EMPTY COLLECTIONS ==="

check 200 GET "/customers"
check 200 GET "/vehicles"


echo "=== CREATE CUSTOMERS ==="

check 201 POST "/customers" \
    '{"id":1,"name":"Andrey","age":19}'

check 201 POST "/customers" \
    '{"id":2,"name":"Alex","age":16}'

check 201 POST "/customers" \
    '{"id":3,"name":"Max","age":18}'


echo "=== CREATE VEHICLES ==="

check 201 POST "/vehicles" \
    '{"id":1,"type":"car","model":"BMW","pricePerHour":500}'

check 201 POST "/vehicles" \
    '{"id":2,"type":"scooter","model":"Ninebot","pricePerHour":200}'

check 201 POST "/vehicles" \
    '{"id":3,"type":"car","model":"Toyota","pricePerHour":400}'


echo "=== GET RESOURCES ==="

check 200 GET "/customers"
check 200 GET "/customers/1"
check 200 GET "/vehicles"
check 200 GET "/vehicles/1"


echo "=== UPDATE CUSTOMER ==="

check 200 PUT "/customers/1" \
    '{"name":"Andrey Ivanov","age":19}'

check 200 GET "/customers/1"
expect_body '"name":"Andrey Ivanov"'


echo "=== UPDATE VEHICLE ==="

check 200 PUT "/vehicles/1" \
    '{"model":"BMW M3","pricePerHour":550}'

check 200 GET "/vehicles/1"
expect_body '"model":"BMW M3"'


echo "=== FILTER VEHICLES ==="

check 200 GET "/vehicles?available=true"
check 200 GET "/vehicles?type=car"
check 200 GET "/vehicles?type=scooter&available=true"
check 200 GET "/vehicles?available=false"


echo "=== RENTAL PRICE CALCULATION ==="

check 200 GET "/vehicles/1/price?hours=2"
expect_body '"totalPrice":1100.0'

check 200 GET "/vehicles/2/price?hours=2"
expect_body '"totalPrice":500.0'


echo "=== AGE RESTRICTIONS ==="

# A 16-year-old customer cannot rent a car.
check 409 POST "/vehicles/3/rent" \
    '{"customerId":2}'

# A customer aged exactly 18 can rent a car.
check 200 POST "/vehicles/3/rent" \
    '{"customerId":3}'

check 200 POST "/vehicles/3/return"


echo "=== RENT SCOOTER ==="

# A customer aged exactly 16 can rent a scooter.
check 200 POST "/vehicles/2/rent" \
    '{"customerId":2}'

check 200 GET "/vehicles/2"
expect_body '"available":false'


echo "=== PROTECT ACTIVE RENTAL ==="

check 409 DELETE "/customers/2"

# The customer must not become too young for an active rental.
check 409 PUT "/customers/2" \
    '{"name":"Alex","age":15}'

check 200 GET "/customers/2"
expect_body '"age":16'


echo "=== RENT CAR ==="

check 200 POST "/vehicles/1/rent" \
    '{"customerId":1}'

check 200 GET "/vehicles/1"
expect_body '"available":false'


echo "=== ERROR: VEHICLE ALREADY RENTED ==="

check 409 POST "/vehicles/1/rent" \
    '{"customerId":3}'


echo "=== ERROR: DELETE RENTED VEHICLE ==="

check 409 DELETE "/vehicles/1"


echo "=== ERROR: INVALID AGE CHANGE ==="

check 409 PUT "/customers/1" \
    '{"name":"Andrey","age":15}'

check 200 GET "/customers/1"
expect_body '"age":19'


echo "=== FILTER AFTER RENT ==="

check 200 GET "/vehicles?available=true"
check 200 GET "/vehicles?available=false"


echo "=== RETURN VEHICLES ==="

check 200 POST "/vehicles/1/return"
check 200 POST "/vehicles/2/return"


echo "=== ERROR: RETURN AVAILABLE VEHICLE ==="

check 409 POST "/vehicles/1/return"


echo "=== ERROR: DUPLICATE RESOURCES ==="

check 409 POST "/customers" \
    '{"id":1,"name":"Duplicate","age":20}'

check 409 POST "/vehicles" \
    '{"id":1,"type":"car","model":"Duplicate","pricePerHour":100}'


echo "=== ERROR: RESOURCE NOT FOUND ==="

check 404 GET "/vehicles/999"
check 404 GET "/customers/999"
check 404 DELETE "/vehicles/999"
check 404 DELETE "/customers/999"


echo "=== ERROR: INVALID ID ==="

check 400 GET "/vehicles/abc"
check 400 GET "/customers/abc"
check 400 GET "/vehicles/0"


echo "=== ERROR: INVALID FILTERS ==="

check 400 GET "/vehicles?type=plane"
check 400 GET "/vehicles?available=maybe"


echo "=== ERROR: INVALID RENTAL DURATION ==="

check 400 GET "/vehicles/1/price?hours=0"
check 400 GET "/vehicles/1/price?hours=-2"
check 400 GET "/vehicles/1/price?hours=abc"
check 400 GET "/vehicles/1/price"


echo "=== ERROR: MALFORMED JSON ==="

check 400 POST "/customers" \
    '{"id":4,'


echo "=== ERROR: JSON NULL ==="

check 400 POST "/customers" 'null'
check 400 POST "/vehicles" 'null'
check 400 POST "/vehicles/1/rent" 'null'


echo "=== ERROR: MISSING REQUIRED FIELDS ==="

check 400 POST "/customers" \
    '{"id":4,"name":"Ivan"}'

check 400 POST "/vehicles" \
    '{"id":4,"type":"car","model":"Audi"}'

check 400 POST "/vehicles/1/rent" \
    '{}'


echo "=== ERROR: INVALID FIELD VALUES ==="

check 400 POST "/customers" \
    '{"id":4,"name":"","age":20}'

check 400 POST "/customers" \
    '{"id":4,"name":"Ivan","age":-1}'

check 400 POST "/vehicles" \
    '{"id":4,"type":"plane","model":"Boeing","pricePerHour":500}'

check 400 POST "/vehicles" \
    '{"id":4,"type":"car","model":"Audi","pricePerHour":0}'


echo "=== ERROR: UNSUPPORTED HTTP METHOD ==="

check 405 PATCH "/vehicles"


echo "=== ERROR: UNKNOWN ROUTE ==="

check 404 GET "/unknown"


echo "=== DELETE RESOURCES ==="

check 204 DELETE "/vehicles/1"
check 204 DELETE "/vehicles/2"
check 204 DELETE "/vehicles/3"

check 204 DELETE "/customers/1"
check 204 DELETE "/customers/2"
check 204 DELETE "/customers/3"


echo "=== FINAL STATE ==="

check 200 GET "/vehicles"
expect_body '[]'

check 200 GET "/customers"
expect_body '[]'


echo "=================================="
echo "PASSED: $PASSED"
echo "FAILED: $FAILED"
echo "=================================="

if [ "$FAILED" -ne 0 ]; then
    exit 1
fi

exit 0
