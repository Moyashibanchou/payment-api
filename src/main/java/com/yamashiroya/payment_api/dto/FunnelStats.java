package com.yamashiroya.payment_api.dto;

public class FunnelStats {

    private long cartAdds;

    private long checkoutStarts;

    private long checkoutCompletes;

    public long getCartAdds() {
        return cartAdds;
    }

    public void setCartAdds(long cartAdds) {
        this.cartAdds = cartAdds;
    }

    public long getCheckoutStarts() {
        return checkoutStarts;
    }

    public void setCheckoutStarts(long checkoutStarts) {
        this.checkoutStarts = checkoutStarts;
    }

    public long getCheckoutCompletes() {
        return checkoutCompletes;
    }

    public void setCheckoutCompletes(long checkoutCompletes) {
        this.checkoutCompletes = checkoutCompletes;
    }
}
