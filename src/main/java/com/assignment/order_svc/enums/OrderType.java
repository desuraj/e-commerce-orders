package com.assignment.order_svc.enums;

/**
 * Represents the type of order.
 * STANDARD - regular order with normal delivery SLA.
 * RUSH     - express order with 2-hour delivery SLA, subject to eligibility.
 */
public enum OrderType {
    STANDARD,
    RUSH
}
