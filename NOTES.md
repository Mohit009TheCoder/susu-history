# Assumptions and decisions

- The group has one fixed positive weekly contribution, expressed in GHS with no more than two decimal places.
- Member names/IDs are trimmed and compared case-insensitively, so `Ama` and `ama` are duplicates.
- Members can be added only before the first contribution of week 1. Membership is frozen after the first payment to avoid changing the pot size or payout order mid-cycle.
- Members pay in the current week only. Partial payments accumulate; a member cannot pay more than their remaining balance.
- A week closes only when every member has paid the full contribution. If not, the week remains open and the report lists every outstanding balance.
- Payout order follows join order and starts with the first member in week 1. The pot is contribution amount multiplied by the number of members.
- After a successful close, the next week starts with all balances reset to zero. Payout history is retained in memory for the life of the process.
- Data is in-memory and is lost when the application exits; persistence, authentication, and concurrent access are out of scope for this small exercise.

## Improvements with more time
- Persist groups, payments, and payout history to a database or file.
- Add stronger input parsing and a non-interactive command/API mode.
- Add integration tests for the CLI and property-based tests for money/payment invariants.
- Add audit events and safeguards for corrections or reversed payments.
