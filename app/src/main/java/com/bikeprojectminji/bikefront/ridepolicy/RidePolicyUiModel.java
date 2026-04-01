package com.bikeprojectminji.bikefront.ridepolicy;

public class RidePolicyUiModel {

    private final String stateLabel;
    private final String message;
    private final boolean showBanner;
    private final String bannerMessage;
    private final int stateTextColorResId;
    private final int bannerBackgroundColorResId;
    private final int bannerTextColorResId;

    public RidePolicyUiModel(
            String stateLabel,
            String message,
            boolean showBanner,
            String bannerMessage,
            int stateTextColorResId,
            int bannerBackgroundColorResId,
            int bannerTextColorResId
    ) {
        this.stateLabel = stateLabel;
        this.message = message;
        this.showBanner = showBanner;
        this.bannerMessage = bannerMessage;
        this.stateTextColorResId = stateTextColorResId;
        this.bannerBackgroundColorResId = bannerBackgroundColorResId;
        this.bannerTextColorResId = bannerTextColorResId;
    }

    public String getStateLabel() {
        return stateLabel;
    }

    public String getMessage() {
        return message;
    }

    public boolean isShowBanner() {
        return showBanner;
    }

    public String getBannerMessage() {
        return bannerMessage;
    }

    public int getStateTextColorResId() {
        return stateTextColorResId;
    }

    public int getBannerBackgroundColorResId() {
        return bannerBackgroundColorResId;
    }

    public int getBannerTextColorResId() {
        return bannerTextColorResId;
    }
}
