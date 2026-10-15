package com.example.susu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SusuGroupTest {
    private SusuGroup group;
    private BigDecimal money(String value) { return new BigDecimal(value); }
    @BeforeEach void setup() { group = new SusuGroup(money("50.00")); group.addMember("Ama"); group.addMember("Kofi"); group.addMember("Esi"); }

    @Test void partialPaymentLeavesCorrectBalanceAndWeekCannotClose() {
        group.recordContribution("Ama", money("50.00")); group.recordContribution("Kofi", money("50.00")); group.recordContribution("Esi", money("30.00"));
        SusuGroup.CloseWeekResult result = group.closeWeek();
        assertFalse(result.closed()); assertEquals(money("20.00"), result.outstanding().get("Esi"));
        assertEquals(1, group.getCurrentWeek()); assertTrue(group.getPayoutHistory().isEmpty());
    }
    @Test void completingPartialPaymentClosesWeekAndPaysFirstMember() {
        group.recordContribution("Ama", money("50.00")); group.recordContribution("Kofi", money("50.00")); group.recordContribution("Esi", money("30.00"));
        group.closeWeek(); group.recordContribution("Esi", money("20.00"));
        var result = group.closeWeek();
        assertTrue(result.closed()); assertEquals("Ama", result.recipient()); assertEquals(money("150.00"), result.pot()); assertEquals(2, group.getCurrentWeek());
    }
    @Test void nextWeekPaysNextMemberInJoinOrder() {
        for (String member : group.getMembers()) group.recordContribution(member, money("50.00")); group.closeWeek();
        for (String member : group.getMembers()) group.recordContribution(member, money("50.00"));
        assertEquals("Kofi", group.closeWeek().recipient());
    }
    @Test void rejectsDuplicateMemberIgnoringCase() { assertThrows(IllegalArgumentException.class, () -> group.addMember("ama")); }
    @Test void rejectsUnknownMember() { assertThrows(IllegalArgumentException.class, () -> group.recordContribution("Kojo", money("10.00"))); }
    @Test void rejectsZeroAndNegativePayments() { assertThrows(IllegalArgumentException.class, () -> group.recordContribution("Ama", money("0.00"))); assertThrows(IllegalArgumentException.class, () -> group.recordContribution("Ama", money("-1.00"))); }
    @Test void rejectsPaymentAboveBalance() { group.recordContribution("Ama", money("40.00")); assertThrows(IllegalArgumentException.class, () -> group.recordContribution("Ama", money("10.01"))); }
    @Test void moneyRequiresAtMostTwoDecimalPlaces() { assertThrows(IllegalArgumentException.class, () -> group.recordContribution("Ama", money("1.001"))); }
    @Test void doesNotAllowAddingMembersAfterPaymentsStart() { group.recordContribution("Ama", money("5.00")); assertThrows(IllegalStateException.class, () -> group.addMember("Kojo")); }
    @Test void reportShowsPaidAndOwedAndHistory() { group.recordContribution("Ama", money("50.00")); assertEquals(money("50.00"), group.report().paid().get("Ama")); assertEquals(money("50.00"), group.report().owed().get("Kofi")); }
}
