package com.vlad.eventhub.exception;

/** Кидається, коли вільних місць недостатньо АБО коли конкурентне бронювання програло гонку (optimistic lock). */
public class SoldOutException extends RuntimeException {
    public SoldOutException(String message) { super(message); }
}
