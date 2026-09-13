package com.example.commerceplus.common.schedule;

import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.service.OrderCancelService;
import com.example.commerceplus.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j(topic = "orderCancelSchedule")
@Component
@RequiredArgsConstructor
public class OrderCancelScheduler {

    private final OrderService orderService;
    private final OrderCancelService orderCancelService;

    // 주문이 생성된지 30분동안 결제를 하지 않을경우 결제 취소
    @Scheduled(cron = "0 */1 * * * *")
    public void cancelOrder() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(30);
        List<Order> expiredOrderList = orderService.findPendingOrdersOlderThan(thresholdTime);

        for (Order expiredOrder : expiredOrderList) {
            try{
                orderCancelService.cancelOrderAndRestoreStock(expiredOrder.getId());
            }catch (Exception e){
                log.error("주문 취소 처리 실패 - 주문 ID: {}", expiredOrder.getId(), e);
            }
        }

    }

}
