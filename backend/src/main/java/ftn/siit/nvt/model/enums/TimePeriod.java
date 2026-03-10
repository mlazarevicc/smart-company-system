package ftn.siit.nvt.model.enums;

import java.time.LocalDateTime;

public enum TimePeriod {
    // Short periods (for availability analytics)
    LAST_1_HOUR,
    LAST_3_HOURS,
    LAST_12_HOURS,
    LAST_24_HOURS,

    // Medium periods (production analytics)
    LAST_WEEK,
    LAST_MONTH,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    LAST_YEAR,

    // Custom range (requires fromDate + toDate)
    CUSTOM;

    public LocalDateTime getStartDate() {
        LocalDateTime now = LocalDateTime.now();
        return switch (this) {
            case LAST_1_HOUR -> now.minusHours(1);
            case LAST_3_HOURS -> now.minusHours(3);
            case LAST_12_HOURS -> now.minusHours(12);
            case LAST_24_HOURS -> now.minusDays(1);
            case LAST_WEEK -> now.minusWeeks(1);
            case LAST_MONTH -> now.minusMonths(1);
            case LAST_3_MONTHS -> now.minusMonths(3);
            case LAST_6_MONTHS -> now.minusMonths(6);
            case LAST_YEAR -> now.minusYears(1);
            case CUSTOM -> throw new IllegalStateException(
                    "CUSTOM period requires explicit fromDate and toDate parameters"
            );
        };
    }

    public long getTotalDays() {
        return switch (this) {
            case LAST_1_HOUR -> 0; // < 1 day
            case LAST_3_HOURS -> 0;
            case LAST_12_HOURS -> 0;
            case LAST_24_HOURS -> 1;
            case LAST_WEEK -> 7;
            case LAST_MONTH -> 30;
            case LAST_3_MONTHS -> 90;
            case LAST_6_MONTHS -> 180;
            case LAST_YEAR -> 365;
            case CUSTOM -> -1; // unknown
        };
    }
}
