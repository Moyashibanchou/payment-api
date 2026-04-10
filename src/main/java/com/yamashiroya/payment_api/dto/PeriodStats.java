package com.yamashiroya.payment_api.dto;

import java.util.Map;

public class PeriodStats {

    private String from;

    private String to;

    private long totalRevenue;

    private long totalOrders;

    private long totalSessions;

    private double purchaseRate;

    private FunnelStats funnel;

    private Map<String, Long> channels;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public FunnelStats getFunnel() {
        return funnel;
    }

    public void setFunnel(FunnelStats funnel) {
        this.funnel = funnel;
    }

    public Map<String, Long> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, Long> channels) {
        this.channels = channels;
    }
}
