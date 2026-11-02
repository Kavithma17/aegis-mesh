package com.aegismesh.stock.messaging;

public final class KafkaTopics {

    public static final String REQUEST_CREATED =
            "aegis.medicine-request.created";

    public static final String REQUEST_CANCELLED =
            "aegis.medicine-request.cancelled";

    public static final String RESERVATION_RESULT =
            "aegis.stock.reservation-result";

    private KafkaTopics() {
    }
}
