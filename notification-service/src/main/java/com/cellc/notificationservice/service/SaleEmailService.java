package com.cellc.notificationservice.service;

import com.cellc.notificationservice.messaging.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleEmailService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;

    @Value("${app.mail.sales.enabled:false}")
    private boolean salesMailEnabled;

    @Value("${app.mail.sales.from:no-reply@cellc.local}")
    private String fromAddress;

    public void sendSaleDetails(OrderPaidEvent event) {
        if (!salesMailEnabled) {
            log.debug("Envio de correo deshabilitado para la orden {}", event.orderId());
            return;
        }
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("No se enviara correo para la orden {} porque el evento no trae email", event.orderId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(event.customerEmail());
            message.setSubject("Detalle de tu compra #" + event.orderId());
            message.setText(buildBody(event));
            mailSender.send(message);
            log.info("Correo de venta enviado para orderId={} a {}", event.orderId(), event.customerEmail());
        } catch (Exception ex) {
            log.warn("Fallo el envio del correo para la orden {}: {}", event.orderId(), ex.getMessage());
        }
    }

    private String buildBody(OrderPaidEvent event) {
        StringBuilder body = new StringBuilder();
        body.append("Hola ");
        body.append(event.customerName() == null || event.customerName().isBlank() ? "cliente" : event.customerName());
        body.append(",\n\n");
        body.append("Tu compra fue registrada correctamente.\n\n");
        body.append("Pedido: #").append(event.orderId()).append('\n');
        body.append("Fecha: ").append(DATE_FORMATTER.format(resolveEventInstant(event))).append('\n');
        body.append("Metodo de pago: ").append(event.paymentMethod()).append('\n');
        body.append("Estado del pago: ").append(event.paymentStatus()).append('\n');
        body.append("Estado de la orden: ").append(event.orderStatus()).append('\n');
        body.append("Subtotal: ").append(event.itemsTotalAmount()).append('\n');
        body.append("Descuento: ").append(event.discountAmount()).append('\n');
        body.append("Total: ").append(event.totalAmount()).append('\n');
        if (event.promotionCode() != null && !event.promotionCode().isBlank()) {
            body.append("Promocion aplicada: ").append(event.promotionCode()).append('\n');
        }
        body.append("\nItems:\n");
        if (event.items() == null || event.items().isEmpty()) {
            body.append("- Sin items registrados\n");
        } else {
            for (OrderPaidEvent.OrderPaidItem item : event.items()) {
                body.append("- Producto ")
                        .append(item.productId())
                        .append(": cantidad ")
                        .append(item.quantity())
                        .append(", precio unitario ")
                        .append(item.unitPrice())
                        .append(", total ")
                        .append(item.lineTotal())
                        .append('\n');
            }
        }
        body.append("\nGracias por tu compra.");
        return body.toString();
    }

    private Instant resolveEventInstant(OrderPaidEvent event) {
        Object rawDate = event.metadata() == null ? null : event.metadata().get("paidAt");
        if (rawDate instanceof String rawString) {
            try {
                return Instant.parse(rawString);
            } catch (Exception ex) {
                log.debug("No se pudo parsear paidAt='{}' para orderId={}", rawString, event.orderId());
            }
        }
        return Instant.now();
    }
}
