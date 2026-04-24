package org.nttdata.apps.notification.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.nttdata.apps.notification.entity.PaymentOrder;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PaymentOrderRepository implements PanacheRepositoryBase<PaymentOrder, UUID> {

    public Optional<PaymentOrder> findByAppointmentId(UUID appointmentId) {
        return find("appointmentId", appointmentId).firstResultOptional();
    }
}
