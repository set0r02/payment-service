package unit;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.config.kafka.properties.KafkaTopicsProperties;
import com.innowise.paymentservice.dto.PaymentSummaryResponse;
import com.innowise.paymentservice.dto.event.PaymentCompletedEvent;
import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.Status;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Mock
    private KafkaTopicsProperties kafkaTopicsProperties;

    @Mock private RandomNumberClient randomNumberClient;

    @Test
    void createPayment() {

        PaymentInputDto input = new PaymentInputDto(1L, BigDecimal.TEN);

        Payment pending = Payment.builder()
                .orderId(1L)
                .userId(1L)
                .status(Status.PENDING)
                .paymentAmount(BigDecimal.TEN)
                .build();

        Payment success = Payment.builder()
                .orderId(1L)
                .userId(1L)
                .status(Status.SUCCESS)
                .paymentAmount(BigDecimal.TEN)
                .build();

        when(paymentRepository.save(any())).thenReturn(pending);
        when(randomNumberClient.getRandomNumber()).thenReturn("2");
        when(paymentRepository.save(any())).thenReturn(success);
        when(paymentMapper.toDto(any())).thenReturn(mock(PaymentOutputDto.class));
        when(kafkaTopicsProperties.paymentEvents()).thenReturn("payment-events");

        PaymentOutputDto result =
                paymentService.createPayment(input, 1L);

        assertNotNull(result);

        verify(kafkaTemplate, times(1))
                .send(eq("payment-events"), any(PaymentCompletedEvent.class));
    }

    @Test
    void findById() {

        Payment payment = Payment.builder().id(1L).build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toDto(payment))
                .thenReturn(mock(PaymentOutputDto.class));

        var result = paymentService.findById(1L);

        assertNotNull(result);
    }

    @Test
    void findPayments() {

        Payment payment = Payment.builder()
                .id(1L)
                .userId(1L)
                .orderId(1L)
                .status(Status.SUCCESS)
                .paymentAmount(BigDecimal.valueOf(100))
                .build();

        when(mongoTemplate.find(
                any(Query.class),
                eq(Payment.class)
        )).thenReturn(List.of(payment));

        when(paymentMapper.toDto(any()))
                .thenReturn(mock(PaymentOutputDto.class));

        var result = paymentService.findPayments(1L, Status.SUCCESS, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    void getUserSummary() {

        Document doc = new Document("total", 100);

        AggregationResults<Document> result = mock(AggregationResults.class);
        when(result.getUniqueMappedResult()).thenReturn(doc);

        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("payments"),
                eq(Document.class)
        )).thenReturn(result);

        PaymentSummaryResponse response =
                paymentService.getUserSummary(1L, null, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("100"), response.total());
    }

    @Test
    void getGlobalSummary() {

        Document doc = new Document("total", 250);

        AggregationResults<Document> result = mock(AggregationResults.class);
        when(result.getUniqueMappedResult()).thenReturn(doc);

        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("payments"),
                eq(Document.class)
        )).thenReturn(result);

        PaymentSummaryResponse response =
                paymentService.getGlobalSummary(null, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("250"), response.total());
    }
}
