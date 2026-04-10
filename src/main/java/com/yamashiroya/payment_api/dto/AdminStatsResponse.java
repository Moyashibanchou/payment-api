package com.yamashiroya.payment_api.dto;

public class AdminStatsResponse {

    private PeriodStats current;

    private PeriodStats previous;

    public PeriodStats getCurrent() {
        return current;
    }

    public void setCurrent(PeriodStats current) {
        this.current = current;
    }

    public PeriodStats getPrevious() {
        return previous;
    }

    public void setPrevious(PeriodStats previous) {
        this.previous = previous;
    }
}
