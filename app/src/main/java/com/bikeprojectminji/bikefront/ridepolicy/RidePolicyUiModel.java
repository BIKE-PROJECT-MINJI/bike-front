package com.bikeprojectminji.bikefront.ridepolicy;

public class RidePolicyUiModel {

    private final String stateLabel;
    private final String message;
    private final String detailCaption;
    private final boolean showBanner;
    private final String bannerMessage;
    private final int stateTextColorResId;
    private final int bannerBackgroundColorResId;
    private final int bannerTextColorResId;
    private final boolean completionEligible;
    private final String completionDialogMessage;

    public RidePolicyUiModel(
            String stateLabel,
            String message,
            String detailCaption,
            boolean showBanner,
            String bannerMessage,
            int stateTextColorResId,
            int bannerBackgroundColorResId,
            int bannerTextColorResId,
            boolean completionEligible,
            String completionDialogMessage
    ) {
        this.stateLabel = stateLabel;
        this.message = message;
        this.detailCaption = detailCaption;
        this.showBanner = showBanner;
        this.bannerMessage = bannerMessage;
        this.stateTextColorResId = stateTextColorResId;
        this.bannerBackgroundColorResId = bannerBackgroundColorResId;
        this.bannerTextColorResId = bannerTextColorResId;
        this.completionEligible = completionEligible;
        this.completionDialogMessage = completionDialogMessage;
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

    public String getDetailCaption() {
        return detailCaption;
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

    public boolean isCompletionEligible() {
        return completionEligible;
    }

    public String getCompletionDialogMessage() {
        return completionDialogMessage;
    }
}
