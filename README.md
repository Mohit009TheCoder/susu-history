# Susu Group Manager (Java)

A small command-line program for managing one rotating savings group. It uses Java `BigDecimal` for exact money calculations and JUnit 5 for automated tests.

## Requirements
- Java 17 or newer
- Maven 3.8+

## Run
```bash
mvn test
mvn package
java -cp target/classes com.example.susu.Main
```
Follow the menu to add members, record weekly contributions, close a week, and view the report. Amounts are entered in GHS (for example, `50.00`).

## Tests
Run `mvn test`. The automated suite covers partial payments, blocked week closure, successful payout, payout order, duplicate/unknown members, invalid amounts, overpayment, membership timing, and reporting.
# susu-group-with-history
