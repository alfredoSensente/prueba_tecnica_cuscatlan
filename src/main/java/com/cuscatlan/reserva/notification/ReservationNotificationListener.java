package com.cuscatlan.reserva.notification;

import com.cuscatlan.reserva.event.ReservationConfirmationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReservationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotificationListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationConfirmation(ReservationConfirmationEvent event) {
        log.info("[notificación simulada] correo enviado a {} — reserva {} quedó en estado {}",
                event.userEmail(), event.reservationId(), event.status());
    }
}
