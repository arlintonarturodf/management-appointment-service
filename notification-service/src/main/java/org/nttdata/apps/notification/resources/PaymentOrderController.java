package org.nttdata.apps.notification.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.nttdata.apps.notification.entity.PaymentOrder;
import org.nttdata.apps.notification.repository.PaymentOrderRepository;
import org.nttdata.apps.notification.resources.dto.PaymentOrderResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@Path("/payment-orders")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentOrderController {

    @Inject
    PaymentOrderRepository paymentOrderRepository;

    // GET /api/v1/payment-orders
    @GET
    public Response getAll() {
        List<PaymentOrderResponse> orders = paymentOrderRepository.listAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return Response.ok(orders).build();
    }

    // GET /api/v1/payment-orders/{id}
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        PaymentOrder order = paymentOrderRepository.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Orden de pago no encontrada: " + id)
                    .build();
        }
        return Response.ok(toResponse(order)).build();
    }

    // GET /api/v1/payment-orders/appointment/{appointmentId}
    @GET
    @Path("/appointment/{appointmentId}")
    public Response getByAppointmentId(@PathParam("appointmentId") UUID appointmentId) {
        return paymentOrderRepository.findByAppointmentId(appointmentId)
                .map(order -> Response.ok(toResponse(order)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("No existe orden para la cita: " + appointmentId)
                        .build());
    }

    private PaymentOrderResponse toResponse(PaymentOrder order) {
        return new PaymentOrderResponse(
                order.getId(),
                order.getAppointmentId(),
                order.getPatientId(),
                order.getDoctorId(),
                order.getAppointmentDateTime(),
                order.getStatus(),
                order.getAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

}
