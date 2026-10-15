package com.example.susu;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Scanner;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Susu Group Manager ===");
        SusuGroup group;
        while (true) {
            try { System.out.print("Weekly contribution in GHS (e.g. 50.00): "); group = new SusuGroup(new BigDecimal(scanner.nextLine().trim())); break; }
            catch (RuntimeException e) { System.out.println("Invalid amount: " + e.getMessage()); }
        }
        boolean running = true;
        while (running) {
            System.out.println("\nWeek " + group.getCurrentWeek() + " | 1 Add member | 2 Record payment | 3 Close week | 4 Report | 0 Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> { System.out.print("Member name/ID: "); group.addMember(scanner.nextLine()); System.out.println("Member added."); }
                    case "2" -> {
                        System.out.print("Member name/ID: "); String member = scanner.nextLine();
                        System.out.print("Amount paid in GHS: "); BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
                        group.recordContribution(member, amount); System.out.println("Payment recorded.");
                    }
                    case "3" -> {
                        SusuGroup.CloseWeekResult result = group.closeWeek();
                        if (result.closed()) System.out.println("Week " + result.week() + " closed. " + result.recipient() + " receives GHS " + SusuGroup.money(result.pot()) + ".");
                        else { System.out.println("Week " + result.week() + " cannot close. Outstanding amounts:"); for (Map.Entry<String, BigDecimal> entry : result.outstanding().entrySet()) System.out.println(" - " + entry.getKey() + " owes GHS " + SusuGroup.money(entry.getValue())); }
                    }
                    case "4" -> printReport(group);
                    case "0" -> running = false;
                    default -> System.out.println("Choose one of the listed options.");
                }
            } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        }
        System.out.println("Goodbye.");
    }
    private static void printReport(SusuGroup group) {
        SusuGroup.Report report = group.report();
        System.out.println("\n--- Susu report ---\nCurrent week: " + report.currentWeek() + "\nWeekly contribution: GHS " + SusuGroup.money(report.weeklyContribution()));
        System.out.println("Payments and balances:");
        for (String member : report.paid().keySet()) {
            String status = report.owed().get(member).signum() == 0 ? "PAID IN FULL" : "owes GHS " + SusuGroup.money(report.owed().get(member));
            System.out.println(" - " + member + ": paid GHS " + SusuGroup.money(report.paid().get(member)) + ", " + status);
        }
        System.out.println("Payout history:");
        if (report.payoutHistory().isEmpty()) System.out.println(" - No payouts yet.");
        else for (SusuGroup.Payout payout : report.payoutHistory()) System.out.println(" - Week " + payout.week() + ": " + payout.recipient() + " received GHS " + SusuGroup.money(payout.amount()));
    }
}
