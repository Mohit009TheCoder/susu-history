package com.example.susu;

import java.math.BigDecimal;
import java.util.*;

/** In-memory manager for one rotating savings (susu) group. */
public final class SusuGroup {
    private final BigDecimal weeklyContribution;
    private final List<String> members = new ArrayList<>();
    private final Map<Integer, Map<String, BigDecimal>> paymentsByWeek = new HashMap<>();
    private final List<Payout> payoutHistory = new ArrayList<>();
    private int currentWeek = 1;

    public SusuGroup(BigDecimal weeklyContribution) {
        this.weeklyContribution = requirePositiveMoney(weeklyContribution, "Weekly contribution");
        paymentsByWeek.put(currentWeek, new LinkedHashMap<>());
    }

    public BigDecimal getWeeklyContribution() { return weeklyContribution; }
    public int getCurrentWeek() { return currentWeek; }
    public List<String> getMembers() { return Collections.unmodifiableList(members); }
    public List<Payout> getPayoutHistory() { return Collections.unmodifiableList(payoutHistory); }

    public void addMember(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Member name/ID cannot be blank.");
        String normalized = name.trim();
        if (members.stream().anyMatch(m -> m.equalsIgnoreCase(normalized))) throw new IllegalArgumentException("Duplicate member: " + normalized);
        if (!paymentsByWeek.get(currentWeek).isEmpty() || currentWeek > 1) throw new IllegalStateException("Members can only be added before any payment is recorded in week 1.");
        members.add(normalized);
    }

    public BigDecimal amountPaid(String member, int week) {
        requireMember(member);
        requireKnownWeek(week);
        return paymentsByWeek.get(week).getOrDefault(findMember(member), BigDecimal.ZERO).setScale(2);
    }

    public BigDecimal amountOwed(String member) {
        requireMember(member);
        return weeklyContribution.subtract(amountPaid(member, currentWeek)).setScale(2);
    }

    public void recordContribution(String member, BigDecimal amount) {
        String canonical = findMember(requireMember(member));
        BigDecimal value = requirePositiveMoney(amount, "Payment");
        BigDecimal owed = amountOwed(canonical);
        if (value.compareTo(owed) > 0) throw new IllegalArgumentException("Payment exceeds amount owed (" + money(owed) + ").");
        Map<String, BigDecimal> weekPayments = paymentsByWeek.get(currentWeek);
        weekPayments.put(canonical, weekPayments.getOrDefault(canonical, BigDecimal.ZERO).add(value).setScale(2));
    }

    /** Closes the current week only when all members have paid in full. */
    public CloseWeekResult closeWeek() {
        if (members.isEmpty()) throw new IllegalStateException("Add at least one member before closing a week.");
        Map<String, BigDecimal> outstanding = new LinkedHashMap<>();
        for (String member : members) {
            BigDecimal owed = amountOwed(member);
            if (owed.compareTo(BigDecimal.ZERO) > 0) outstanding.put(member, owed);
        }
        if (!outstanding.isEmpty()) return CloseWeekResult.incomplete(currentWeek, outstanding);

        String recipient = members.get((currentWeek - 1) % members.size());
        BigDecimal pot = weeklyContribution.multiply(BigDecimal.valueOf(members.size())).setScale(2);
        payoutHistory.add(new Payout(currentWeek, recipient, pot));
        int closedWeek = currentWeek;
        currentWeek++;
        paymentsByWeek.put(currentWeek, new LinkedHashMap<>());
        return CloseWeekResult.closed(closedWeek, recipient, pot);
    }

    public Report report() {
        Map<String, BigDecimal> paid = new LinkedHashMap<>(), owed = new LinkedHashMap<>();
        for (String member : members) {
            BigDecimal amount = amountPaid(member, currentWeek);
            paid.put(member, amount);
            owed.put(member, weeklyContribution.subtract(amount).setScale(2));
        }
        return new Report(currentWeek, weeklyContribution, paid, owed, new ArrayList<>(payoutHistory));
    }

    private String requireMember(String name) {
        if (name == null) throw new IllegalArgumentException("Unknown member: null");
        return members.stream().filter(m -> m.equalsIgnoreCase(name.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown member: " + name));
    }
    private String findMember(String name) { return requireMember(name); }
    private void requireKnownWeek(int week) {
        if (week < 1 || week > currentWeek) throw new IllegalArgumentException("Unknown week: " + week);
    }
    private static BigDecimal requirePositiveMoney(BigDecimal amount, String label) {
        if (amount == null || amount.scale() > 2 || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(label + " must be greater than zero and have at most two decimal places.");
        return amount.setScale(2);
    }
    public static String money(BigDecimal value) { return value.setScale(2).toPlainString(); }

    public record Payout(int week, String recipient, BigDecimal amount) { }
    public record CloseWeekResult(boolean closed, int week, String recipient, BigDecimal pot, Map<String, BigDecimal> outstanding) {
        static CloseWeekResult incomplete(int week, Map<String, BigDecimal> outstanding) { return new CloseWeekResult(false, week, null, BigDecimal.ZERO.setScale(2), Collections.unmodifiableMap(new LinkedHashMap<>(outstanding))); }
        static CloseWeekResult closed(int week, String recipient, BigDecimal pot) { return new CloseWeekResult(true, week, recipient, pot, Map.of()); }
    }
    public record Report(int currentWeek, BigDecimal weeklyContribution, Map<String, BigDecimal> paid, Map<String, BigDecimal> owed, List<Payout> payoutHistory) { }
}
